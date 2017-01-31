-- This file must be run once in order to create the database structure

CREATE TABLE IF NOT EXISTS CampusPlan (
	id         INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	ppm        INTEGER,
	IMAGE_PATH TEXT
);

CREATE TABLE IF NOT EXISTS BuildingPlan (
	id         INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	campusId   INTEGER NOT NULL REFERENCES CampusPlan,
	name       TEXT,
	ppm        INTEGER,
	IMAGE_PATH TEXT,
	xOnParent  INTEGER,
	yOnParent  INTEGER,
	bgCoordX   INTEGER,
	bgCoordY   INTEGER,
	angle      FLOAT  -- in degrees!
);

CREATE TABLE IF NOT EXISTS Node (
	id           INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
	buildingId   INTEGER NOT NULL REFERENCES BuildingPlan,
	xCoord       INTEGER,
	yCoord       INTEGER,
	idOnBuilding INTEGER
);

