-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Direct RAG table used by the Gemini/Vercel implementation
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

-- LangChain creates its own tables, but we can pre-create for control
-- This table will store document chunks with embeddings
CREATE TABLE IF NOT EXISTS langchain_pg_embedding (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_id UUID,
    embedding vector(1536),  -- OpenAI ada/text-embedding-3-small dimension
    document TEXT,
    cmetadata JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Create index for fast vector similarity search
CREATE INDEX IF NOT EXISTS langchain_pg_embedding_embedding_idx 
ON langchain_pg_embedding USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- Index on metadata for role-based filtering
CREATE INDEX IF NOT EXISTS langchain_pg_embedding_metadata_idx 
ON langchain_pg_embedding USING GIN(cmetadata);

-- Collection table (LangChain requirement)
CREATE TABLE IF NOT EXISTS langchain_pg_collection (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL,
    cmetadata JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Insert default collection
INSERT INTO langchain_pg_collection (name, cmetadata)
VALUES ('document_chunks', '{"description": "AEMS business event documents"}')
ON CONFLICT (name) DO NOTHING;

-- Query history table (optional - for analytics)
CREATE TABLE IF NOT EXISTS query_history (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255),
    user_role VARCHAR(50),
    query TEXT NOT NULL,
    answer TEXT,
    sources_count INT,
    filter_applied BOOLEAN,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS query_history_user_idx ON query_history(user_email);
CREATE INDEX IF NOT EXISTS query_history_created_idx ON query_history(created_at DESC);
