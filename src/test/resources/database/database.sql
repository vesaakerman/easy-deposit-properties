-- differences with src/main/assembly/dist/install/db-tables.sql in order to support HSQLDB syntax
-- * SMALLSERIAL --> INTEGER IDENTITY
-- * TEXT --> CLOB 

CREATE TABLE Deposit (
    depositId CHAR(36) NOT NULL PRIMARY KEY,
    creationTimestamp TIME WITH TIME ZONE NOT NULL,
    depositorId VARCHAR(64) NOT NULL,
);

CREATE TABLE State (
    stateId INTEGER IDENTITY PRIMARY KEY,
    value VARCHAR(64) NOT NULL,
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
    depositId CHAR(36) NOT NULL,
    stateId INTEGER IDENTITY NOT NULL,
    description CLOB NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    FOREIGN KEY (stateId) REFERENCES State (stateId),
);

CREATE TABLE IngestStep (
    stepId INTEGER IDENTITY PRIMARY KEY,
    value VARCHAR(64) NOT NULL,
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
    depositId CHAR(36) NOT NULL,
    stepId INTEGER IDENTITY NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    FOREIGN KEY (stepId) REFERENCES IngestStep (stepId),
);

CREATE TABLE Identifier (
    identifierId INTEGER IDENTITY PRIMARY KEY,
    value VARCHAR(64) NOT NULL,
);

INSERT INTO Identifier (value)
VALUES ('doi'),
       ('urn'),
       ('fedora'),
       ('bag-store');

CREATE TABLE DepositIdentifier (
    depositId CHAR(36) NOT NULL,
    identifierId INTEGER IDENTITY NOT NULL,
    identifierValue VARCHAR(64) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    FOREIGN KEY (identifierId) REFERENCES Identifier (identifierId),
);

CREATE TABLE DoiEventType (
    doiEventId INTEGER IDENTITY PRIMARY KEY,
    value VARCHAR(64) NOT NULL,
);

INSERT INTO DoiEventType (value)
VALUES ('registered'),
       ('action');

CREATE TABLE DepositDoiEvent (
    depositId CHAR(36) NOT NULL,
    doiEventId INTEGER IDENTITY NOT NULL,
    value VARCHAR(64) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    FOREIGN KEY (doiEventId) REFERENCES DoiEventType (doiEventId),
);

CREATE TABLE DepositCurator (
    depositId CHAR(36) NOT NULL,
    datamanagerUserId VARCHAR(64) NOT NULL,
    datamanagerEmail CLOB NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
);

CREATE TABLE CurationType (
    curationId INTEGER IDENTITY PRIMARY KEY,
    value VARCHAR(64) NOT NULL,
);

INSERT INTO CurationType (value)
VALUES ('is-new-version'),
       ('curation-required'),
       ('curation-performed');

CREATE TABLE DepositCurationEvent (
    depositId CHAR(36) NOT NULL,
    curationId INTEGER IDENTITY NOT NULL,
    value VARCHAR(64) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
    FOREIGN KEY (curationId) REFERENCES CurationType (curationId),
);

CREATE TABLE DepositSpringfield (
    depositId CHAR(36) NOT NULL,
    domain VARCHAR(32) NOT NULL,
    user VARCHAR(32) NOT NULL,
    collection VARCHAR(32) NOT NULL,
    playmode VARCHAR(32) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
);

CREATE TABLE DepositClientMessageContentType (
    depositId CHAR(36) NOT NULL,
    contentType VARCHAR(64) NOT NULL,
    timestamp TIME WITH TIME ZONE NOT NULL,
    FOREIGN KEY (depositId) REFERENCES Deposit (depositId),
);

