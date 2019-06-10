-- differences with src/main/assembly/dist/install/db-tables.sql in order to support HSQLDB syntax
-- * SMALLSERIAL --> INTEGER IDENTITY
-- * TEXT --> CLOB

CREATE TABLE Deposit (
    depositId CHAR(36) NOT NULL PRIMARY KEY,
    bagName CLOB NOT NULL,
    creationTimestamp TIME WITH TIME ZONE NOT NULL,
    depositorId VARCHAR(64) NOT NULL
);

CREATE TABLE State (
    value VARCHAR(64) NOT NULL PRIMARY KEY
);

INSERT INTO State (value)
VALUES ('DRAFT'),
       ('UPLOADED'),
       ('FINALIZING'),
       ('INVALID'),
       ('SUBMITTED'),
       ('REJECTED'),
       ('FAILED'),
       ('IN_REVIEW'),
       ('ARCHIVED'),
       ('FEDORA_ARCHIVED');

CREATE TABLE DepositState (
    stateId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    state VARCHAR(64) NOT NULL,
    description CLOB NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    FOREIGN KEY (state) REFERENCES State (value)
);

CREATE TABLE IngestStep (
    value VARCHAR(64) NOT NULL PRIMARY KEY
);

INSERT INTO IngestStep (value)
VALUES ('VALIDATE'),
       ('PID_GENERATOR'),
       ('FEDORA'),
       ('SPRINGFIELD'),
       ('BAGSTORE'),
       ('BAGINDEX'),
       ('SOLR4FILES');

CREATE TABLE DepositIngestStep (
    stepId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    step VARCHAR(64) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    FOREIGN KEY (step) REFERENCES IngestStep (value)
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
    FOREIGN KEY (identifier) REFERENCES Identifier (value)
--    TODO add uniqueness constraint on (depositId, identifier)
);

CREATE TABLE DoiEventType (
    value VARCHAR(64) NOT NULL PRIMARY KEY
);

INSERT INTO DoiEventType (value)
VALUES ('registered'),
       ('action');

CREATE TABLE DepositDoiEvent (
    doiEventId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    doiEvent VARCHAR(64) NOT NULL,
    value VARCHAR(64) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    FOREIGN KEY (doiEvent) REFERENCES DoiEventType (value)
);

CREATE TABLE DepositCurator (
    curatorId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    datamanagerUserId VARCHAR(64) NOT NULL,
    datamanagerEmail CLOB NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);

CREATE TABLE CurationType (
    value VARCHAR(64) NOT NULL PRIMARY KEY
);

INSERT INTO CurationType (value)
VALUES ('is-new-version'),
       ('curation-required'),
       ('curation-performed');

CREATE TABLE DepositCurationEvent (
    curationEventId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    curationType VARCHAR(64) NOT NULL,
    value VARCHAR(64) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    FOREIGN KEY (curationType) REFERENCES CurationType (value)
);

CREATE TABLE SpringfieldPlayMode (
    value VARCHAR(32) NOT NULL PRIMARY KEY
);

INSERT INTO SpringfieldPlayMode (value)
VALUES ('continuous'),
       ('menu');

CREATE TABLE DepositSpringfield (
    springfieldId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    domain VARCHAR(32) NOT NULL,
    user VARCHAR(32) NOT NULL,
    collection VARCHAR(32) NOT NULL,
    playmode VARCHAR(32) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    FOREIGN KEY (playmode) REFERENCES SpringfieldPlayMode (value)
);

CREATE TABLE DepositClientMessageContentType (
    clientMessageContentTypeId INTEGER IDENTITY NOT NULL PRIMARY KEY,
    depositId CHAR(36) NOT NULL,
    contentType VARCHAR(64) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId)
);
