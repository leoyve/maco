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
# TASK:
Text: "{text}"
JSON:
"""

def get_todo_prompt(text: str) -> str:
    """Gets the detailed prompt for the To-Do module."""
    today_str = datetime.now().strftime("%Y-%m-%d")
    # ... 這裡貼上您之前寫的、專門用於「待辦事項」的詳細 Prompt ...
    # (包含 addTodo, queryTodo, modifyTodoStatus 等範例)
    return f"""
You are an NLU engine for the "To-do List" module.
Your task is to analyze the user's text and extract entities related to to-do items.
Possible intents are: "addTodo", "queryTodo", "modifyTodoStatus".
The current date is {today_str}.
Respond ONLY with a valid JSON object.

# EXAMPLES:
---
Text: "提醒我明天下午三點跟 David 開會"
JSON: {{ "intent": "addTodo", "entities": {{ "task": "跟 David 開會", "time": {{"timestamp": "2025-08-19 15:00"}}, "location": null, "status": "TODO"  }}, "is_clear": true, "recommendation": null }}
---
Text: "把開會那件事標示為完成"
JSON: {{ "intent": "modifyTodoStatus", "target_task": {{"task": "開會"}}, "new_status": "DONE", "is_clear": true, "recommendation": null }}
---
Text: "下星期一要去林口體育場打羽球"
JSON: {{ "intent": "addTodo", "entities": {{ "task": "打羽球", "time": {{"timestamp": "2025-08-25 00:00"}}, "location": 林口體育場, "status": "TODO"  }}, "is_clear": true, "recommendation": null }}
---
Text: "這星期有什麼事情"
JSON: {{ "intent": "queryTodo", "entities": {{"task": null, time": {{"startDate": "2025-08-18 00:00", "endDate": "2025-08-24 23:59"}}, "location": null, "status": null }}, "is_clear": true, "recommendation": null}}
---
# TASK:
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