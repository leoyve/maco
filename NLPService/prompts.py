# prompts.py
from __future__ import annotations
from dataclasses import dataclass
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
import calendar
import textwrap

TAIPEI = ZoneInfo("Asia/Taipei")


@dataclass(frozen=True)
class DateAnchors:
    today_str: str
    yesterday_str: str
    tomorrow_str: str
    this_monday_start: str
    this_sunday_end: str
    next_monday: str
    next_sunday: str
    this_month_start: str
    this_month_end_day: str


def _anchors(now: datetime) -> DateAnchors:
    """Compute commonly used date anchors in Asia/Taipei, returned as 'YYYY-MM-DD' strings."""
    if now.tzinfo is None:
        now = now.replace(tzinfo=TAIPEI)
    today = now.date()
    yesterday = today - timedelta(days=1)
    tomorrow = today + timedelta(days=1)

    # Week anchors (Monday=0)
    weekday = today.weekday()
    this_monday = today - timedelta(days=weekday)
    this_sunday = this_monday + timedelta(days=6)
    next_monday = this_monday + timedelta(days=7)
    next_sunday = next_monday + timedelta(days=6)

    # Month anchors
    month_start = today.replace(day=1)
    last_day = calendar.monthrange(today.year, today.month)[1]
    month_end = today.replace(day=last_day)

    fmt = lambda d: d.strftime("%Y-%m-%d")

    return DateAnchors(
        today_str=fmt(today),
        yesterday_str=fmt(yesterday),
        tomorrow_str=fmt(tomorrow),
        this_monday_start=fmt(this_monday),
        this_sunday_end=fmt(this_sunday),
        next_monday=fmt(next_monday),
        next_sunday=fmt(next_sunday),
        this_month_start=fmt(month_start),
        this_month_end_day=fmt(month_end),
    )


def get_router_prompt(text: str) -> str:
    """Gets the prompt for the Domain Router."""
    examples = textwrap.dedent("""
    You are a domain classifier for a personal assistant bot. Your only task is to determine the user's primary goal and classify it into ONE of the following domains: "todo", "health", or "unrelated".
    You must respond ONLY with a JSON object containing a single key "domain".

    # EXAMPLES:
    ---
    Text: "提醒我明天下午三點要開會"
    JSON: {"domain": "todo"}
    ---
    Text: "明天要去打球"
    JSON: {"domain": "todo"}
    ---
    Text: "今天體重 75.5 公斤"
    JSON: {"domain": "health"}
    ---
    Text: "90"
    JSON: {"domain": "health"}
    ---
    Text: "今天天氣真好"
    JSON: {"domain": "unrelated"}
    ---
    Text: "後天要去泌尿科看醫生"
    JSON: {"domain": "todo"}
    ---
    Text: "買貓砂"
    JSON: {"domain": "todo"}
    ---
    Text: "還有什麼事情"
    JSON: {"domain": "todo"}
    ---
    # TASK:
    """).strip()

    return f"""{examples}
Text: "{text}"
JSON:
""".rstrip()


