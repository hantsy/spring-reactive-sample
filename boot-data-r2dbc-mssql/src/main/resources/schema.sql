use tempdb;

IF OBJECT_ID(N'dbo.posts', N'U') IS NULL
BEGIN
    CREATE TABLE posts (
        id BIGINT NOT NULL IDENTITY(1,1) PRIMARY KEY,
        title VARCHAR (50) NOT NULL,
        content VARCHAR (50) NOT NULL,
        createdAt DATETIME,
        updatedAt DATETIME
    )
END;


