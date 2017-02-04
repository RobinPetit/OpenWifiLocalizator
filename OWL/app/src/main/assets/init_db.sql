BEGIN TRANSACTION;

CREATE TABLE "Wifi" (
	`Id`           INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	`BSS`          TEXT,
	`NodeId`       INTEGER,
	`Avg`          REAL,
	`Variance`     REAL,
	`ScanningDate` DATE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "Node" (
	`Id`         INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	`PlanId` INTEGER,
	`X`          REAL,
	`Y`          REAL
);

CREATE TABLE "Edge" (
	`Id`      INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	`Node1Id` INTEGER,
	`Node2Id` INTEGER
);

CREATE TRIGGER CheckEdgeOnSameCampus
	BEFORE INSERT
	ON "Edge"
	WHEN
		(SELECT P.CampusId
			FROM Plan P
			WHERE P.Id=(SELECT N.PlanId
				FROM Node N
				WHERE N.Id=NEW.Node1Id))
		!=
		(SELECT P.CampusId
			FROM Plan P
			WHERE P.Id=(SELECT N.PlanId
				FROM Node N
				WHERE N.Id=NEW.Node2Id))
BEGIN
	SELECT RAISE(ABORT, 'Nodes from an edge must be from the same campus');
END;

CREATE TRIGGER CheckEdgeGoingFromTwoSeparateNodes
	BEFORE INSERT
	ON "Edge"
	WHEN NEW.Node1Id=NEW.Node2Id
BEGIN
	SELECT RAISE(ABORT, 'An edge must join two separate nodes');
END;

-- table to store exceptional edges that don't fit the `Edge` table
-- for instance edges between campus
CREATE TABLE "SpecialEdges" (
	`Id`      INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	`Node1Id` INTEGER REFERENCES Node,
	`Node2Id` INTEGER REFERENCES Node,
	`Weight`  REAL NOT NULL
);

-- TODO: INSERT INTO  `SpecialEdges` VALULES(NodeBorderPlaine, NodeBorderSolbosch, Distance);

CREATE TABLE "Plan" (
	`Id`             INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	`CampusId`       INTEGER,
	`Name`           TEXT,
	`Ppm`            REAL DEFAULT 0.0,
	`ImageDirectory` TEXT,
	`XOnParent`      REAL DEFAULT 0.0,
	`YOnParent`      REAL DEFAULT 0.0,
	`BgCoordX`       REAL DEFAULT 0.0,
	`BgCoordY`       REAL DEFAULT 0.0,
	`RelativeAngle`  REAL DEFAULT 0.0
);

INSERT INTO Plan(CampusId, Name, ImageDirectory) VALUES (0, 'Plaine', '');
INSERT INTO Plan(CampusId, Name, ImageDirectory) VALUES (0, 'Solbosch', '');

CREATE TABLE `AliasesLink` (
	`NodeId`  INTEGER,
	`AliasId` INTEGER
);

CREATE TABLE "Aliases" (
	`Id`   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	`Name` TEXT NOT NULL
);

COMMIT;
