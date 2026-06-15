"""
RAG Query Router
Endpoints for role-based question answering
"""
from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field
from typing import List, Dict, Optional
from ..auth import verify_jwt_token
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


class SourceDocument(BaseModel):
    content: str
    metadata: Dict


class QueryResponse(BaseModel):
    answer: str
    sources: List[SourceDocument]
    role: str
    filter_applied: bool


@router.post("/query", response_model=QueryResponse)
async def query_rag(
    request: QueryRequest,
    auth: dict = Depends(verify_jwt_token)
):
    """
    Execute RAG query with role-based document filtering
    
    Flow:
    1. Validate JWT and extract role
    2. Build metadata filter based on role
    3. Search vector database for relevant documents
    4. Generate answer using LLM with filtered context
    5. Return answer with source documents
    """
    try:
        # Validate role matches JWT
        if auth["role"] != request.role:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Role in request does not match authenticated user role"
            )
        
        # Execute RAG query
        result = rag_service.query(
            query=request.query,
            user_role=request.role,
            buyer_id=request.buyer_id,
            buyer_status=request.buyer_status
        )
        
        # Check for errors
        if "error" in result:
            logger.error(f"RAG query error: {result['error']}")
            # Still return the fallback answer
        
        return QueryResponse(
            answer=result["answer"],
            sources=[SourceDocument(**src) for src in result["sources"]],
            role=result.get("role", request.role),
            filter_applied=result.get("filter_applied", False)
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
