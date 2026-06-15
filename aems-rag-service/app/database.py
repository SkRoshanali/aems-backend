"""
Database connection utilities
"""
from .config import settings


def get_connection_string() -> str:
    """
    Get PostgreSQL connection string for pgvector
    
    Returns:
        PostgreSQL connection URL
    """
    return settings.DATABASE_URL
