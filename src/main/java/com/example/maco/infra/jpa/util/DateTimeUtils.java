package com.example.maco.infra.jpa.util;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Locale;

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

    // --- Flex UI helpers ---
    public static String formatFlexDayLabel(Instant ts, ZoneId zone) {
        if (ts == null)
            return "";
        ZonedDateTime zdt = ZonedDateTime.ofInstant(ts, zone);
        LocalDate date = zdt.toLocalDate();
        LocalDate today = LocalDate.now(zone);
        long diff = ChronoUnit.DAYS.between(today, date);
        if (diff == 0)
            return "今天";
        if (diff == 1)
            return "明天";
        if (diff == 2)
            return "後天";
        // If the date is within the same ISO week as today, return the weekday name
        // (星期一..星期日)
        DayOfWeek targetDow = date.getDayOfWeek();

        // Determine the week-based year and week number to compare week membership
        WeekFields wf = WeekFields.ISO;
        int targetWeek = date.get(wf.weekOfWeekBasedYear());
        int todayWeek = today.get(wf.weekOfWeekBasedYear());
        int targetYear = date.get(wf.weekBasedYear());
        int todayYear = today.get(wf.weekBasedYear());

        if (targetYear == todayYear && targetWeek == todayWeek) {
            // Map DayOfWeek to Chinese weekday
            switch (targetDow) {
                case MONDAY:
                    return "星期一";
                case TUESDAY:
                    return "星期二";
                case WEDNESDAY:
                    return "星期三";
                case THURSDAY:
                    return "星期四";
                case FRIDAY:
                    return "星期五";
                case SATURDAY:
                    return "星期六";
                case SUNDAY:
                default:
                    return "星期日";
            }
        }

        return date.format(DateTimeFormatter.ofPattern("M月d日", Locale.TAIWAN));
    }

    public static String formatFlexTime(Instant ts, ZoneId zone) {
        if (ts == null)
            return "";
        ZonedDateTime zdt = ZonedDateTime.ofInstant(ts, zone);
        return zdt.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
