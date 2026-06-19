"""
RAG query service using direct Gemini calls and pgvector SQL.
"""
from typing import Dict, List, Optional, Tuple

import logging
import psycopg2
from fastapi import HTTPException, status
from google import genai

from ..config import settings
from ..database import get_connection_string
from .ingestion_service import embed_text, vector_literal

logger = logging.getLogger(__name__)

client = genai.Client(api_key=settings.GOOGLE_API_KEY)


def build_filter(
    role: str,
    buyer_status: Optional[str] = None,
    buyer_id: Optional[str] = None,
) -> Tuple[str, List[str]]:
    trusted_role = role.upper()

    if trusted_role == "BUYER":
        if buyer_status == "ACCEPTED" and buyer_id:
            return (
                "(visibility = 'public' OR buyer_id = %s OR visibility = %s)",
                [buyer_id, f"buyer:{buyer_id}"],
            )
        return "visibility = 'public'", []

    if trusted_role == "EMPLOYEE":
        return "visibility IN ('public', 'internal')", []

    if trusted_role == "MANAGER":
        return "visibility IN ('public', 'internal', 'management')", []

    if trusted_role in {"ADMIN", "SUPER_ADMIN"}:
        return "TRUE", []

    return "visibility = 'public'", []


class RAGService:
    def connect(self):
        return psycopg2.connect(get_connection_string())

    def search_chunks(
        self,
        query: str,
        user_role: str,
        buyer_id: Optional[str],
        buyer_status: Optional[str],
    ) -> List[str]:
        embedding = embed_text(query)
        where_clause, filter_params = build_filter(user_role, buyer_status, buyer_id)

        sql = f"""
            SELECT content
            FROM document_chunks
            WHERE {where_clause}
            ORDER BY embedding <=> %s::vector
            LIMIT 4
        """

        with self.connect() as conn:
            with conn.cursor() as cur:
                cur.execute(sql, [*filter_params, vector_literal(embedding)])
                return [row[0] for row in cur.fetchall()]

    def generate_answer(self, query: str, chunks: List[str]) -> str:
        context = "\n\n---\n\n".join(chunks) if chunks else "No matching context was found."
        prompt = f"""You are the AEMS assistant. Answer using only the context below.
If the context does not contain the answer, say you do not have that information.

Context:
{context}

Question:
{query}

Answer:"""

        try:
            response = client.models.generate_content(
                model=settings.GOOGLE_CHAT_MODEL,
                contents=prompt,
            )
        except Exception as exc:
            logger.error("Gemini generation failed: %s", exc)
            raise HTTPException(
                status_code=status.HTTP_502_BAD_GATEWAY,
                detail="AI service temporarily unavailable",
            ) from exc

        return response.text or "I do not have enough information to answer that."

    def query(
        self,
        query: str,
        user_role: str,
        buyer_id: Optional[str] = None,
        buyer_status: Optional[str] = None,
    ) -> Dict:
        chunks = self.search_chunks(query, user_role, buyer_id, buyer_status)
        answer = self.generate_answer(query, chunks)

        logger.info(
            "RAG query executed for role %s with %s source chunk(s)",
            user_role,
            len(chunks),
        )

        return {
            "answer": answer,
            "sources": chunks,
        }


rag_service = RAGService()
