"""
Configuration settings for AEMS RAG Service
Loads from environment variables with defaults
"""
from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """
    Application settings loaded from environment variables
    """
    
    # Database Configuration
    DATABASE_URL: str
    
    # Internal service authentication
    INTERNAL_SECRET: str

    # Google Generative AI Configuration
    GOOGLE_API_KEY: str
    GOOGLE_EMBEDDING_MODEL: str = "gemini-embedding-001"
    GOOGLE_CHAT_MODEL: str = "gemini-2.5-flash"
    
    # RAG Configuration
    CHUNK_SIZE: int = 500
    CHUNK_OVERLAP: int = 50
    SEARCH_TOP_K: int = 5
    
    # Service Configuration
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    
    # Optional: Logging
    LOG_LEVEL: str = "INFO"
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"

 
settings = Settings()
