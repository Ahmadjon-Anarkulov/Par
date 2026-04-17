package com.parlament.service;

import com.parlament.config.AdminProperties;
import com.parlament.persistence.entity.OrderEntity;
import com.parlament.persistence.entity.Role;
import com.parlament.persistence.entity.TelegramUserEntity;
import com.parlament.persistence.repo.OrderJpaRepository;
import com.parlament.persistence.repo.TelegramUserJpaRepository;
import com.parlament.telegram.TelegramBotSender;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private static final int PAGE_SIZE = 5;

    private final TelegramUserJpaRepository telegramUserRepo;
    private final OrderJpaRepository orderRepo;
    private final AdminProperties adminProperties;

    public AdminService(TelegramUserJpaRepository telegramUserRepo,
                        OrderJpaRepository orderRepo,
                        AdminProperties adminProperties) {
        this.telegramUserRepo = telegramUserRepo;
        this.orderRepo = orderRepo;
        this.adminProperties = adminProperties;
    }

    // ─────────────────────────── Bootstrap ───────────────────────────────────

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void ensureInitialAdmins() {
        if (telegramUserRepo.existsByRole(Role.ADMIN)) return;

        List<Long> initialIds = adminProperties.getInitialIds();
        if (initialIds == null || initialIds.isEmpty()) {
            log.warn("No admins exist yet, and admin.initial-ids is empty.");
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (Long id : initialIds) {
            if (id == null) continue;
            TelegramUserEntity user = telegramUserRepo.findById(id).orElseGet(() -> {
                TelegramUserEntity e = new TelegramUserEntity();
                e.setTelegramUserId(id);
                e.setCreatedAt(now);
                e.setUpdatedAt(now);
                return e;
            });
            user.setRole(Role.ADMIN);
            user.setUpdatedAt(now);
            telegramUserRepo.save(user);
            log.info("Bootstrapped ADMIN userId={}", id);
        }
    }

    // ─────────────────────────── Access checks ───────────────────────────────

    @Transactional(readOnly = true)
    public boolean isAdmin(long telegramUserId) {
        return telegramUserRepo.findById(telegramUserId)
                .map(TelegramUserEntity::isAdmin)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Set<Long> getAdminIds() {
        return telegramUserRepo.findByRole(Role.ADMIN).stream()
                .map(TelegramUserEntity::getTelegramUserId)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void promoteToAdmin(long actorId, long targetId) {
        if (!isAdmin(actorId)) throw new SecurityException("Forbidden: only admins can promote others.");

        OffsetDateTime now = OffsetDateTime.now();
        TelegramUserEntity target = telegramUserRepo.findById(targetId).orElseGet(() -> {
            TelegramUserEntity e = new TelegramUserEntity();
            e.setTelegramUserId(targetId);
            e.setCreatedAt(now);
            e.setUpdatedAt(now);
            return e;
        });
        if (target.isAdmin()) return;
        target.setRole(Role.ADMIN);
        target.setUpdatedAt(now);
        telegramUserRepo.save(target);
        log.info("Admin {} promoted user {} to ADMIN", actorId, targetId);
    }

    // ─────────────────────────── Orders ──────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<OrderEntity> getAllOrdersPaged(int page) {
        return orderRepo.findAll(
                PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Transactional(readOnly = true)
    public long getTotalOrderCount() {
        return orderRepo.count();
    }

    // ─────────────────────────── Users ───────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<TelegramUserEntity> getAllUsersPaged(int page) {
        return telegramUserRepo.findAll(
                PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return telegramUserRepo.count();
    }

    // ─────────────────────────── Statistics ──────────────────────────────────

    @Transactional(readOnly = true)
    public AdminStats getStats() {
        long totalOrders = orderRepo.count();
        long totalUsers = telegramUserRepo.count();
        Map<String, Long> byStatus = orderRepo.findAll().stream()
                .collect(Collectors.groupingBy(OrderEntity::getStatus, Collectors.counting()));
        return new AdminStats(totalOrders, totalUsers, byStatus);
    }

    public record AdminStats(long totalOrders, long totalUsers, Map<String, Long> ordersByStatus) {}

    // ─────────────────────────── Broadcast ───────────────────────────────────

    /**
     * Sends a text message to every known user.
     * Returns number of successfully sent messages.
     */
    @Transactional(readOnly = true)
    public int broadcastMessage(String text, TelegramBotSender sender) {
        List<TelegramUserEntity> users = telegramUserRepo.findAll();
        int sent = 0;
        for (TelegramUserEntity user : users) {
            try {
                SendMessage msg = new SendMessage();
                msg.setChatId(user.getTelegramUserId().toString());
                msg.setText("📢 <b>Сообщение от Parlament</b>\n\n" + text);
                msg.setParseMode("HTML");
                sender.sendText(msg);
                sent++;
            } catch (Exception e) {
                log.warn("Broadcast failed for user {}: {}", user.getTelegramUserId(), e.getMessage());
            }
        }
        log.info("Broadcast complete: {}/{} messages sent", sent, users.size());
        return sent;
    }

    // ─────────────────────────── Excel export ────────────────────────────────

    /**
     * Exports all orders to an Excel (.xlsx) file and sends it to the requesting admin.
     */
    @Transactional(readOnly = true)
    public void exportOrdersToExcel(long adminChatId, TelegramBotSender sender) {
        List<OrderEntity> orders = orderRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Orders");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row header = sheet.createRow(0);
            String[] columns = {"Номер заказа", "Telegram ID", "Имя", "Телефон", "Адрес", "Статус", "Дата"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            int rowNum = 1;
            for (OrderEntity order : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getOrderId());
                row.createCell(1).setCellValue(order.getTelegramUserId());
                row.createCell(2).setCellValue(nvl(order.getCustomerName()));
                row.createCell(3).setCellValue(nvl(order.getPhoneNumber()));
                row.createCell(4).setCellValue(nvl(order.getDeliveryAddress()));
                row.createCell(5).setCellValue(nvl(order.getStatus()));
                row.createCell(6).setCellValue(
                        order.getCreatedAt() != null ? order.getCreatedAt().format(fmt) : "");
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            // Write to byte array and send
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            byte[] bytes = baos.toByteArray();

            SendDocument doc = new SendDocument();
            doc.setChatId(String.valueOf(adminChatId));
            doc.setDocument(new InputFile(new ByteArrayInputStream(bytes),
                    "parlament_orders_" + System.currentTimeMillis() + ".xlsx"));
            doc.setCaption("📊 Экспорт заказов — " + orders.size() + " записей");
            sender.sendDocument(doc);
            log.info("Excel export sent to admin {}, {} rows", adminChatId, orders.size());

        } catch (Exception e) {
            log.error("Excel export failed: {}", e.getMessage(), e);
            SendMessage msg = new SendMessage();
            msg.setChatId(String.valueOf(adminChatId));
            msg.setText("⚠️ Ошибка при формировании Excel-файла: " + e.getMessage());
            sender.sendText(msg);
        }
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
