"""
RAG Query Router
Endpoints for role-based question answering
"""
from fastapi import APIRouter, Header, HTTPException, status
from pydantic import BaseModel, Field
from typing import List, Optional
from ..auth import verify_internal
from ..services.rag_service import rag_service
import logging

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/rag", tags=["RAG Query"])


class QueryRequest(BaseModel):
    query: str = Field(..., description="User question")
    role: str = Field(..., description="User role (BUYER, EMPLOYEE, MANAGER, ADMIN, SUPER_ADMIN)")
    buyer_id: Optional[str] = Field(None, description="Buyer ID (required if role is BUYER)")
    buyer_status: Optional[str] = Field(None, description="Buyer status: PENDING or ACCEPTED")
    
    class Config:
        json_schema_extra = {
            "example": {
                "query": "What products are available in stock?",
                "role": "BUYER",
                "buyer_id": "123",
                "buyer_status": "ACCEPTED"
            }
        }


class QueryResponse(BaseModel):
    answer: str
    sources: List[str]


@router.post("/query", response_model=QueryResponse)
async def query_rag(
    request: QueryRequest,
    x_internal_secret: str | None = Header(None)
):
    """
    Execute RAG query with role-based document filtering
    
    Flow:
    1. Verify internal service secret
    2. Build metadata filter based on role
    3. Search vector database for relevant documents
    4. Generate answer using LLM with filtered context
    5. Return answer with source documents
    """
    try:
        verify_internal(x_internal_secret)
        
        # Execute RAG query
        result = rag_service.query(
            query=request.query,
            user_role=request.role,
            buyer_id=request.buyer_id,
            buyer_status=request.buyer_status
        )
        
        return QueryResponse(
            answer=result["answer"],
            sources=result["sources"],
        )
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Query endpoint error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to process query: {str(e)}"
        )


@router.get("/health")
async def rag_health():
    """Health check for RAG query service"""
    return {
        "status": "healthy",
        "service": "rag_query",
        "llm_model": "gpt-4-turbo-preview"
    }
