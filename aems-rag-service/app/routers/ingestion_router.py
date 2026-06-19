"""
Document Ingestion Router
Endpoints for Spring Boot to push business events
"""
from fastapi import APIRouter, Header, HTTPException, status
from pydantic import BaseModel, Field
from typing import List, Dict, Optional
from ..services.ingestion_service import ingestion_service
from ..auth import verify_internal
import logging

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/ingest", tags=["Ingestion"])


class DocumentRequest(BaseModel):
    content: str = Field(..., description="Text content to embed and store")
    metadata: Dict[str, str] = Field(
        ..., 
        description="Metadata tags (visibility, buyer_id, event_type, etc.)"
    )
    
    class Config:
        json_schema_extra = {
            "example": {
                "content": "Buyer John Doe submitted application from New York. Company: ACME Corp. Status: Pending approval.",
                "metadata": {
                    "visibility": "management",
                    "buyer_id": "123",
                    "event_type": "buyer_application",
                    "status": "pending"
                }
            }
        }


class BatchDocumentRequest(BaseModel):
    documents: List[DocumentRequest]


class IngestionResponse(BaseModel):
    status: str
    message: Optional[str] = None
    chunks_created: Optional[int] = None
    document_ids: Optional[List[str]] = None


class BatchIngestionResponse(BaseModel):
    status: str
    documents_ingested: Optional[int] = None
    total_chunks: Optional[int] = None


class DeleteRequest(BaseModel):
    metadata: Dict[str, str] = Field(
        ...,
        description="Metadata filter for documents to delete"
    )
    
    class Config:
        json_schema_extra = {
            "example": {
                "metadata": {
                    "buyer_id": "123",
                    "event_type": "buyer_application"
                }
            }
        }


@router.post("/document", response_model=IngestionResponse)
async def ingest_document(
    request: DocumentRequest,
    x_internal_secret: str | None = Header(None),
):
    """
    Ingest a single business event document into the RAG vector database
    
    Called by Spring Boot services when business events occur:
    - Buyer submits application
    - Manager approves buyer
    - Employee creates stock
    - Buyer places order
    - Admin updates order status
    """
    try:
        verify_internal(x_internal_secret)
        result = ingestion_service.ingest_document(
            content=request.content,
            metadata=request.metadata
        )
        
        if result["status"] == "error":
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=result.get("message", "Failed to ingest document")
            )
        
        return IngestionResponse(**result)
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Ingestion endpoint error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


@router.post("/batch", response_model=BatchIngestionResponse)
async def ingest_batch(
    request: BatchDocumentRequest,
    x_internal_secret: str | None = Header(None),
):
    """
    Batch ingest multiple documents
    
    Useful for:
    - Initial data migration
    - Bulk operations
    - Periodic sync jobs
    """
    try:
        verify_internal(x_internal_secret)
        documents = [
            {"content": doc.content, "metadata": doc.metadata}
            for doc in request.documents
        ]
        
        result = ingestion_service.ingest_batch(documents)
        
        if result["status"] == "error":
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=result.get("message", "Failed to batch ingest")
            )
        
        return BatchIngestionResponse(**result)
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Batch ingestion error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


@router.delete("/delete")
async def delete_documents(
    request: DeleteRequest,
    x_internal_secret: str | None = Header(None),
):
    """
    Delete documents matching metadata filter
    
    Use cases:
    - Buyer account deleted → remove buyer's documents
    - Order cancelled → remove order event records
    - Data cleanup
    """
    try:
        verify_internal(x_internal_secret)
        result = ingestion_service.delete_by_metadata(
            metadata_filter=request.metadata
        )
        
        if result["status"] == "error":
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=result.get("message", "Failed to delete documents")
            )
        
        return {
            "status": "success",
            "deleted_count": result.get("deleted_count", 0),
            "filter": request.metadata
        }
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Delete endpoint error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


@router.get("/health")
async def ingestion_health():
    """Health check for ingestion service"""
    return {
        "status": "healthy",
        "service": "ingestion",
        "embedding_model": "gemini-embedding-001"
    }
