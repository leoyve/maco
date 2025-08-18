# service.py
import os
import json
import re
import google.generativeai as genai
# 引用我們新的 prompts.py 檔案
from prompts import get_router_prompt, get_todo_prompt, get_health_prompt
from dotenv import load_dotenv


# --- Model Initialization (Same as before) ---
try:
    load_dotenv()
    GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
    genai.configure(api_key=GEMINI_API_KEY)
    model = genai.GenerativeModel("gemini-2.0-flash")
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
        response = await model.generate_content_async(prompt)
        cleaned_text = re.sub(r'```[jJ][sS][oO][nN]?', '', response.text).replace('```', '').strip()
        result = json.loads(cleaned_text)
        return result
    except Exception as e:
        return {"error": f"Router failed: {str(e)}", "raw_response": response.text if 'response' in locals() else None}


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
        response = await model.generate_content_async(prompt)
        cleaned_text = re.sub(r'```[jJ][sS][oO][nN]?', '', response.text).replace('```', '').strip()
        result = json.loads(cleaned_text)
        # 在最終結果中，再次把 domain 加上，方便後端處理
        result["domain"] = domain
        return result
    except Exception as e:
        return {"error": f"Processor failed for domain '{domain}': {str(e)}", "raw_response": response.text if 'response' in locals() else None}