# OWL
from app.Config import Config
from app.database.tables import *
from app.data.PlanData import Node, Edge
# std
import sqlite3
from os.path import splitext, basename

class Database:
    """
        Interface for database manipulations: intended to centralize all of the
        requests in order to have only one opening per execution.

        Note that all queries must be secured: no use of format before using
        connection.execute, but always parametrize the queries and let
        connection.execute replace the values (see documentation fir more information)
    """
    ##### queries definition

    CHECK_IF_PLAN_EXISTS_QUERY = \
        """
        SELECT COUNT(*)
            FROM Building
            WHERE Name=?
        """
    INSERT_PLAN_QUERY = \
        """
        INSERT INTO Building(CampusId, Name, Ppm, ImagePath, XOnParent, YOnParent, BgCoordX, BgCoordY, RelativeAngle)
            VALUES (
                (SELECT id
                    FROM Building
                    WHERE CampusId=0 AND Name LIKE ?),
                ?, ?, ?, ?, ?, ?, ?, ?)
        """
    UPDATE_PLAN_QUERY = \
        """
        UPDATE Building
            SET BgCoordX=?, BgCoordY=?
            WHERE Name=?
        """
    LOAD_PLAN_QUERY = \
        """
        SELECT Ppm, XOnParent, YOnParent, BgCoordX, BgCoordY, RelativeAngle
            FROM Building
            WHERE NAME=?
        """
    INSERT_NODE_QUERY = \
        """
        INSERT INTO Node(buildingId, X, Y)
            VALUES(
                (SELECT id
                    FROM Building
                    WHERE Name=?),
                ?, ?)
        """
    UPDATE_NODE_QUERY = \
        """
        UPDATE Node
            SET X=?, Y=?
            WHERE id=?
        """
    INSERT_ALIAS_QUERY = \
        """
        INSERT INTO Aliases(NodeId, Name)
            VALUES(?, ?)
        """
    INSERT_ACCESS_POINT_QUERY = \
        """
        INSERT INTO Wifi(Bss, NodeId, Min, Max, Avg)
            VALUES(?, ?, ?, ?, ?)
        """

    def __init__(self, path=Config.DB_PATH):
        """constructor: creates an open connection"""
        self.path = path  # may be used at some point
        self.conn = sqlite3.connect(path)
        # set to False for big updates and only commit at the end
        self.allowed_to_commit = True

    def close(self, need_to_commit=False):
        """properly closes the connection to the database"""
        if need_to_commit:
            self.commit()
        self.conn.close()

    ##### static

    @staticmethod
    def path_to_building_name(path):
        return splitext(basename(path))[0]
        
    @staticmethod
    def center_of_rectangle(rectangle):
        return [(rectangle[i]+rectangle[2+i])//2 for i in (0, 1)]

    ##### save functions

    ## private
    def commit(self):
        if self.allowed_to_commit:
            self.conn.commit()

    def save_plan(self, path, plan_data, bg_coord=(0, 0)):
        """registers a new plan"""
        query = Database.INSERT_PLAN_QUERY
        self.conn.execute(query, (
            path[0] + '%',
            path,
            plan_data.ppm,
            '',
            plan_data.x,
            plan_data.y,
            bg_coord[0],
            bg_coord[1],
            plan_data.angle))
        # NOTE: ImagePath is ignored (set to empty string) since it is intended to be removed
        # then @TODO: update the method when ImagePath is removed from db schematics
        self.commit()

    def update_plan(self, bg_coord, filename):
        """updates the background image coord"""
        query = Database.UPDATE_PLAN_QUERY
        self.conn.execute(query, (*bg_coord, filename))
        self.commit()

    def save_node(self, node, plan_name):
        """registers a new node
        returns the id of the fresh node"""
        assert type(node) is Node
        query = Database.INSERT_NODE_QUERY
        cursor = self.conn.execute(query, (plan_name, *Database.center_of_rectangle(node.coord())))
        node_id = cursor.lastrowid
        print('node id is: ' + str(node_id))
        query = Database.INSERT_ALIAS_QUERY
        for alias in node.aliases():
            self.conn.execute(query, (node_id, alias))
        query = Database.INSERT_ACCESS_POINT_QUERY
        for ap in node.access_points():
            self.conn.execute(query, (ap.get_bss(), node_id, ap.get_min(), ap.get_max(), ap.avg()))
        self.commit()
        return node_id
        
    def update_node(self, node):
        """changes the coordinate of a node"""
        query = Database.UPDATE_NODE_QUERY
        self.conn.execute(query, (*node.coord(), node.id()))
        self.commit()

    ##### load functions

    def exists_plan(self, plan_name):
        """returns True if the plan already exists in database and False otherwise"""
        query = Database.CHECK_IF_PLAN_EXISTS_QUERY
        return self.conn.execute(query, (plan_name,)).fetchone()[0] != 0

    def load_plan(self, filename):
        """retrieves a plan"""
        assert self.exists_plan(filename)
        query = Database.LOAD_PLAN_QUERY
        cursor = self.conn.execute(query, (filename,))
        plan = cursor.fetchone()
        return BuildingTable(filename, plan[0], tuple(plan[1:3]), tuple(plan[3:5]), plan[5])

    #def load_node


