"""
Document ingestion using Gemini embeddings and pgvector.
"""
from typing import Dict, List

import logging
import psycopg2
from psycopg2.extras import Json
from google import genai
from google.genai import types

from ..config import settings
from ..database import get_connection_string

logger = logging.getLogger(__name__)

client = genai.Client(api_key=settings.GOOGLE_API_KEY)


def vector_literal(values: List[float]) -> str:
    return "[" + ",".join(str(value) for value in values) + "]"


def embed_text(text: str) -> List[float]:
    result = client.models.embed_content(
        model=settings.GOOGLE_EMBEDDING_MODEL,
        contents=text,
        config=types.EmbedContentConfig(output_dimensionality=768),
    )
    return list(result.embeddings[0].values)


def chunk_text(content: str, chunk_size: int, overlap: int) -> List[str]:
    text = content.strip()
    if not text:
        return []
    if len(text) <= chunk_size:
        return [text]

    chunks: List[str] = []
    start = 0
    step = max(chunk_size - overlap, 1)
    while start < len(text):
        chunks.append(text[start:start + chunk_size])
        start += step
    return chunks


class IngestionService:
    def __init__(self):
        self.ensure_schema()

    def connect(self):
        return psycopg2.connect(get_connection_string())

    def ensure_schema(self) -> None:
        with self.connect() as conn:
            with conn.cursor() as cur:
                cur.execute("CREATE EXTENSION IF NOT EXISTS vector")
                cur.execute(
                    """
                    CREATE TABLE IF NOT EXISTS document_chunks (
                        id BIGSERIAL PRIMARY KEY,
                        content TEXT NOT NULL,
                        embedding vector(768) NOT NULL,
                        visibility TEXT NOT NULL DEFAULT 'public',
                        buyer_id TEXT,
                        event_type TEXT,
                        metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                    )
                    """
                )
                cur.execute(
                    """
                    CREATE INDEX IF NOT EXISTS document_chunks_embedding_hnsw_idx
                    ON document_chunks USING hnsw (embedding vector_cosine_ops)
                    """
                )
                cur.execute(
                    """
                    CREATE INDEX IF NOT EXISTS document_chunks_visibility_idx
                    ON document_chunks (visibility)
                    """
                )
                cur.execute(
                    """
                    CREATE INDEX IF NOT EXISTS document_chunks_metadata_gin_idx
                    ON document_chunks USING GIN (metadata)
                    """
                )
            conn.commit()

    def ingest_document(self, content: str, metadata: Dict[str, str]) -> Dict:
        try:
            chunks = chunk_text(content, settings.CHUNK_SIZE, settings.CHUNK_OVERLAP)
            if not chunks:
                return {
                    "status": "error",
                    "message": "Content cannot be empty",
                }

            document_ids: List[int] = []
            with self.connect() as conn:
                with conn.cursor() as cur:
                    for chunk in chunks:
                        embedding = embed_text(chunk)
                        cur.execute(
                            """
                            INSERT INTO document_chunks
                                (content, embedding, visibility, buyer_id, event_type, metadata)
                            VALUES
                                (%s, %s::vector, %s, %s, %s, %s)
                            RETURNING id
                            """,
                            (
                                chunk,
                                vector_literal(embedding),
                                metadata.get("visibility", "public"),
                                metadata.get("buyer_id"),
                                metadata.get("event_type"),
                                Json(metadata),
                            ),
                        )
                        document_ids.append(cur.fetchone()[0])
                conn.commit()

            logger.info(
                "Ingested %s chunk(s) for event %s",
                len(document_ids),
                metadata.get("event_type", "unknown"),
            )
            return {
                "status": "success",
                "chunks_created": len(document_ids),
                "document_ids": [str(document_id) for document_id in document_ids],
            }
        except Exception as exc:
            logger.error("Failed to ingest document: %s", exc)
            return {
                "status": "error",
                "message": str(exc),
            }

    def ingest_batch(self, documents: List[Dict]) -> Dict:
        total_chunks = 0
        for document in documents:
            result = self.ingest_document(
                document.get("content", ""),
                document.get("metadata", {}),
            )
            if result["status"] == "error":
                return result
            total_chunks += result.get("chunks_created", 0)

        return {
            "status": "success",
            "documents_ingested": len(documents),
            "total_chunks": total_chunks,
        }

    def delete_by_metadata(self, metadata_filter: Dict[str, str]) -> Dict:
        try:
            with self.connect() as conn:
                with conn.cursor() as cur:
                    cur.execute(
                        """
                        DELETE FROM document_chunks
                        WHERE metadata @> %s::jsonb
                        """,
                        (Json(metadata_filter),),
                    )
                    deleted_count = cur.rowcount
                conn.commit()

            return {
                "status": "success",
                "deleted_count": deleted_count,
            }
        except Exception as exc:
            logger.error("Failed to delete documents: %s", exc)
            return {
                "status": "error",
                "message": str(exc),
            }


ingestion_service = IngestionService()
