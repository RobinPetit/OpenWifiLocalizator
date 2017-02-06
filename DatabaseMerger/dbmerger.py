#!/usr/bin/python3

from sys import argv
from os.path import isfile
from os import remove
from shutil import copyfile
from time import gmtime, strftime
import sqlite3

class Logger:
    LOG_FILE = 'dbmerger.log'

    def __init__(self):
        self.file = open(Logger.LOG_FILE, 'a')

    def log(self, message):
        time = strftime("%Y-%m-%d %H:%M:%S", gmtime())
        self.file.write("[{}] {}\n".format(time, message))

    def close(self):
        self.file.close()

class AbstractDatabase:
    def __init__(self, path):
        if not isfile(path):
            raise ValueError('Error: You must provide a valid database file ({})!' \
                             .format(path))
        self.path = path
        self.connection = sqlite3.connect(path)

    def get_db_path(self):
        return self.path

    def close(self):
        self.connection.close()

class ReadableDatabase(AbstractDatabase):
    def __init__(self, path):
        super().__init__(path)

    ##### queries

    def get_plans_name(self):
        """returns a list of names"""
        query = \
            """
            SELECT Name FROM Plan
                WHERE CampusId!=0;
            """
        return [r[0] for r in self.connection.execute(query)]

    def get_all_plans(self):
        """returns a dictionary mapping an id to a tuple containing:
        + CampusId
        + Name
        + Ppm
        + ImageDirecitory
        + XOnParent
        + YOnParent
        + BgCoordX
        + BgCoordY
        + RelativeAngle"""
        query = \
            """
            SELECT * FROM Plan
                WHERE CampusId!=0
            """
        ret = dict()
        for plan in self.connection.execute(query):
            ret[plan[0]] = plan[1:]
        return ret

    def get_all_nodes(self):
        """returns a dictionary mapping an id to a tuple containing:
        + PlanId
        + X
        + Y"""
        query = \
            """
            SELECT * FROM Node;
            """
        ret = dict()
        for node in self.connection.execute(query):
            ret[node[0]] = node[1:]
        return ret

    def get_all_edges(self):
        """return a dictionary mapping an id to a tuple containing:
        + Node1Id
        + Node2Id"""
        query = \
            """
            SELECT * FROM Edge;
            """
        ret = dict()
        for edge in self.connection.execute(query):
            ret[edge[0]] = edge[1:]
        return ret

    def has_node_alias(self, node_id, alias):
        """return True if given node has given alias"""
        query = \
            """
            SELECT COUNT(id)
                FROM AliasesLink
                WHERE NodeId=?
                    AND AliasId=(
                        SELECT Id
                            FROM Aliases
                            WHERE Name=?)
            """
        return self.connection.execute(query, (node_id, alias)).fetchone()[0] > 0

    def get_node_aliases(self, node_id):
        """return a list of aliases attached to given node"""
        query = \
            """
            SELECT A.Name
                From Aliases A
                JOIN AliasesLink L
                    ON L.AliasId=A.Id
                WHERE L.NodeId=?
            """
        return [r[0] for r in self.connection.execute(query, (node_id,))]

    def contains_alias(self, alias):
        """return True if given alias already exists in db"""
        query = \
            """
            SELECT COUNT(id)
                FROM Aliases
                WHERE Name=?
            """
        return self.connection.execute(query, (alias,)).fetchone()[0]

    def get_wifis_by_node(self, node_id):
        """return a list of tuples containing:
        + BSS
        + NodeId
        + Avg
        + Variance
        + ScanningDate"""
        query = \
            """
            SELECT BSS, NodeId, Avg, Variance, ScanningDate
                FROM Wifi
                WHERE NodeId=?;
            """
        return [r for r in self.connection.execute(query, (node_id,))]

class WritableDatabase(ReadableDatabase):
    def __init__(self, path):
        super().__init__(path)

    def commit(self):
        self.connection.commit()

    ##### queries
    def insert_plan(self, data):
        """insert a plan and return its id"""
        query = \
            """
            INSERT INTO Plan(CampusId, Name, Ppm, ImageDirectory, XOnParent,
                             YOnParent, BgCoordX, BgCoordY, RelativeAngle)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
        return self.connection.execute(query, data).lastrowid

    def insert_node(self, x, y, plan_id):
        """insert a plan and return its id"""
        query = \
            """
            INSERT INTO Node(PlanId, X, Y)
                VALUES(?, ?, ?)
            """
        return self.connection.execute(query, (plan_id, x, y)).lastrowid

    def insert_edge(self, node1_id, node2_id):
        """insert an edge and return its id"""
        query = \
            """
            INSERT INTO Edge(Node1Id, Node2Id)
                VALUES(?, ?)
            """
        return self.connection.execute(query, (node1_id, node2_id)).lastrowid

    def add_alias(self, alias):
        """add an alias to db and return its id"""
        query = \
            """
            INSERT INTO Aliases(Name)
                VALUES(?)
            """
        return self.connection.execute(query, (alias,)).lastrowid

    def add_alias_to_node(self, node_id, alias):
        """link given alias to given node.
        If alias doesn't exist yet in db, then it is created"""
        if not self.contains_alias(alias):
            self.add_alias(alias)
        query = \
            """
            INSERT INTO AliasesLink(NodeId, AliasId)
                Values(?, (
                    SELECT Id
                        FROM Aliases
                        WHERE Name=?)
                )
            """
        self.connection.execute(query, (node_id, alias))

    def add_wifi_to_node(self, wifi):
        """..."""
        query = \
            """
            INSERT INTO Wifi(BSS, NodeId, Avg, Variance, ScanningDate)
                VALUES(?, ?, ?, ?, ?)
            """
        self.connection.execute(query, tuple(wifi))

