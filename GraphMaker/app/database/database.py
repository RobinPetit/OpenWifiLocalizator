# OWL
from app.general.constants import *
from app.general.functions import *
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
        INSERT INTO Plan(CampusId, Name, Ppm, ImageDirectory, XOnParent, YOnParent, BgCoordX, BgCoordY, RelativeAngle)
            VALUES (
                (SELECT id
                    FROM Plan
                    WHERE CampusId=0 AND Name LIKE ?),
                ?, ?, ?, ?, ?, ?, ?, ?)
        """
    INSERT_NODE_QUERY = \
        """
        INSERT INTO Node(PlanId, X, Y)
            VALUES(
                (SELECT id
                    FROM Plan
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
        INSERT INTO Wifi(Bss, NodeId, Avg, Variance)
            VALUES(?, ?, ?, ?)
        """
    INSERT_EDGE_QUERY = \
        """
        INSERT INTO Edge(Node1Id, Node2Id)
            VALUES(?, ?)
        """
    
    ########## LOAD
    
    LOAD_PLAN_QUERY = \
        """
        SELECT Ppm, XOnParent, YOnParent, BgCoordX, BgCoordY, RelativeAngle, ImageDirectory
            FROM Plan
            WHERE NAME=?
        """
    LOAD_NODES_FROM_PLAN_QUERY = \
        """
        SELECT Id, X, Y
            FROM Node
            WHERE PlanId=(
                SELECT Id
                    FROM Plan
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
                ON L.AliasId=A.Id
            WHERE L.NodeId=?
        """
    LOAD_EDGES_FROM_PLAN_QUERY = \
        """
        SELECT E.Id, E.Node1Id, E.Node2Id
            FROM Edge E
            JOIN Node N1
                ON N1.Id=E.Node1Id
            JOIN Node N2
                ON N2.Id=E.Node2Id
            WHERE N1.PlanId=N2.PlanId
                AND N1.PlanId=(
                    SELECT Id
                        FROM Plan
                        WHERE Name=?)
        """
    LOAD_EXTERNAL_EDGES_FROM_NODE_ID = \
        """
        SELECT E.Id, E.Node1Id, E.Node2Id
            FROM Edge E
            JOIN Node N1
                ON N1.Id=E.Node1Id
            JOIN Node N2
                ON N2.Id=E.Node2Id
            WHERE N1.PlanId!=N2.PlanId
                AND (N1.Id=? OR N2.Id=?)
        """
    LOAD_PLAN_NAME_FROM_NODE_ID = \
        """
        SELECT P.Name
            FROM Plan P
            JOIN Node N
                ON N.PlanId=P.Id
            WHERE N.Id=?
        """
    
    ########## UPDATE
    
    UPDATE_PLAN_QUERY = \
        """
        UPDATE Plan
            SET BgCoordX=?, BgCoordY=?
            WHERE Name=?
        """
    UPDATE_NODE_POSITION_QUERY = \
        """
        UPDATE Node
            SET X=?, Y=?
            WHERE Id=?
        """
        
    ########## DELETE
    
    REMOVE_ALIAS_FROM_NODE = \
        """
        DELETE FROM AliasesLink
            WHERE AliasId=(
                SELECT Id
                    FROM Aliases
                    WHERE Name=?)
                AND NodeId=?
        """
    REMOVE_ACCESS_POINTS_OF_NODE_QUERY = \
        """
        DELETE FROM Wifi
            WHERE NodeId=?
        """
    REMOVE_ALIAS_QUERY = \
        """
        DELETE FROM Aliases
            WHERE Name=?
        """
    REMOVE_EDGE_BY_ID_QUERY = \
        """
        DELETE FROM Edge
            WHERE Id=?
        """
    REMOVE_EDGE_BY_NODES_IDS_QUERY = \
        """
        DELETE FROM Edge
            WHERE (Node1Id=? AND Node2Id=?) OR (Node1Id=? AND Node2Id=?)
        """
    REOMVE_EDGES_FROM_NODE_ID_QUERY = \
        """
        DELETE FROM Edge
            WHERE Node1Id=? OR Node2Id=?
        """
    REMOVE_NODE_QUERY = \
        """
        DELETE FROM Node
            WHERE Id=?
        """
    
    ########## MISC

    CHECK_IF_PLAN_EXISTS_QUERY = \
        """
        SELECT COUNT(id)
            FROM Plan
            WHERE Name=?
        """
    CHECK_IF_NODE_HAS_ACCESS_POINTS_QUERY = \
        """
        SELECT COUNT(Id)
            FROM Wifi
            WHERE NodeId=?
        """
    CHECK_IF_NODE_HAS_EXTERNAL_EDGES_QUERY = \
        """
        SELECT COUNT(E.id)
            FROM Edge E
            JOIN Node N1
                ON N1.Id=E.Node1Id
            JOIN Node N2
                ON N2.Id=E.Node2Id
            WHERE (N1.PlanId!=N2.PlanId) AND (N1.Id=? OR N2.Id=?)
        """
    CHECK_IF_ALIAS_IS_UNUSED = \
        """
        SELECT COUNT(L.NodeId)
            FROM AliasesLink L
            JOIN Aliases A
                ON A.Id=L.AliasId
            WHERE A.Name=?
        """
        
    ##### Code

    def __init__(self, path=Config.DB_PATH):
        """constructor: creates an open connection"""
        self.path = path  # may be used at some point
        self.conn = sqlite3.connect(path)
        # set to False for big updates and only commit at the end
        self.allowed_to_commit = True
        self.all_aliases = self.get_all_aliases()
        self.disable_counter = 0

    def close(self, need_to_commit=False):
        """properly closes the connection to the database"""
        if need_to_commit:
            self.commit()
        self.conn.close()
        
    def get_all_aliases(self):
        """return a list of all aliases currently in the database"""
        query = Database.LOAD_ALL_ALIASES_QUERY
        return [r[0] for r in self.conn.execute(query).fetchall()]
        
    def disable_commit(self):
        self.disable_counter += 1
        self.allowed_to_commit = False
        
    def enable_commit(self):
        self.disable_counter -= 1
        self.allowed_to_commit = self.disable_counter == 0

    ##### save functions

    ## private
    def commit(self):
        if self.allowed_to_commit:
            self.conn.commit()
            if Config.DEBUG:
                print('commiting change')

    def save_plan(self, path, plan_data, bg_coord=(0, 0)):
        """registers a new plan"""
        query = Database.INSERT_PLAN_QUERY
        self.conn.execute(query, (
            path[0] + '%',
            path,
            plan_data.ppm,
            plan_data.image_dir,
            plan_data.x,
            plan_data.y,
            bg_coord[0],
            bg_coord[1],
            plan_data.angle))
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
            *center_of_rectangle(node.coord())))
        node_id = cursor.lastrowid
        self.add_aliases_to_node(node_id, node.aliases())
        self.set_node_access_points(node_id, node.access_points())
        self.commit()
        return node_id
        
    def add_aliases_to_node(self, node_id, aliases):
        """add the given aliases to the given node"""
        self.disable_commit()
        query = Database.LINK_NODE_TO_ALIAS
        for alias in aliases:
            if not alias in self.all_aliases:
                self.all_aliases.append(alias)
                self.add_alias(alias)
            self.conn.execute(query, (node_id, alias))
        self.enable_commit()
        self.commit()
        
    def add_alias(self, alias):
        """add a brand new alias into the database alias list"""
        query = Database.INSERT_ALIAS_QUERY
        self.conn.execute(query, (alias,))
        self.commit()
        
    def update_node_position(self, node):
        """changes the coordinate of a node"""
        query = Database.UPDATE_NODE_POSITION_QUERY
        self.conn.execute(query, (*center_of_rectangle(node.coord()), node.id()))
        self.commit()
        
    def update_all_nodes_position(self, nodes_list):
        """changes the coordinate of every given node"""
        self.disable_commit()
        for node in nodes_list:
            self.update_node_position(node)
        self.enable_commit()
        self.commit()
        
    def update_node_aliases(self, node, removed, added):
        """updates aliases of a node"""
        assert type(removed) is type(added) is set
        self.disable_commit()
        for alias in removed:
            query = Database.REMOVE_ALIAS_FROM_NODE
            self.conn.execute(query, (alias, node.id(),))
            if self.is_alias_unused(alias):
                self.remove_alias(alias)
        self.add_aliases_to_node(node.id(), added)
        self.enable_commit()
        self.commit()
        
    def set_node_access_points(self, node_id, access_points):
        """set (replaces if exists) the access points linked to a given node"""
        if type(node_id) is Node:
            node_id = node_id.id()
        assert type(node_id) is int
        self.remove_access_points_from_node(node_id)
        query = Database.INSERT_ACCESS_POINT_QUERY
        for ap in access_points:
            self.conn.execute(query, (
                ap.get_bss(),
                node_id,
                -ap.get_avg(),
                ap.get_variance())
            )
        self.commit()
        
    def save_edge(self, edge):
        """register a new edge
        returns the id of the fresh edge"""
        assert type(edge) in (Edge, ExternalEdge)
        query = Database.INSERT_EDGE_QUERY
        cursor = self.conn.execute(query, tuple(edge.get_extremity_ids()))
        self.commit()
        return cursor.lastrowid

    ##### load functions

    def exists_plan(self, plan_name):
        """returns True if the plan already exists in database and False otherwise"""
        query = Database.CHECK_IF_PLAN_EXISTS_QUERY
        return self.conn.execute(query, (plan_name,)).fetchone()[0] != 0
    
    def is_alias_unused(self, alias):
        """return True if no node is linked to the given alias and False otherwise"""
        query = Database.CHECK_IF_ALIAS_IS_UNUSED
        return self.conn.execute(query, (alias,)).fetchone()[0] == 0
        
    def is_alias_used(self, alias):
        """@see is_alias_unused"""
        return not self.is_alias_unused(alias)

    def load_plan(self, filename):
        """retrieves a plan"""
        assert self.exists_plan(filename)
        query = Database.LOAD_PLAN_QUERY
        cursor = self.conn.execute(query, (filename,))
        plan = cursor.fetchone()
        return PlanTable(filename, plan[0], tuple(plan[1:3]), tuple(plan[3:5]), plan[5], plan[6])

    def load_nodes_from_plan(self, plan_name):
        """returns a list of tuples (id, coords, aliases, has_ap) of all the
        Nodes on the given plan where:
        + id is the node id
        + coords are the coordinates of the node
        + aliases are the aliases of the node
        + has_ap tells whether node has already been scanned
        + has_ext_edge tells whether node has external edges"""
        nodes = list()
        query = Database.LOAD_NODES_FROM_PLAN_QUERY
        nodes_cursor = self.conn.execute(query, (plan_name,))
        for result in nodes_cursor.fetchall():
            node_id = result[0]
            coords = result[1:3]
            has_ap = self.node_has_access_point(node_id)
            aliases = self.load_aliases_of_node(node_id)
            has_ext_edge = self.node_has_external_edges(node_id)
            ## create node
            nodes.append((node_id, *coords, aliases, has_ap, has_ext_edge))
        return nodes

    def node_has_external_edges(self, node_id):
        """ returns True if node has external edges"""
        query = Database.CHECK_IF_NODE_HAS_EXTERNAL_EDGES_QUERY
        cursor = self.conn.execute(query, (node_id, node_id))
        return cursor.fetchone()[0] > 0

    def node_has_access_point(self, node_id):
        """returns True if node has scanned wifis"""
        query = Database.CHECK_IF_NODE_HAS_ACCESS_POINTS_QUERY
        cursor = self.conn.execute(query, (node_id,))
        return cursor.fetchone()[0] > 0

    def load_aliases_of_node(self, node_id):
        """returns a list sof aliases for a given node"""
        query = Database.LOAD_ALIASES_FROM_NODE_ID_QUERY
        return [r[0] for r in self.conn.execute(query, (node_id,)).fetchall()]
        
    def load_edges_from_plan(self, plan_name):
        """returns a list of (id, id) where ids are nodes ids to draw the edges from/to"""
        query = Database.LOAD_EDGES_FROM_PLAN_QUERY
        return self.conn.execute(query, (plan_name,)).fetchall()
    
    def load_external_edges_from_node(self, node_id):
        """returns a list of edges going from the given node to other plans"""
        query = Database.LOAD_EXTERNAL_EDGES_FROM_NODE_ID
        return [edge for edge in self.conn.execute(query, (node_id, node_id)).fetchall()]
        
    def get_plan_name_from_node(self, node_id):
        """returns the name of the plan the given ode stands in"""
        query = Database.LOAD_PLAN_NAME_FROM_NODE_ID
        return self.conn.execute(query, (node_id,)).fetchone()[0]
        
    ##### remove functions
    
    def remove_access_points_from_node(self, node_id):
        """removes all the access points linked to a node"""
        query = Database.REMOVE_ACCESS_POINTS_OF_NODE_QUERY
        self.conn.execute(query, (node_id,))
        self.commit()
    
    def remove_alias(self, alias):
        """removes an alias from the aliases list
        WARNING: does not check if any node is still linked to the alias.
        NEVER CALL THIS FUNCTION FROM OUTSIDE THE CLASS!"""
        query = Database.REMOVE_ALIAS_QUERY
        self.conn.execute(query, (alias,))
        self.commit()
        
    def remove_edge(self, edge):
        """remove an edge from database"""
        query = Database.REMOVE_EDGE_BY_ID_QUERY
        self.conn.execute(query, (edge.id(),))
        self.commit()
        
    def remove_edge_by_nodes(self, node1_id, node2_id):
        """removes the edge(s) joining the given nodes"""
        query = Database.REMOVE_EDGE_BY_NODES_IDS_QUERY
        self.conn.execute(query, (node1_id, node2_id, node2_id, node1_id))
        self.commit()
    
    def remove_node(self, node):
        """removes a node from the database and removes all of the edges connected to it"""
        query = Database.REMOVE_NODE_QUERY
        self.conn.execute(query, (node.id(),))
        query = Database.REOMVE_EDGES_FROM_NODE_ID_QUERY
        self.conn.execute(query, (node.id(), node.id()))
        self.commit()


