CREATE TABLE Deposit (
    depositId CHAR(36) NOT NULL PRIMARY KEY,
    bagName TEXT,
    creationTimestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    depositorId VARCHAR(64) NOT NULL
);

CREATE TABLE State (
    stateId SERIAL NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    label VARCHAR(64) NOT NULL,
    description TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);

CREATE TYPE IdentifierSchema AS ENUM ('doi', 'urn', 'fedora', 'bag-store');

CREATE TABLE Identifier (
    identifierId SERIAL NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    identifierSchema IdentifierSchema NOT NULL,
    identifierValue VARCHAR(64) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    UNIQUE (depositId, identifierSchema)
);

CREATE TABLE Curation (
    curationId SERIAL NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    isNewVersion BOOLEAN NOT NULL,
    isRequired BOOLEAN NOT NULL,
    isPerformed BOOLEAN NOT NULL,
    datamanagerUserId VARCHAR(64) NOT NULL,
    datamanagerEmail TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);

CREATE TABLE Springfield (
    springfieldId SERIAL NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    domain VARCHAR(32) NOT NULL,
    "user" VARCHAR(32) NOT NULL,
    collection VARCHAR(32) NOT NULL,
    playmode VARCHAR(32) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);

CREATE TABLE SimpleProperties (
    propertyId SERIAL NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    key VARCHAR(64) NOT NULL,
    value VARCHAR(64) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);

GRANT INSERT, SELECT ON Deposit TO easy_deposit_properties;
GRANT INSERT, SELECT ON State TO easy_deposit_properties;
GRANT INSERT, SELECT ON Identifier TO easy_deposit_properties;
GRANT INSERT, SELECT ON Curation TO easy_deposit_properties;
GRANT INSERT, SELECT ON Springfield TO easy_deposit_properties;
GRANT INSERT, SELECT ON SimpleProperties TO easy_deposit_properties;