class DatabasesMerger:
    def __init__(self, write, read):
        self.write_db = write
        self.read_db = read

    def convert_plan_id(self, plan_id):
        """return the new id of the plan having given id"""
        return self.plans_id_map[plan_id]

    def has_read_plan_id(self, plan_id):
        """return True if plan_id has been copied"""
        return self.convert_plan_id(plan_id) is not None

    def convert_node_id(self, node_id):
        """return the new id of the node having given id"""
        return self.nodes_id_map[node_id]

    def has_read_node_id(self, node_id):
        """return True if node_id has been copied"""
        return self.convert_node_id(node_id) is not None

    def merge_plans(self):
        """copy all plans from read into write and returns a dictionary mapping ids read -> write"""
        read_plans = self.read_db.get_all_plans()
        existing_plans = self.write_db.get_plans_name()
        for plan_id in read_plans:
            if read_plans[plan_id][1] in existing_plans:
                log('WARNING: Plan "{}" already exists in "{}". Plan is ignored!' \
                    .format(read_plans[plan_id][1], self.write_db.get_db_path()))
                read_plans[plan_id] = None
            else:
                read_plans[plan_id] = self.write_db.insert_plan(read_plans[plan_id])
        self.plans_id_map = read_plans

    def merge_nodes(self):
        """copy all nodes from plans which have been copied"""
        read_nodes = self.read_db.get_all_nodes()
        for node_id in read_nodes:
            plan_id = read_nodes[node_id][0]
            if self.has_read_plan_id(plan_id):
                read_nodes[node_id] = self.write_db.insert_node(
                    *read_nodes[node_id][1:],
                    self.convert_plan_id(plan_id))
            else:
                read_nodes[node_id] = None
        self.nodes_id_map = read_nodes

    def merge_edges(self):
        """copy all edges joining nodes that have both been copied"""
        read_edges = self.read_db.get_all_edges()
        for edge_id in read_edges:
            n1_id = read_edges[edge_id][0]
            n2_id = read_edges[edge_id][1]
            if self.has_read_node_id(n1_id) and self.has_read_node_id(n2_id):
                read_edges[edge_id] = self.write_db.insert_edge(
                    self.convert_node_id(n1_id),
                    self.convert_node_id(n2_id))
            else:
                read_edges[edge_id] = None
        self.edges_id_map = read_edges

    def merge_aliases(self):
        """copy all aliases that are linked to copied nodes"""
        for old_node_id in self.nodes_id_map:
            if self.nodes_id_map[old_node_id] is not None:
                for alias in self.read_db.get_node_aliases(old_node_id):
                    self.write_db.add_alias_to_node(self.convert_node_id(old_node_id), alias)

    def merge_wifis(self):
        """copy all wifis attached to copied nodes"""
        for old_node_id in self.nodes_id_map:
            if self.nodes_id_map[old_node_id] is not None:
                # TODO copy wifis
                wifis = self.read_db.get_wifis_by_node(old_node_id)
                for wifi in wifis:
                    self.write_db.add_wifi_to_node(wifi)

    def merge(self):
        # copy all plans and retrieve a dictionary mapping id in read -> id in write
        log('\tMerging plans')
        self.merge_plans()
        # copy all nodes and retrieve a dictionary mapping id in read -> id in write
        log('\tMerging nodes')
        self.merge_nodes()
        # copy all edges from nodes ids
        log('\tMerging edges')
        self.merge_edges()
        # copy all aliases and aliases links if not existing
        log('\tMerging aliases')
        self.merge_aliases()
        # copy all wifis (unconditionally)
        log('\tMerging wifis')
        self.merge_wifis()

logger = Logger()

def log(message):
    print(message)
    logger.log(message)

def usage():
    print('Usage:   ./dbmerger.py [-r] <database (1) to keep> <database(s) to insert in (1)>\n' +
          'Example: ./dbmerger.py Denis.db Remy.db Robin.db Nathan.db\n'
          '    This example will keep Denis.db and copy the content of all three other databases\n' +
          '    in Denis.db.\n' +
          '\n' +
          'If "-r" is given as parameter, the provided databases will be removed\n' +
          'once their content is copied except the first one of course.')

def main():
    if len(argv) < 3 or (argv[1] == '-r' and len(argv) < 4):
        usage()
        return
    need_to_remove = (argv[1] == '-r')
    db_to_keep_idx = 2 if need_to_remove else 1
    path_db_to_keep = argv[db_to_keep_idx]
    paths_dbs_to_copy = argv[db_to_keep_idx+1:]

    try:
        writeable_db = WritableDatabase(path_db_to_keep)
        log('Creating backup of {}'.format(path_db_to_keep))
        copyfile(path_db_to_keep, 'BACKUP-'+path_db_to_keep)
        for path in paths_dbs_to_copy:
            try:
                read_database = ReadableDatabase(path)
                log('Opening {}'.format(path))
                DatabasesMerger(writeable_db, read_database).merge()
                read_database.close()
                log('Closing {}'.format(path))
                log('database {} has been copied in {}'.format(path, path_db_to_keep))
                if need_to_remove:
                    remove(path)
            except ValueError as e:
                log(e)
        writeable_db.commit()
        writeable_db.close()
    except ValueError as e:
        log(e)
    logger.close()

if __name__ == '__main__':
    main()