def get_todo_prompt(text: str) -> str:
    """Gets the detailed prompt for the To-Do module."""
    now = datetime.now(TAIPEI)
    A = _anchors(now)

    header = textwrap.dedent(f"""
    You are an NLU engine for the "To-do List" module. Your job: 從使用者一句話擷取待辦相關的 intent 與 entities。
    只回傳一個 JSON 物件（no extra text）。

    - 時間格式統一使用 "yyyy-MM-dd HH:mm"（若只有日期請補 "00:00" 或 "23:59" 作為查詢範圍邊界）。
    - time 欄位可以是:
      - null （沒有時間）
      - {{ "timestamp": "yyyy-MM-dd HH:mm" }} （單一時間點）
      - {{ "startDate": "yyyy-MM-dd HH:mm", "endDate": "yyyy-MM-dd HH:mm" }} （查詢範圍）
    - entities 結構：{{ "task": string|null, "time": object|null, "location": string|null, "status": "TODO"|"DONE"|null }}
    - 當 task 或時間不明確時，is_clear=false，並在 recommendation 放入中文提示要補的資訊；否則 is_clear=true 且 recommendation=null。
    - 以當前日期為基準推算相對日期（timezone=Asia/Taipei），並在 JSON 中回傳具體字串或 null。

    📅 口語化時間（time_aliases）
    {{
      "今天": ["today", "本日"],
      "明天": ["tomorrow"],
      "昨天": ["yesterday"],
      "這週": ["本週", "這星期", "本星期"],
      "下週": ["下星期", "下個禮拜"],
      "上週": ["上星期", "上個禮拜"],
      "這個月": ["本月"],
      "下個月": ["下月"],
      "上個月": ["上月"],
      "月底": ["月尾"],
      "年底": ["年尾"],
      "週末": ["周末", "weekend"]
    }}

    🕒 時段詞（僅在沒有明確時間時才套用）
    - 早上/上午 → 09:00
    - 中午 → 12:00
    - 下午 → 15:00
    - 傍晚/晚上 → 19:00
    - 深夜/夜間/睡前 → 22:00

    📐 規則
    1) 查詢意圖 (queryTodo)
       - 相對日期 → 輸出區間 {{startDate, endDate}}（請先計算後輸出實值）：
         - 今天："{A.today_str} 00:00" ~ "{A.today_str} 23:59"
         - 明天："{A.tomorrow_str} 00:00" ~ "{A.tomorrow_str} 23:59"
         - 這週/本週："{A.this_monday_start} 00:00" ~ "{A.this_sunday_end} 23:59"
         - 下週："{A.next_monday} 00:00" ~ "{A.next_sunday} 23:59"
         - 這個月/本月："{A.this_month_start} 00:00" ~ "{A.this_month_end_day} 23:59"
         - 週末：當週六 "00:00" ~ 當週日 "23:59"
         - 指定月份（如 2025/09）：該月起訖（例：2025-09-01 00:00 ~ 2025-09-30 23:59）

    2) 新增意圖 (addTodo)
       - 僅有日期、無具體時間 → time=null。
       - 出現時段詞（早上/下午/晚上/…）→ 以預設時刻補成 timestamp。
       - 僅說「週末」且無時段 → time=null（除非句中另有具體時段）。

    3) 地點抽取 (location)
       - 介系詞「在/到/去/於」後的地點片語，例如：在台大醫院、到台中出差、去林口體育場。
       - 地點可為城市/地標/場館名稱（台中、信義誠品、台北101、小巨蛋…）。

    4) 輸出
       - 僅回傳 JSON，鍵固定：intent, entities, is_clear, recommendation。

    Current date: {A.today_str}
    """).strip()

    examples = textwrap.dedent(f"""
    # EXAMPLES:
    ---
    Text: "提醒我明天下午三點跟 David 開會"
    JSON: {{ "intent": "addTodo", "entities": {{ "task": "跟 David 開會", "time": {{ "timestamp": "{A.tomorrow_str} 15:00" }}, "location": null, "status": "TODO" }}, "is_clear": true, "recommendation": null }}
    ---
    Text: "今天晚上在信義誠品看電影"
    JSON: {{ "intent": "addTodo", "entities": {{ "task": "看電影", "time": {{ "timestamp": "{A.today_str} 19:00" }}, "location": "信義誠品", "status": "TODO" }}, "is_clear": true, "recommendation": null }}
    ---
    Text: "下星期一要去林口體育場打羽球"
    JSON: {{ "intent": "addTodo", "entities": {{ "task": "打羽球", "time": {{ "timestamp": "{A.next_monday} 09:00" }}, "location": "林口體育場", "status": "TODO" }}, "is_clear": true, "recommendation": null }}
    ---
    Text: "這週有什麼事情"
    JSON: {{ "intent": "queryTodo", "entities": {{ "task": null, "time": {{ "startDate": "{A.this_monday_start} 00:00", "endDate": "{A.this_sunday_end} 23:59" }}, "location": null, "status": null }}, "is_clear": true, "recommendation": null }}
    ---
    Text: "本月的待辦"
    JSON: {{ "intent": "queryTodo", "entities": {{ "task": null, "time": {{ "startDate": "{A.this_month_start} 00:00", "endDate": "{A.this_month_end_day} 23:59" }}, "location": null, "status": null }}, "is_clear": true, "recommendation": null }}
    ---
    Text: "九月有什麼待辦事項"
    JSON: {{ "intent": "queryTodo", "entities": {{ "task": null, "time": {{ "startDate": "2025-09-01 00:00", "endDate": "2025-09-30 23:59" }}, "location": null, "status": null }}, "is_clear": true, "recommendation": null }}
    ---
    Text: "週末去台中出差"
    JSON: {{ "intent": "addTodo", "entities": {{ "task": "出差", "time": null, "location": "台中", "status": "TODO" }}, "is_clear": true, "recommendation": null }}
    ---
    Text: "明天早上跟設計師開會在台北101"
    JSON: {{ "intent": "addTodo", "entities": {{ "task": "跟設計師開會", "time": {{ "timestamp": "{A.tomorrow_str} 09:00" }}, "location": "台北101", "status": "TODO" }}, "is_clear": true, "recommendation": null }}
    ---
    Text: "今晚8點去小巨蛋看演唱會"
    JSON: {{ "intent": "addTodo", "entities": {{ "task": "看演唱會", "time": {{ "timestamp": "{A.today_str} 20:00" }}, "location": "小巨蛋", "status": "TODO" }}, "is_clear": true, "recommendation": null }}
    ---
    Text: "要開會"
    JSON: {{ "intent": "addTodo", "entities": {{ "task": null, "time": null, "location": null, "status": null }}, "is_clear": false, "recommendation": "請提供時間（日期/時段）與地點，例如：明天下午三點在公司開會。" }}
    ---
    Text: "接下來/還有什麼待辦事項"
    JSON: {{ "intent": "queryTodo", "entities": {{ "task": null, "time": {{ "startDate": "{A.today_str}", "endDate": "9999/12/31 23:59" }}, "location": null, "status": null }}, "is_clear": true, "recommendation": null }}
    ---
    """).strip()

    return f"""{header}

{examples}

# TASK:
Text: "{text}"
JSON:
""".rstrip()


