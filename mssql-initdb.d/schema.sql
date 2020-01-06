CREATE TABLE IF NOT EXISTS posts (
    id INT PRIMARY KEY IDENTITY (1, 1),
    title VARCHAR (50) NOT NULL,
    content VARCHAR (50) NOT NULL,
    createdAt DATETIME,
    updatedAt DATETIME
);
