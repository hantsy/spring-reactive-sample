CREATE KEYSPACE IF NOT EXISTS demo WITH replication = {'class':'SimpleStrategy','replication_factor':'1'};
USE demo;
CREATE TABLE IF NOT EXISTS products(id VARCHAR PRIMARY KEY, name VARCHAR, price DECIMAL);