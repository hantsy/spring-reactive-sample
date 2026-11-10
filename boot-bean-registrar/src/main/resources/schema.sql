CREATE TABLE IF NOT EXISTS posts
(
    id      UUID          NOT NULL DEFAULT random_uuid() PRIMARY KEY,
    title   VARCHAR(255)  NOT NULL,
    content VARCHAR(1000) NOT NULL
);