def get_health_prompt(text: str) -> str:
    """Gets the detailed prompt for the Health module (timezone-consistent)."""
    now = datetime.now(TAIPEI)
    A = _anchors(now)
    today_str = A.today_str

    header = textwrap.dedent(f"""
    You are an NLU engine for the "Health Tracking" module.
    Possible intents are: "addWeightLog", "queryWeightLog".
    The current date is {today_str}. Timezone=Asia/Taipei.
    Respond ONLY with a valid JSON object.
    請依照現在的日期去推算「今天/明天/這週/下週/這個月」等相對時間，並輸出實際日期字串。
    """).strip()

    examples = textwrap.dedent(f"""
    # EXAMPLES:
    ---
    Text: "今天早上體重 75.5 公斤"
    JSON: {{ "intent": "addWeight", "entities": {{ "weight":"75.5" , "time": {{"timestamp": "{A.today_str} 09:00"}} }}, "is_clear": true, "recommendation": null }}
    ---
    Text: "100"
    JSON: {{ "intent": "addWeight", "entities": {{ "weight":"100", "time": {{"timestamp": "{A.today_str} 09:00"}} }}, "is_clear": true, "recommendation": null }}
    ---
    Text: "昨天量體重是 75.5"
    JSON: {{ "intent": "addWeight", "entities": {{ "weight":"75.5", "time": {{"timestamp": "{A.yesterday_str} 09:00"}} }}, "is_clear": true, "recommendation": null }}
    ---
    """).strip()

    return f"""{header}

{examples}

# TASK:
Text: "{text}"
JSON:
""".rstrip()