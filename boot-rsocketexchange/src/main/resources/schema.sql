CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- create posts table
CREATE TABLE IF NOT EXISTS posts (
 id UUID DEFAULT uuid_generate_v4(),
 title VARCHAR(255),
 content VARCHAR(255),
 status VARCHAR(20) default 'DRAFT',
 created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
 PRIMARY KEY (id)
 );