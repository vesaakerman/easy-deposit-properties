-- differences with src/main/assembly/dist/install/db-tables.sql in order to support HSQLDB syntax
-- * SERIAL --> INTEGER IDENTITY
-- * TEXT --> CLOB
-- * IdentifierSchema ENUM --> table with foreign key constraint

CREATE TABLE Deposit (
    depositId CHAR(36) NOT NULL PRIMARY KEY,
    bagName CLOB,
    creationTimestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    depositorId VARCHAR(64) NOT NULL
);

CREATE TABLE State (
    stateId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    label VARCHAR(64) NOT NULL,
    description CLOB NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);

CREATE TABLE IdentifierSchema (
    value VARCHAR(64) NOT NULL PRIMARY KEY
);

INSERT INTO IdentifierSchema (value)
VALUES ('doi'),
       ('urn'),
       ('fedora'),
       ('bag-store');

CREATE TABLE Identifier (
    identifierId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    identifierSchema VARCHAR(64) NOT NULL,
    identifierValue VARCHAR(64) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    FOREIGN KEY (identifierSchema) REFERENCES IdentifierSchema (value),
    UNIQUE (depositId, identifierSchema)
);

CREATE TABLE Curation (
    curationId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    isNewVersion BOOLEAN NOT NULL,
    isRequired BOOLEAN NOT NULL,
    isPerformed BOOLEAN NOT NULL,
    datamanagerUserId VARCHAR(64) NOT NULL,
    datamanagerEmail CLOB NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);

CREATE TABLE DepositSpringfield (
    springfieldId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    domain VARCHAR(32) NOT NULL,
    user VARCHAR(32) NOT NULL,
    collection VARCHAR(32) NOT NULL,
    playmode VARCHAR(32) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);

CREATE TABLE SimpleProperties (
    propertyId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    key VARCHAR(64) NOT NULL,
    value VARCHAR(64) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);
