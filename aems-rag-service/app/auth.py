"""
Internal service authentication for the RAG API.
"""
from fastapi import HTTPException, status

from .config import settings


def verify_internal(x_internal_secret: str | None) -> None:
    """
    Accept only calls that know the shared internal secret.

    FastAPI maps the X-Internal-Secret header to an x_internal_secret
    function parameter when the route declares Header(None).
    """
    if x_internal_secret != settings.INTERNAL_SECRET:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Forbidden",
        )
