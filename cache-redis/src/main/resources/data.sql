-- initialize the sample data.
DELETE FROM posts;
INSERT INTO  posts (title, content)
VALUES ('Cache is supported in WebFlux stack', 'Since Spring 6.1, Cache supports WebFlux stack');
