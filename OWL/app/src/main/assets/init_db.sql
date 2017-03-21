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

-- abort insert operation if making an edge between two campuses
CREATE TRIGGER CheckEdgeOnSameCampus
	BEFORE INSERT
	ON "Edge"
	WHEN
		(SELECT 
			CASE 
				WHEN P.CampusId = 0
					THEN P.Id
					ELSE P.CampusId
			END
			FROM Plan P
			WHERE P.Id=(SELECT N.PlanId
				FROM Node N
				WHERE N.Id=NEW.Node1Id))
		!=
		(SELECT 
			CASE 
				WHEN P.CampusId = 0
					THEN P.Id
					ELSE P.CampusId
			END
			FROM Plan P
			WHERE P.Id=(SELECT N.PlanId
				FROM Node N
				WHERE N.Id=NEW.Node2Id))
BEGIN
	SELECT RAISE(ABORT, 'Nodes from an edge must be from the same campus');
END;

-- abort insert operation if making an edge joining a node to itself
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
	`Ppm`            REAL NOT NULL CHECK (Ppm > 0.0),
	`ImageDirectory` TEXT,
	`XOnParent`      REAL DEFAULT 0.0,
	`YOnParent`      REAL DEFAULT 0.0,
	`BgCoordX`       REAL DEFAULT 0.0,
	`BgCoordY`       REAL DEFAULT 0.0,
	`RelativeAngle`  REAL DEFAULT 0.0
);

INSERT INTO Plan(CampusId, Name, Ppm, ImageDirectory) VALUES (0, 'Plaine', 2.69, '');
INSERT INTO Plan(CampusId, Name, Ppm, ImageDirectory) VALUES (0, 'Solbosch', 2.97, '');

CREATE TABLE `AliasesLink` (
	`NodeId`  INTEGER NOT NULL,
	`AliasId` INTEGER NOT NULL
);

CREATE TABLE "Aliases" (
	`Id`   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	`Name` TEXT NOT NULL
);

CREATE INDEX bss_index ON Wifi(BSS);
CREATE INDEX nodeid_index ON Wifi(NodeId);

COMMIT;
