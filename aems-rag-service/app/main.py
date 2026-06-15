"""
AEMS RAG Service - FastAPI Application
Role-Based Retrieval-Augmented Generation for Agri Export Management System
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .routers import rag_router, ingestion_router
from .config import settings
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

app = FastAPI(
    title="AEMS RAG Service",
    description="Role-Based Retrieval-Augmented Generation Service for Agri Export Management System",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure based on your needs
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(rag_router.router)
app.include_router(ingestion_router.router)


@app.get("/")
async def root():
    return {
        "service": "AEMS RAG Service",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "query": "/api/rag/query",
            "ingest": "/api/ingest/document",
            "batch_ingest": "/api/ingest/batch",
            "delete": "/api/ingest/delete",
            "docs": "/docs"
        }
    }


@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "service": "aems-rag-service",
        "components": {
            "rag_query": "operational",
            "ingestion": "operational",
            "vector_db": "connected"
        }
    }


if __name__ == "__main__":
    import uvicorn
    logger.info(f"Starting AEMS RAG Service on {settings.HOST}:{settings.PORT}")
    uvicorn.run(
        "app.main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=True
    )
