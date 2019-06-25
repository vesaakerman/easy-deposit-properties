-- differences with src/main/assembly/dist/install/db-tables.sql in order to support HSQLDB syntax
-- * SERIAL --> INTEGER IDENTITY
-- * TEXT --> CLOB

CREATE TABLE Deposit (
    depositId CHAR(36) NOT NULL PRIMARY KEY,
    bagName CLOB NOT NULL,
    creationTimestamp TIME WITH TIME ZONE NOT NULL,
    depositorId VARCHAR(64) NOT NULL
);

CREATE TABLE DepositState (
    stateId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    label VARCHAR(64) NOT NULL,
    description CLOB NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);

CREATE TABLE Identifier (
    value VARCHAR(64) NOT NULL PRIMARY KEY
);

INSERT INTO Identifier (value)
VALUES ('doi'),
       ('urn'),
       ('fedora'),
       ('bag-store');

CREATE TABLE DepositIdentifier (
    identifierId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    identifier VARCHAR(64) NOT NULL,
    identifierValue VARCHAR(64) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    FOREIGN KEY (identifier) REFERENCES Identifier (value),
    UNIQUE (depositId, identifier)
);

CREATE TABLE Curation (
    curationId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    isNewVersion BOOLEAN NOT NULL,
    isRequired BOOLEAN NOT NULL,
    isPerformed BOOLEAN NOT NULL,
    datamanagerUserId VARCHAR(64) NOT NULL,
    datamanagerEmail CLOB NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);

CREATE TABLE DepositSpringfield (
    springfieldId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    domain VARCHAR(32) NOT NULL,
    user VARCHAR(32) NOT NULL,
    collection VARCHAR(32) NOT NULL,
    playmode VARCHAR(32) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);

CREATE TABLE SimpleProperties (
    propertyId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    key VARCHAR(64) NOT NULL,
    value VARCHAR(64) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);
