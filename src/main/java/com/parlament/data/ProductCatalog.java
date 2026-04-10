package com.parlament.data;

import com.parlament.model.Category;
import com.parlament.model.Product;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Каталог товаров магазина Parlament.
 * Мужская классическая одежда с корректными фотографиями.
 */
public class ProductCatalog {

    private static final Map<String, Product> PRODUCTS_BY_ID = new LinkedHashMap<>();
    private static final List<Product> ALL_PRODUCTS = new ArrayList<>();

    static {
        // ─────────────────────────── КОСТЮМЫ ───────────────────────────
        addProduct(new Product(
            "suit-001",
            "Костюм Milano двубортный",
            "Шедевр итальянского пошива. Сшит из шерсти 130s Vitale Barberis Canonico, " +
            "тёмно-синий двубортный костюм с пиковыми лацканами и зауженной талией. " +
            "Полный канвас для идеального драпирования, которое улучшается с каждой ноской.",
            new BigDecimal("1290.00"),
            "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800&q=80",
            Category.SUITS
        ));

        addProduct(new Product(
            "suit-002",
            "Костюм Regent Street угольный",
            "Авторитет в офисе и элегантность на выходных. Супер 120s английская шерсть " +
            "в тонкую угольную ёлочку. Однобортный с зубчатыми лацканами и двумя пуговицами. " +
            "Прямые брюки с подрубкой в современном силуэте.",
            new BigDecimal("980.00"),
            "https://images.unsplash.com/photo-1594938298603-c8148c4b4f07?w=800&q=80",
            Category.SUITS
        ));

        addProduct(new Product(
            "suit-003",
            "Летний льняной костюм Riviera",
            "Элегантность для жаркого климата. 100% бельгийский лён в тёплом бежевом тоне. " +
            "Нелинованный пиджак с мягкими плечами, накладными карманами и окантованными швами. " +
            "Лёгкий, дышащий и безупречно стильный для настоящего джентльмена.",
            new BigDecimal("750.00"),
            "https://images.unsplash.com/photo-1617137968427-85924c800a22?w=800&q=80",
            Category.SUITS
        ));

        addProduct(new Product(
            "suit-004",
            "Смокинг Black Tie",
            "Безупречный вечерний костюм. Чёрная шерсть с пиковым лацканом из гроссгрена " +
            "и одной пуговицей, покрытой атласом. В комплекте брюки с атласной лампасой. " +
            "Для вечеров, где допустимо только совершенство.",
            new BigDecimal("1590.00"),
            "https://images.unsplash.com/photo-1598808503746-f34cfabd2cd6?w=800&q=80",
            Category.SUITS
        ));

        // ─────────────────────────── РУБАШКИ ───────────────────────────
        addProduct(new Product(
            "shirt-001",
            "Белая классическая рубашка поплин",
            "Основа гардероба каждого джентльмена. Египетский хлопок Giza 100, 200 нитей — " +
            "для яркой, чёткой фактуры. Широкий воротник, перламутровые пуговицы и " +
            "французская планка. Приталенный силуэт.",
            new BigDecimal("195.00"),
            "https://images.unsplash.com/photo-1603252109303-2751441dd157?w=800&q=80",
            Category.SHIRTS
        ));

        addProduct(new Product(
            "shirt-002",
            "Рубашка Oxford в синюю полоску",
            "Повседневная классика, возведённая до уровня мастерства. Хлопок 120s в насыщенную " +
            "синюю полоску. Пуговичный воротник с мягким рулоном, манжеты на пуговице " +
            "и нагрудный карман. Расслабленная, но изысканная.",
            new BigDecimal("175.00"),
            "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=800&q=80",
            Category.SHIRTS
        ));

        addProduct(new Product(
            "shirt-003",
            "Рубашка Sea Island слоновая кость",
            "Сверхредкий хлопок Sea Island с Карибских островов, тканный в шелковистое саржевое " +
            "полотно слоновой кости. Полу-широкий воротник, исключительно мягкая на ощупь, " +
            "с тонким блеском. Двойные манжеты под запонки.",
            new BigDecimal("285.00"),
            "https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=800&q=80",
            Category.SHIRTS
        ));

        addProduct(new Product(
            "shirt-004",
            "Фланелевая рубашка в клетку",
            "Двойная шотландская фланель в приглушённую зелено-коричневую клетку. " +
            "Свободный воротник и крой делают её идеальной для загородного уик-энда или " +
            "городского кэжуала. Предварительно стиранная для мгновенного комфорта.",
            new BigDecimal("155.00"),
            "https://images.unsplash.com/photo-1588359348347-9bc6cbbb689e?w=800&q=80",
            Category.SHIRTS
        ));

        // ─────────────────────────── ОБУВЬ ───────────────────────────
        addProduct(new Product(
            "shoes-001",
            "Оксфорды Capri с броговой перфорацией",
            "Изготовлены вручную в регионе Марке (Италия) по технологии Goodyear welt для " +
            "возможности перетяжки подошвы. Полнозернистая телячья кожа в насыщенном рыжем " +
            "тоне с ручной полировкой носка. Кожаная подкладка, амортизирующая стелька.",
            new BigDecimal("595.00"),
            "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80",
            Category.SHOES
        ));

        addProduct(new Product(
            "shoes-002",
            "Чёрные дерби Kensington",
            "Идеальный чёрный туфель для города. Конструкция Norwegian welt на стройной, " +
            "элегантной колодке. Бокс-калф с зеркальным блеском на носке. " +
            "Подходит к серым и тёмно-синим костюмам.",
            new BigDecimal("545.00"),
            "https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?w=800&q=80",
            Category.SHOES
        ));

        addProduct(new Product(
            "shoes-003",
            "Лоферы Venetian цвет коньяк",
            "Настоящие ремесленные лоферы в духе классического пенни-стайла. Мягкая телячья " +
            "кожа цвета коньяка, ручная мокасинная прошивка, кожаная подошва с резиновым " +
            "каблуком. Идеально с брюками чинос или фланелью.",
            new BigDecimal("480.00"),
            "https://images.unsplash.com/photo-1631988665834-55a4e3fe1de0?w=800&q=80",
            Category.SHOES
        ));

        addProduct(new Product(
            "shoes-004",
            "Замшевые челси — тёмный бургунди",
            "Современная интерпретация викторианской классики. Верх из тёмно-бургундской замши " +
            "на стройной колодке с миндалевидным носком и наборном кожаном каблуке. " +
            "Эластичные вставки по бокам. Резиновая подошва Dainite для города.",
            new BigDecimal("620.00"),
            "https://images.unsplash.com/photo-1608256246200-53e635b5b65f?w=800&q=80",
            Category.SHOES
        ));

        // ─────────────────────────── АКСЕССУАРЫ ───────────────────────────
        addProduct(new Product(
            "acc-001",
            "Шёлковый галстук гренадин — тёмно-синий",
            "Соткан в Комо (Италия) из 100% шёлка гренадин. Тёмно-синий галстук с тонкой " +
            "текстурой — универсальное дополнение к любой коллекции. Ручная подгибка, " +
            "конструкция семь-фолд без подкладки. Ширина 8,5 см.",
            new BigDecimal("165.00"),
            "https://images.unsplash.com/photo-1589756823695-278bc923f962?w=800&q=80",
            Category.ACCESSORIES
        ));

        addProduct(new Product(
            "acc-002",
            "Кашемировый платок — слоновая кость",
            "Соткан из кашемира класса А монгольского происхождения в чистом тоне слоновой кости. " +
            "Ручная подгибка и ручная прошивка. Сдержанное, но роскошное дополнение к любому " +
            "пиджаку. Поставляется в фирменной подарочной коробке Parlament.",
            new BigDecimal("95.00"),
            "https://images.unsplash.com/photo-1520903920243-00d872a2d1c9?w=800&q=80",
            Category.ACCESSORIES
        ));

        addProduct(new Product(
            "acc-003",
            "Шарф из мериноса — верблюжий",
            "Невероятно мягкий двухслойный шарф из шерсти мериноса в классическом верблюжьем тоне. " +
            "Длина 190 см, широкий для разнообразных способов завязывания. Соткан в Шотландии. " +
            "Идеальный спутник с сентября по март.",
            new BigDecimal("185.00"),
            "https://images.unsplash.com/photo-1601924994987-69e26d50dc26?w=800&q=80",
            Category.ACCESSORIES
        ));

        addProduct(new Product(
            "acc-004",
            "Запонки из нержавеющей стали — гильоше",
            "Точно обработаны из хирургической стали 316L с гравировкой гильоше и полированным " +
            "ободком. Застёжка Т-образная. Изысканный штрих для рубашек с двойными манжетами. " +
            "Поставляются в кожаной коробке для запонок Parlament.",
            new BigDecimal("125.00"),
            "https://images.unsplash.com/photo-1611085583191-a3b181a88401?w=800&q=80",
            Category.ACCESSORIES
        ));

        addProduct(new Product(
            "acc-005",
            "Ремень из растительно-дублёной кожи — тёмно-коричневый",
            "Ремень, который расскажет историю многолетней носки. Полнозернистая кожа " +
            "растительного дубления из семейной дубильни в Тоскане. Ширина 35 мм, латунная " +
            "однозубая пряжка. Кожа развивает насыщенную личную патину.",
            new BigDecimal("145.00"),
            "https://images.unsplash.com/photo-1624222247344-550fb60583dc?w=800&q=80",
            Category.ACCESSORIES
        ));
    }

    private static void addProduct(Product product) {
        PRODUCTS_BY_ID.put(product.getId(), product);
        ALL_PRODUCTS.add(product);
    }

    public static List<Product> getAllProducts() {
        return Collections.unmodifiableList(ALL_PRODUCTS);
    }

    public static Optional<Product> findById(String id) {
        return Optional.ofNullable(PRODUCTS_BY_ID.get(id));
    }

    public static List<Product> findByCategory(Category category) {
        return ALL_PRODUCTS.stream()
                .filter(p -> p.getCategory() == category)
                .collect(Collectors.toList());
    }

    public static int getTotalCount() {
        return ALL_PRODUCTS.size();
    }
}
