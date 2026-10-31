CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS posts (
    id UUID NOT NULL/* [jooq ignore start] */ DEFAULT  uuid_generate_v4()/* [jooq ignore stop] */,
    title VARCHAR(255),
    content VARCHAR(255),
    status VARCHAR(255) default 'DRAFT',
    created_at TIMESTAMP , --NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at TIMESTAMP , --NOT NULL DEFAULT LOCALTIMESTAMP,
    version INTEGER
);

CREATE TABLE IF NOT EXISTS comments (
    id UUID  NOT NULL /* [jooq ignore start] */DEFAULT uuid_generate_v4()/* [jooq ignore stop] */,
    content VARCHAR(255),
    created_at TIMESTAMP , --NOT NULL DEFAULT LOCALTIMESTAMP,
    post_id UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS hash_tags (
    id UUID NOT NULL /* [jooq ignore start] */DEFAULT uuid_generate_v4()/* [jooq ignore stop] */,
    name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS posts_tags (
    post_id UUID NOT NULL,
    tag_id UUID NOT NULL
);

-- drop constraints
ALTER TABLE posts_tags DROP CONSTRAINT IF EXISTS fk_posts_tags_to_hash_tags;
ALTER TABLE posts_tags DROP CONSTRAINT IF EXISTS fk_posts_tags_to_posts;
ALTER TABLE hash_tags DROP CONSTRAINT IF EXISTS pk_hash_tags;
ALTER TABLE comments DROP CONSTRAINT IF EXISTS fk_comments_to_posts;
ALTER TABLE comments DROP CONSTRAINT IF EXISTS pk_comments;
ALTER TABLE posts DROP CONSTRAINT IF EXISTS pk_posts;

-- add constraints
ALTER TABLE posts ADD CONSTRAINT  pk_posts PRIMARY KEY (id);
ALTER TABLE comments ADD CONSTRAINT  pk_comments PRIMARY KEY (id);
ALTER TABLE comments ADD CONSTRAINT  fk_comments_to_posts FOREIGN KEY (post_id) REFERENCES posts (id) /* [jooq ignore start] */ ON DELETE CASCADE/* [jooq ignore stop] */;
ALTER TABLE hash_tags ADD CONSTRAINT  pk_hash_tags PRIMARY KEY (id);
ALTER TABLE posts_tags ADD CONSTRAINT  fk_posts_tags_to_posts FOREIGN KEY (post_id) REFERENCES posts (id) /* [jooq ignore start] */ MATCH FULL/* [jooq ignore stop] */;
ALTER TABLE posts_tags ADD CONSTRAINT  fk_posts_tags_to_hash_tags FOREIGN KEY (tag_id) REFERENCES hash_tags (id) /* [jooq ignore start] */ MATCH FULL/* [jooq ignore stop] */;