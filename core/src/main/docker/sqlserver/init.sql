CREATE DATABASE TEST;
GO

USE DATABASE TEST;
GO

CREATE LOGIN test WITH PASSWORD = 'test';  
GO  

CREATE USER test FOR LOGIN test;  
GO  