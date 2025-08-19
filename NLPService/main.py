# main.py
from fastapi import FastAPI, HTTPException
from models import RouterRequest, RouterResponse, ProcessRequest, ErrorResponse
from service import get_domain, process_domain_specific_text
from dotenv import load_dotenv
import logging

load_dotenv()

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(message)s')
logger = logging.getLogger("NLPService")

app = FastAPI(title="Multi-Domain NLU Service", version="2.0.0")

@app.post("/router",
          response_model=RouterResponse,
          summary="Stage 1: Classify user text into a domain",
          responses={500: {"model": ErrorResponse}})
async def route_request(request: RouterRequest):
    """
    Receives text and determines its primary domain (e.g., 'todo', 'health').
    """
    result = await get_domain(request.text)
    if "error" in result:
        raise HTTPException(status_code=500, detail=result)
    return result

@app.post("/process",
          summary="Stage 2: Process text with a domain-specific engine",
          responses={500: {"model": ErrorResponse}})
async def process_request(request: ProcessRequest):
    """
    Receives text AND a domain, then performs detailed NLU analysis.
    """
    logger.info(f"/process 收到: domain={request.domain}, text={request.text}, request={request}")
    result = await process_domain_specific_text(request.domain, request.text)
    if "error" in result:
        raise HTTPException(status_code=500, detail=result)
    return result

@app.get("/", tags=["Health Check"])
def root():
    return {"message": "Multi-Domain NLP Service is running."}