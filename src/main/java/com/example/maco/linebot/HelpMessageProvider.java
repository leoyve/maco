package com.example.maco.linebot;

public final class HelpMessageProvider {
    public static final String HELP_MESSAGE = String.join("\n",
            "✨ Maestro 助理 v1.2 功能說明 ✨",
            "",
            "🧭 我會做的事",
            "• 新增 / 查詢「今天 / 這週 / 本月」待辦",
            "• 標記完成、刪除待辦",
            "• 新增 / 查詢體重",
            "",
            "🗣️ 範例指令",
            "• 明天下午三點跟 David 開會",
            "• 這週有什麼事",
            "• 週末去台中出差",
            "• 接下來還有什麼待辦事項",
            "• 今天早上體重 75.5 公斤",
            "• 最新的體重",
            "",
            "⏰ 時間規則（台北時區）",
            "• 相對日期：今天 / 明天 / 這週 / 下週 / 本月 / 週末",
            "• 時段補時：早上 09:00 ｜ 中午 12:00 ｜ 下午 15:00 ｜ 晚上 19:00 ｜ 睡前 22:00",
            "",
            "📍 地點",
            "• 抓「在 / 到 / 去 / 於」後面的詞（例：在台大醫院、去小巨蛋）",
            "",
            "🙋 不清楚？",
            "輸入：幫助 (help) 或 功能說明");

    private HelpMessageProvider() {
    }
}
