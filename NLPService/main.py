"""NLPService FastAPI app

提供兩個主要 endpoint：
- POST /router  -> 分類文字成 domain
- POST /process -> 根據 domain 做進一步語意解析

這個檔案採用簡單的單檔結構以便開發與快速部署；已在 README 中記錄如何用 Docker 建置與執行。
"""

from fastapi import FastAPI, HTTPException
from models import RouterRequest, RouterResponse, ProcessRequest, ErrorResponse
from service import get_domain, process_domain_specific_text
from dotenv import load_dotenv
import logging
from typing import Any, Dict

load_dotenv()

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(message)s')
logger = logging.getLogger("NLPService")

# OpenAPI tag definitions for Swagger UI grouping
openapi_tags = [
    {"name": "NLP", "description": "NLP endpoints: /router (domain classification) and /process (domain-specific processing)."},
    {"name": "Health Check", "description": "Health and status endpoints."}
]

app = FastAPI(title="Multi-Domain NLP Service", version="2.0.0", openapi_tags=openapi_tags)

@app.post("/router",
          response_model=RouterResponse,
          summary="Stage 1: Classify user text into a domain",
          tags=["NLP"],
          responses={500: {"model": ErrorResponse}})
async def route_request(request: RouterRequest) -> RouterResponse:
    """
    Receives text and determines its primary domain (e.g., 'todo', 'health').
    """
    result = await get_domain(request.text)
    if "error" in result:
        raise HTTPException(status_code=500, detail=result)
    return result

@app.post("/process", 
          summary="Stage 2: Process text with a domain-specific engine",
          tags=["NLP"],
          responses={500: {"model": ErrorResponse}})
async def process_request(request: ProcessRequest) -> Dict[str, Any]:
    """
    Receives text AND a domain, then performs detailed NLU analysis.
    """
    logger.info(f"/process 收到: domain={request.domain}, text={request.text}, request={request}")
    result = await process_domain_specific_text(request.domain, request.text)
    if "error" in result:
        raise HTTPException(status_code=500, detail=result)
    return result

@app.get("/", tags=["Health Check"])
def root() -> Dict[str, str]:
    return {"message": "Multi-Domain NLP Service is running."}