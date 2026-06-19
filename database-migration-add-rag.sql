-- ============================================================
-- RAG Extension for Existing AEMS Database
-- Run this AFTER your existing schema
-- ============================================================

-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================================
-- TABLE: document_chunks
-- Direct Gemini + pgvector RAG storage
-- ============================================================
CREATE TABLE IF NOT EXISTS document_chunks (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    embedding vector(768) NOT NULL,
    visibility TEXT NOT NULL DEFAULT 'public',
    buyer_id TEXT,
    event_type TEXT,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS document_chunks_embedding_hnsw_idx
ON document_chunks USING hnsw (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS document_chunks_visibility_idx
ON document_chunks (visibility);

CREATE INDEX IF NOT EXISTS document_chunks_metadata_gin_idx
ON document_chunks USING GIN(metadata);

-- ============================================================
-- TABLE: langchain_pg_collection
-- Stores collection metadata for LangChain
-- ============================================================
CREATE TABLE IF NOT EXISTS langchain_pg_collection (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL,
    cmetadata JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Insert default collection for AEMS documents
INSERT INTO langchain_pg_collection (name, cmetadata)
VALUES ('document_chunks', '{"description": "AEMS business event documents", "system": "aems-rag"}')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- TABLE: langchain_pg_embedding
-- Stores document chunks with vector embeddings
-- ============================================================
CREATE TABLE IF NOT EXISTS langchain_pg_embedding (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_id UUID REFERENCES langchain_pg_collection(id) ON DELETE CASCADE,
    embedding vector(1536),  -- OpenAI text-embedding-3-small dimension
    document TEXT NOT NULL,
    cmetadata JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- INDEXES for Fast Vector Search and Filtering
-- ============================================================

-- Vector similarity search index (using IVFFlat)
-- Lists parameter: Use sqrt(total_rows) for optimal performance
-- Start with 100, increase as data grows
CREATE INDEX IF NOT EXISTS langchain_pg_embedding_embedding_idx 
ON langchain_pg_embedding USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- Metadata filtering index (for role-based access)
CREATE INDEX IF NOT EXISTS langchain_pg_embedding_metadata_idx 
ON langchain_pg_embedding USING GIN(cmetadata);

-- Collection index
CREATE INDEX IF NOT EXISTS langchain_pg_embedding_collection_idx 
ON langchain_pg_embedding(collection_id);

-- ============================================================
-- TABLE: query_history (Optional - for analytics)
-- Track RAG queries for monitoring and improvement
-- ============================================================
CREATE TABLE IF NOT EXISTS query_history (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255),
    user_role VARCHAR(50),
    query TEXT NOT NULL,
    answer TEXT,
    sources_count INT DEFAULT 0,
    filter_applied BOOLEAN DEFAULT FALSE,
    response_time_ms INT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS query_history_user_email_idx ON query_history(user_email);
CREATE INDEX IF NOT EXISTS query_history_user_role_idx ON query_history(user_role);
CREATE INDEX IF NOT EXISTS query_history_created_at_idx ON query_history(created_at DESC);

-- ============================================================
-- VERIFY INSTALLATION
-- ============================================================

-- Check if pgvector is installed
SELECT * FROM pg_extension WHERE extname='vector';

-- Check collections
SELECT * FROM langchain_pg_collection;

-- Count embeddings (should be 0 initially)
SELECT COUNT(*) as embedding_count FROM langchain_pg_embedding;

-- ============================================================
-- NOTES:
-- 1. Run this script on your Neon PostgreSQL database
-- 2. The vector dimension (1536) matches OpenAI text-embedding-3-small
-- 3. If you switch to a different embedding model, update the dimension
-- 4. IVFFlat index improves search speed but requires periodic rebuilding
-- ============================================================
