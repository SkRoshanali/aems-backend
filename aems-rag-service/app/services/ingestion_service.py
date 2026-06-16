"""
Document Ingestion Service
Handles embedding generation and storage for business events
"""
from typing import List, Dict, Optional
from langchain_google_genai import GoogleGenerativeAIEmbeddings
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.vectorstores import PGVector
from sqlalchemy import create_engine, text
from ..config import settings
from ..database import get_connection_string
import logging

logger = logging.getLogger(__name__)


class IngestionService:
    
    def __init__(self):
        self.embeddings = GoogleGenerativeAIEmbeddings(
            model=settings.GOOGLE_EMBEDDING_MODEL,
            google_api_key=settings.GOOGLE_API_KEY
        )
        
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=settings.CHUNK_SIZE,
            chunk_overlap=settings.CHUNK_OVERLAP,
            separators=["\n\n", "\n", ". ", ", ", " ", ""]
        )
        
        self.vector_store = PGVector(
            connection_string=get_connection_string(),
            embedding_function=self.embeddings,
            collection_name="document_chunks"
        )
    
    def ingest_document(
        self, 
        content: str, 
        metadata: Dict[str, str]
    ) -> Dict:
        """
        Ingest a single document (business event) into vector database
        
        Args:
            content: Text content to embed
            metadata: Tags for filtering (visibility, buyer_id, event_type, etc.)
        
        Returns:
            Status dict with document_id and chunks_created
        """
        try:
            # Split content into chunks if needed
            chunks = self.text_splitter.split_text(content)
            
            # Add metadata to each chunk
            metadatas = [metadata.copy() for _ in chunks]
            
            # Add chunks to vector store
            ids = self.vector_store.add_texts(
                texts=chunks,
                metadatas=metadatas
            )
            
            logger.info(
                f"Ingested document: {metadata.get('event_type', 'unknown')} "
                f"with {len(chunks)} chunks"
            )
            
            return {
                "status": "success",
                "chunks_created": len(chunks),
                "document_ids": ids
            }
            
        except Exception as e:
            logger.error(f"Failed to ingest document: {str(e)}")
            return {
                "status": "error",
                "message": str(e)
            }
    
    def ingest_batch(
        self,
        documents: List[Dict]
    ) -> Dict:
        """
        Batch ingest multiple documents
        
        Args:
            documents: List of dicts with 'content' and 'metadata' keys
        
        Returns:
            Status dict with total_ingested count
        """
        try:
            all_texts = []
            all_metadatas = []
            
            for doc in documents:
                content = doc.get("content", "")
                metadata = doc.get("metadata", {})
                
                chunks = self.text_splitter.split_text(content)
                all_texts.extend(chunks)
                all_metadatas.extend([metadata.copy() for _ in chunks])
            
            ids = self.vector_store.add_texts(
                texts=all_texts,
                metadatas=all_metadatas
            )
            
            logger.info(f"Batch ingested {len(documents)} documents ({len(ids)} chunks)")
            
            return {
                "status": "success",
                "documents_ingested": len(documents),
                "total_chunks": len(ids)
            }
            
        except Exception as e:
            logger.error(f"Failed to batch ingest: {str(e)}")
            return {
                "status": "error",
                "message": str(e)
            }
    
    def delete_by_metadata(
        self,
        metadata_filter: Dict[str, str]
    ) -> Dict:
        """
        Delete documents matching metadata filter
        Useful when buyer is deleted or order is cancelled
        
        Args:
            metadata_filter: Dict of metadata key-value pairs to match
        
        Returns:
            Status dict with deleted_count
        """
        try:
            # Note: PGVector delete by filter might need custom SQL
            # This is a simplified version
            engine = create_engine(get_connection_string())
            
            # Build WHERE clause from metadata filter
            conditions = []
            for key, value in metadata_filter.items():
                conditions.append(f"metadata->'{key}' = '{value}'")
            
            where_clause = " AND ".join(conditions)
            
            delete_query = text(f"""
                DELETE FROM langchain_pg_embedding
                WHERE {where_clause}
            """)
            
            with engine.connect() as conn:
                result = conn.execute(delete_query)
                conn.commit()
                deleted_count = result.rowcount
            
            logger.info(f"Deleted {deleted_count} documents matching {metadata_filter}")
            
            return {
                "status": "success",
                "deleted_count": deleted_count
            }
            
        except Exception as e:
            logger.error(f"Failed to delete documents: {str(e)}")
            return {
                "status": "error",
                "message": str(e)
            }


# Singleton instance
ingestion_service = IngestionService()
