package com.example.maco.infra.jpa.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    private static final DateTimeFormatter DEFAULT_DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateTimeUtils() {
        // utility
    }

    /**
     * 解析常見的代辦時間字串為 Instant。
     * 支援格式："yyyy-MM-dd HH:mm"（以系統時區轉換）或 ISO-8601 (Instant.parse)。
     * 解析失敗回傳 null。
     */
    public static Instant parseToInstant(String ts) {
        if (ts == null || ts.isBlank())
            return null;
        try {
            LocalDateTime ldt = LocalDateTime.parse(ts, DEFAULT_DTF);
            return ldt.atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            try {
                return Instant.parse(ts);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * 將 Instant 以系統時區格式化為 "yyyy-MM-dd HH:mm"。
     * 若 instant 為 null，回傳空字串。
     */
    public static String formatInstantToLocal(Instant instant) {
        if (instant == null)
            return "";
        return DEFAULT_DTF.withZone(ZoneId.systemDefault()).format(instant);
    }

    public static String formatInstantToLocal(String ts) {
        Instant instant = parseToInstant(ts);
        return formatInstantToLocal(instant);
    }
}
