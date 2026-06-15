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
    
    # OpenAI Configuration
    OPENAI_API_KEY: str
    OPENAI_EMBEDDING_MODEL: str = "text-embedding-3-small"
    OPENAI_CHAT_MODEL: str = "gpt-4-turbo-preview"
    
    # JWT Configuration (must match Spring Boot)
    JWT_SECRET: str
    JWT_ALGORITHM: str = "HS256"
    
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
