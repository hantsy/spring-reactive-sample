-- initialize the sample data.
DELETE FROM posts;
INSERT INTO posts (title, content) VALUES ('R2dbc is refined', 'R2dbc is now part of Spring framework core');
INSERT INTO posts (title, content) VALUES ('Spring and R2dbc', 'content of my Spring reactive post');