CREATE SEQUENCE table_name_id_seq;

CREATE TABLE IF NOT EXISTS posts(
id integer NOT NULL PRIMARY KEY DEFAULT nextval('table_name_id_seq') ,
title VARCHAR(255) NOT NULL,
content VARCHAR(255) NOT NULL
);

ALTER SEQUENCE table_name_id_seq OWNED BY posts.id;
