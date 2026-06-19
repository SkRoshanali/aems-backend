"""
Pydantic models for request/response validation
"""
from pydantic import BaseModel, Field
from typing import List, Dict, Optional


class DocumentRequest(BaseModel):
    content: str = Field(..., description="Text content to embed and store")
    metadata: Dict[str, str] = Field(
        ..., 
        description="Metadata tags (visibility, buyer_id, event_type, etc.)"
    )


class QueryRequest(BaseModel):
    query: str = Field(..., description="User question")
    role: str = Field(..., description="User role")
    buyer_id: Optional[str] = Field(None, description="Buyer ID if role is BUYER")
    buyer_status: Optional[str] = Field(None, description="PENDING or ACCEPTED")


class QueryResponse(BaseModel):
    answer: str
    sources: List[str]
