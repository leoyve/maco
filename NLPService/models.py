# models.py
from pydantic import BaseModel
from typing import Optional, Dict, Any

# --- Models for the /router endpoint ---
class RouterRequest(BaseModel):
    text: str

class RouterResponse(BaseModel):
    domain: str

# --- Models for the /process endpoint ---
class ProcessRequest(BaseModel):
    domain: str
    text: str

# ProcessResponse can be any valid dictionary, so we don't need a strict model here.
# We can reuse ErrorResponse from before for error handling.
class ErrorResponse(BaseModel):
    error: str
    raw_response: Optional[str] = None