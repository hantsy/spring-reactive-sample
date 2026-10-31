CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE post_status AS ENUM ('DRAFT', 'PENDING_MODERATION', 'PUBLISHED');

CREATE TABLE IF NOT EXISTS posts (
    id UUID DEFAULT uuid_generate_v4(),
    title VARCHAR(255),
    content VARCHAR(255),
    tags VARCHAR[] DEFAULT '{}', -- Default to an empty array
    status post_status default 'DRAFT',
    version INTEGER
    );

ALTER TABLE posts ADD CONSTRAINT posts_pk PRIMARY KEY (id);