# prompts.py
from datetime import datetime

def get_router_prompt(text: str) -> str:
    """Gets the prompt for the Domain Router."""
    return f"""
You are a domain classifier for a personal assistant bot. Your only task is to determine the user's primary goal and classify it into ONE of the following domains: "todo", "health", or "unrelated".
You must respond ONLY with a JSON object containing a single key "domain".

# EXAMPLES:
---
Text: "提醒我明天下午三點要開會"
JSON: {{"domain": "todo"}}
---
Text: "明天要去打球"
JSON: {{"domain": "todo"}}
---
Text: "今天體重 75.5 公斤"
JSON: {{"domain": "health"}}
---
Text: "90"
JSON: {{"domain": "health"}}
---
Text: "今天天氣真好"
JSON: {{"domain": "unrelated"}}
---
Text: "後天要去泌尿科看醫生"
JSON: {{"domain": "todo"}}
---
Text: "買貓砂"
JSON: {{"domain": "todo"}}
---
Text: "還有什麼事情"
JSON: {{"domain": "todo"}}
---
# TASK:
Text: "{text}"
JSON:
"""

def get_todo_prompt(text: str) -> str:
    """Gets the detailed prompt for the To-Do module."""
    from datetime import datetime, timedelta
    today = datetime.now().date()
    today_str = today.strftime("%Y-%m-%d")
    tomorrow_str = (today + timedelta(days=1)).strftime("%Y-%m-%d")
    day_after_str = (today + timedelta(days=2)).strftime("%Y-%m-%d")
    this_monday = today - timedelta(days=today.weekday())
    this_sunday = this_monday + timedelta(days=6)
    this_monday_start = this_monday.strftime("%Y-%m-%d") + " 00:00"
    this_sunday_end = this_sunday.strftime("%Y-%m-%d") + " 23:59"
    next_monday = (this_monday + timedelta(days=7)).strftime("%Y-%m-%d")

    return f"""
You are an NLU engine for the "To-do List" module. Your job:從使用者一句話擷取待辦相關的 intent 與 entities。
- 只回傳一個 JSON 物件（no extra text）。
- 時間格式統一使用 "yyyy-MM-dd HH:mm"（若只有日期請補 "00:00" 或 "23:59" 作為範圍邊界）。
- time 欄位可以是:
  - null （沒有時間）
  - {{ "timestamp": "yyyy-MM-dd HH:mm" }} （單一時間點）
  - {{ "startDate": "yyyy-MM-dd HH:mm", "endDate": "yyyy-MM-dd HH:mm" }} （查詢範圍）
- entities 結構：{{ "task": string|null, "time": object|null, "location": string|null, "status": "TODO"|"DONE"|null }}

請務必以當前日期為基準推算「明天/下星期/下個月」等相對日期，並在 JSON 中回傳具體的 yyyy-MM-dd HH:mm 字串或 null。

Current date: {today_str}
# EXAMPLES:
---
Text: "提醒我明天下午三點跟 David 開會"
JSON: {{ "intent": "addTodo", "entities": {{ "task": "跟 David 開會", "time": {{ "timestamp": "{tomorrow_str} 15:00" }}, "location": null, "status": "TODO" }}, "is_clear": true, "recommendation": null }}
---
Text: "買貓砂"
JSON: {{ "intent": "addTodo", "entities": {{ "task": "買貓砂", "time": null, "location": null, "status": "TODO" }}, "is_clear": true, "recommendation": null }}
---
Text: "下星期一要去林口體育場打羽球"
JSON: {{ "intent": "addTodo", "entities": {{ "task": "打羽球", "time": {{ "timestamp": "{next_monday} 09:00" }}, "location": "林口體育場", "status": "TODO" }}, "is_clear": true, "recommendation": null }}
---
Text: "這星期有什麼事情"
JSON: {{ "intent": "queryTodo", "entities": {{ "task": null, "time": {{ "startDate": "{this_monday_start}", "endDate": "{this_sunday_end}" }}, "location": null, "status": null }}, "is_clear": true, "recommendation": null }}
---
Text: "要開會"
JSON: {{ "intent": "addTodo", "entities": {{ "task": null, "time": null, "location": null, "status": null }}, "is_clear": false, "recommendation": "請提供時間或地點等細節。" }}
---
Text: "明天有什麼事"
JSON: {{ "intent": "queryTodo", "entities": {{ "task": null, "time": {{ "startDate": "{tomorrow_str} 00:00", "endDate": "{tomorrow_str} 23:59" }}, "location": null, "status": null }}, "is_clear": true, "recommendation": null }}
---
Text: "我還有什麼事"
JSON: {{ "intent": "queryTodo", "entities": {{ "task": null, "time": {{ "startDate": "{today_str} 00:00", "endDate": "9999/12/31 23:59" }}, "location": null, "status": null }}, "is_clear": true, "recommendation": null }}
---
TASK:
Text: "{text}"
JSON:
"""

def get_health_prompt(text: str) -> str:
    """Gets the detailed prompt for the Health module."""
    today_str = datetime.now().strftime("%Y-%m-%d")
    # ... 這裡貼上專門用於「健康管理」的詳細 Prompt ...
    return f"""
You are an NLU engine for the "Health Tracking" module.
Possible intents are: "addWeightLog", "queryWeightLog".
The current date is {today_str}.
Respond ONLY with a valid JSON object.
請依照現在的日期去推算明後天，下星期下個月等日期  {today_str}.

# EXAMPLES:
---
Text: "今天早上體重 75.5 公斤"
JSON: {{ "intent": "addWeightLog", "entities": {{ "weight": {{ "value": 75.5, "unit": "kg" }}, "time": {{"timestamp": "{today_str} 09:00"}} }}, "is_clear": true, "recommendation": null }}
---
Text: "100"
JSON: {{ "intent": "addWeightLog", "entities": {{ "weight": {{ "value": 100, "unit": "kg" }}, "time": {{"timestamp": "{today_str} 09:00"}} }}, "is_clear": true, "recommendation": null }}
---
Text: "我今天的體重是多少？"
JSON: {{ "intent": "queryWeightLog", "entities": {{"time": {{"startDate": "{today_str} 00:00", "endDate": "{today_str} 23:59"}}}}, "is_clear": true, "recommendation": null }}
---
Text: "幫我查一下我這禮拜的體重紀錄"
JSON: {{"intent": "queryWeightLog", "entities": {{ "time": {{ "startDate": "2025-08-18 00:00", "endDate": "2025-08-24 23:59" }} }}, "is_clear": true, "recommendation": null }}
---
Text: "最新的體重"
JSON: {{"intent": "queryWeightLog", "entities": {{"time": "latest" }}, "is_clear": true, "recommendation": null }},
---
Text: "下個月的體重"
JSON: {{"intent": "queryWeightLog", "entities": {{"time": "latest" }}, "is_clear": false, "recommendation": "體重不能預知！" }},
---
# TASK:
Text: "{text}"
JSON:
"""