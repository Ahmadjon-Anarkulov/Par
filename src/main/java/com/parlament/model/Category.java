package com.parlament.model;

public enum Category {

    SUITS("👔 Костюмы", "suits"),
    SHIRTS("👕 Рубашки", "shirts"),
    SHOES("👞 Обувь", "shoes"),
    ACCESSORIES("⌚ Аксессуары", "accessories");

    private final String displayName;
    private final String callbackPrefix;

    Category(String displayName, String callbackPrefix) {
        this.displayName = displayName;
        this.callbackPrefix = callbackPrefix;
    }

    public String getDisplayName() { return displayName; }
    public String getCallbackPrefix() { return callbackPrefix; }

    public static Category fromCallbackPrefix(String prefix) {
        for (Category c : values()) {
            if (c.callbackPrefix.equals(prefix)) return c;
        }
        return null;
    }
}
