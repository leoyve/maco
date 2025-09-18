# service.py
import os
import json
import re
import asyncio
import logging
import sys
if "google.colab" in sys.modules:
    from google.colab import auth

    auth.authenticate_user()
     
# 引用我們新的 prompts.py 檔案
from prompts import get_router_prompt, get_todo_prompt, get_health_prompt
from dotenv import load_dotenv

from typing import List
from google import genai
from google.genai.types import (
    GenerateContentConfig,
    GoogleSearch,
    HarmBlockThreshold,
    HarmCategory,
    SafetySetting,
    ThinkingConfig,
    Tool,
    ToolCodeExecution,
)
from pydantic import BaseModel

# --- Model Initialization (Same as before) ---
try:
    load_dotenv()
    client = genai.Client(vertexai=True, project="even-acumen-472211-n2", location="global")

    MODEL_ID = "gemini-2.5-flash-lite" # 替換為你想使用的 Gemini 模型 ID
    #GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
    #genai.configure(api_key=GEMINI_API_KEY)
    #model = genai.GenerativeModel("Gemini 2.5 Flash-Lite")
except KeyError:
    raise RuntimeError("GEMINI_API_KEY environment variable not set.")
except Exception as e:
    raise RuntimeError(f"Failed to configure Gemini model: {e}")

async def get_domain(text: str) -> dict:
    """
    Stage 1: The Router. Determines the user's main intent (domain).
    """
    prompt = get_router_prompt(text)
    try:
        response = await asyncio.to_thread(client.models.generate_content, model=MODEL_ID, contents=prompt)
        raw = getattr(response, "text", None) or str(response)
        cleaned_text = re.sub(r'```[jJ][sS][oO][nN]?', '', raw).replace('```', '').strip()
        result = json.loads(cleaned_text)
        return result
    except Exception as e:
        return {"error": f"Router failed: {str(e)}", "raw_response": raw if 'raw' in locals() else None}


async def process_domain_specific_text(domain: str, text: str) -> dict:
    """
    Stage 2: The Processor. Selects the correct prompt based on the domain and analyzes the text.
    """
    # 根據 domain 選擇對應的「部門專家」Prompt
    if domain == "todo":
        prompt = get_todo_prompt(text)
    elif domain == "health":
        prompt = get_health_prompt(text)
    else: # "unrelated" or other domains we haven't implemented
        return {"domain": domain, "intent": "unrelated", "entities": None}

    try:
        response = await asyncio.to_thread(client.models.generate_content, model=MODEL_ID, contents=prompt)
        raw = getattr(response, "text", None) or str(response)
        cleaned_text = re.sub(r'```[jJ][sS][oO][nN]?', '', raw).replace('```', '').strip()
        result = json.loads(cleaned_text)
        # 在最終結果中，再次把 domain 加上，方便後端處理
        result["domain"] = domain
        return {"result": result}
    except Exception as e:
        return {"error": f"Processor failed for domain '{domain}': {str(e)}", "raw_response": raw if 'raw' in locals() else None}