# OWL
from app.general.constants import *
from app.Config import Config
from app.database.tables import *
from app.data.PlanData import Node, Edge, ExternalEdge
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
    
    ########## INSERTS
    
    INSERT_PLAN_QUERY = \
        """
        INSERT INTO Building(CampusId, Name, Ppm, ImagePath, XOnParent, YOnParent, BgCoordX, BgCoordY, RelativeAngle)
            VALUES (
                (SELECT id
                    FROM Building
                    WHERE CampusId=0 AND Name LIKE ?),
                ?, ?, ?, ?, ?, ?, ?, ?)
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
    INSERT_ALIAS_QUERY = \
        """
        INSERT INTO Aliases(Name)
            VALUES(?)
        """
    LINK_NODE_TO_ALIAS = \
        """
        INSERT INTO AliasesLink(NodeId, AliasId)
            VALUES(?, (
                SELECT id
                    FROM Aliases
                    WHERE Name=?))
        """
    INSERT_ACCESS_POINT_QUERY = \
        """
        INSERT INTO Wifi(Bss, NodeId, Min, Max, Avg, Variance)
            VALUES(?, ?, ?, ?, ?, ?)
        """
    INSERT_EDGE_QUERY = \
        """
        INSERT INTO Edge(Node1Id, Node2Id, Weight)
            VALUES(?, ?, ?)
        """
    
    ########## LOAD
    
    LOAD_PLAN_QUERY = \
        """
        SELECT Ppm, XOnParent, YOnParent, BgCoordX, BgCoordY, RelativeAngle
            FROM Building
            WHERE NAME=?
        """
    LOAD_NODES_FROM_BUILDING_QUERY = \
        """
        SELECT Id, X, Y
            FROM Node
            WHERE BuildingId=(
                SELECT Id
                    FROM Building
                    WHERE Name=?)
        """
    LOAD_ALL_ALIASES_QUERY = \
        """
        SELECT Name
            From Aliases;
        """
    LOAD_ALIASES_FROM_NODE_ID_QUERY = \
        """
        SELECT A.Name
            FROM Aliases A
            JOIN AliasesLink L
            WHERE L.NodeId=?
        """
    LOAD_EDGES_FROM_BUILDING_QUERY = \
        """
        SELECT E.Node1Id, E.Node2Id, E.Weight
            FROM Edge E
            JOIN Node N1 on N1.Id=E.Node1Id
            JOIN Node N2 on N2.Id=E.Node2Id
            WHERE N1.BuildingId=N2.BuildingId
                AND N1.BuildingId=(
                    SELECT Id
                        FROM Building
                        WHERE Name=?)
        """
    
    ########## UPDATE
    
    UPDATE_PLAN_QUERY = \
        """
        UPDATE Building
            SET BgCoordX=?, BgCoordY=?
            WHERE Name=?
        """
    UPDATE_NODE_QUERY = \
        """
        UPDATE Node
            SET X=?, Y=?
            WHERE id=?
        """
    UPDATE_EDGE_QUERY = \
        """
        UPDATE Edge
            SET Weight=?
            WHERE id=?
        """
    
    ########## MISC

    CHECK_IF_PLAN_EXISTS_QUERY = \
        """
        SELECT COUNT(*)
            FROM Building
            WHERE Name=?
        """
    CHECK_IF_NODE_HAS_ACCESS_POINTS_QUERY = \
        """
        SELECT COUNT(Id)
            FROM Wifi
            WHERE NodeId=?
        """
        
    ##### Code

    def __init__(self, path=Config.DB_PATH):
        """constructor: creates an open connection"""
        self.path = path  # may be used at some point
        self.conn = sqlite3.connect(path)
        # set to False for big updates and only commit at the end
        self.allowed_to_commit = True
        self.all_aliases = self.get_all_aliases()

    def close(self, need_to_commit=False):
        """properly closes the connection to the database"""
        if need_to_commit:
            self.commit()
        self.conn.close()
        
    def get_all_aliases(self):
        """return a list of all aliases currently in the database"""
        query = Database.LOAD_ALL_ALIASES_QUERY
        return [r[0] for r in self.conn.execute(query).fetchall()]

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
        cursor = self.conn.execute(query, (
            plan_name,
            *Database.center_of_rectangle(node.coord())))
        node_id = cursor.lastrowid
        print('node id is: ' + str(node_id))
        query = Database.LINK_NODE_TO_ALIAS
        for alias in node.aliases():
            if not alias in self.all_aliases:
                self.all_aliases.append(alias)
                self.add_alias(alias)
            self.conn.execute(query, (node_id, alias))
        query = Database.INSERT_ACCESS_POINT_QUERY
        for ap in node.access_points():
            self.conn.execute(query, (
                ap.get_bss(),
                node_id,
                -ap.get_min(),
                -ap.get_max(),
                -ap.avg(),
                ap.get_variance()))
        self.commit()
        return node_id
        
    def add_alias(self, alias):
        """add a brand new alias into the database alias list"""
        query = Database.INSERT_ALIAS_QUERY
        self.conn.execute(query, (alias,))
        self.commit()
        
    def update_node(self, node):
        """changes the coordinate of a node"""
        query = Database.UPDATE_NODE_QUERY
        self.conn.execute(query, (*node.coord(), node.id()))
        self.commit()
        
    def save_edge(self, edge):
        """register a new edge
        returns the id of the fresh edge"""
        assert type(edge) is Edge or type(edge) is ExternalEdge
        query = Database.INSERT_EDGE_QUERY
        cursor = self.conn.execute(query, (*edge.get_extremity_ids(), edge.weight()))
        self.commit()
        return cursor.lastrowid

    def update_edge(self, edge):
        """changes the weight of an edge"""
        query = Database.UPDATE_EDGE_QUERY
        self.conn.execute(query, (edge.weight(),))
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

    def load_nodes_from_building(self, plan_name):
        """returns a list of tuples (id, coords, aliases, has_ap) of all the
        Nodes on the given plan where:
        + id is the node id
        + coords are the coordinates of the node
        + aliases are the aliases of the node
        + has_ap tells whether node has already been scanned"""
        nodes = list()
        query = Database.LOAD_NODES_FROM_BUILDING_QUERY
        nodes_cursor = self.conn.execute(query, (plan_name,))
        for result in nodes_cursor.fetchall():
            node_id = result[0]
            coords = result[1:3]
            has_ap = self.node_has_access_point(node_id)
            aliases = self.load_aliases_of_node(node_id)
            ## create node
            nodes.append((node_id, *coords, aliases, has_ap))
        return nodes
            
    def node_has_access_point(self, node_id):
        """returns True if node has scanned wifis"""
        query = Database.CHECK_IF_NODE_HAS_ACCESS_POINTS_QUERY
        cursor = self.conn.execute(query, (node_id,))
        _ = cursor.fetchone()[0]
        print(_, 'aps')
        return _ > 0
    
    def load_aliases_of_node(self, node_id):
        """returns a list sof aliases for a given node"""
        query = Database.LOAD_ALIASES_FROM_NODE_ID_QUERY
        return [r[0] for r in self.conn.execute(query, (node_id,)).fetchall()]
        
    def load_edges_from_building(self, plan_name):
        """returns a list of (id, id, weight) where ids are nodes ids to draw the
        edges from/to and weight is the weight of the edge"""
        query = Database.LOAD_EDGES_FROM_BUILDING_QUERY
        return self.conn.execute(query, (plan_name,)).fetchall()


