-- Add embedding column to tags table using pgvector type
-- The vector size 1536 matches OpenAI text-embedding-3-* models
ALTER TABLE tags
    ADD COLUMN IF NOT EXISTS embedding vector(1536);
