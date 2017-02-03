BEGIN TRANSACTION;

CREATE TABLE "Wifi" (
	`Id`       INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	`BSS`      TEXT,
	`NodeId`   INTEGER,
	`Min`      REAL,
	`Max`      REAL,
	`Avg`      REAL,
	`Variance` REAL
);

CREATE TABLE "Node" (
	`Id`         INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	`BuildingId` INTEGER,
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
		(SELECT B.CampusId
			FROM Building B
			WHERE B.Id=(SELECT N.BuildingId
				FROM Node N
				WHERE N.Id=NEW.Node1Id))
		!=
		(SELECT B.CampusId
			FROM Building B
			WHERE B.Id=(SELECT N.BuildingId
				FROM Node N
				WHERE N.Id=NEW.Node2Id))
BEGIN
	SELECT RAISE(ABORT, 'Nodes from an edge must be from the same campus');
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

CREATE TABLE "Campus" (
	`Id`     INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	`Name`   TEXT NOT NULL,
	`Abbrev` TEXT
);

INSERT INTO `Campus` VALUES (1,'Plaine','P');
INSERT INTO `Campus` VALUES (2,'Solbosch','S');

CREATE TABLE "Building" (
	`Id`            INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,
	`CampusId`      INTEGER,
	`Name`          TEXT,
	`Ppm`           REAL DEFAULT 0.0,
	`ImagePath`     TEXT,
	`XOnParent`     REAL DEFAULT 0.0,
	`YOnParent`     REAL DEFAULT 0.0,
	`BgCoordX`      REAL DEFAULT 0.0,
	`BgCoordY`      REAL DEFAULT 0.0,
	`RelativeAngle` REAL DEFAULT 0.0
);

INSERT INTO Building(CampusId, Name, Ppm, ImagePath, XOnParent, XOnParent) VALUES (0, 'Plaine', 2.78, NULL, NULL, NULL);
INSERT INTO Building(CampusId, Name, Ppm, ImagePath, XOnParent, XOnParent) VALUES (0, 'Solbosch', 2.97, NULL, NULL, NULL);

CREATE TABLE `AliasesLink` (
	`NodeId`  INTEGER,
	`AliasId` INTEGER
);

CREATE TABLE "Aliases" (
	`Id`   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	`Name` TEXT NOT NULL
);

COMMIT;
