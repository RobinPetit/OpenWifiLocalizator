#!/usr/bin/python3

from sys import argv
from os.path import isfile
from shutil import copyfile
import sqlite3

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

class WritableDatabase(ReadableDatabase):
    def __init__(self, path):
        super().__init__(path)

    def commit(self):
        self.connection.commit()

    ##### queries
    def insert_plan(self, data):
        """insert a plan and returns its id"""
        query = \
            """
            INSERT INTO Plan(CampusId, Name, Ppm, ImageDirectory, XOnParent,
                             YOnParent, BgCoordX, BgCoordY, RelativeAngle)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
        return self.connection.execute(query, data).lastrowid

class DatabasesMerger:
    def __init__(self, write, read):
        self.write_db = write
        self.read_db = read

    def convert_plan_id(self, read_id):
        """returns the new id of the plan having given id"""
        return self.plans_id_map[read_id]

    def merge_plans(self):
        """copy all plans from read into write and returns a dictionary mapping ids read -> write"""
        read_plans = self.read_db.get_all_plans()
        existing_plans = self.write_db.get_plans_name()
        for plan_id in read_plans:
            if read_plans[plan_id][1] in existing_plans:
                print('WARNING: Plan "{}" already exists in "{}". Plan is ignored!' \
                      .format(read_plans[plan_id][1], self.write_db.get_db_path()))
            else:
                read_plans[plan_id] = self.write_db.insert_plan(read_plans[plan_id])
        self.plans_id_map = read_plans

    def merge(self):
        # copy all plans and retrieve a dictionary mapping id in read -> id in write
        self.merge_plans()
        # copy all nodes and retrieve a dictionary mapping id in read -> id in write
        # copy all edges from nodes ids
        # copy all aliases if not existing
        # copy then all aliases links
        # copy all wifis (unconditionally)
        pass

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
    db_to_keep_idx = 1 if argv[1] != '-r' else 2
    path_db_to_keep = argv[db_to_keep_idx]
    paths_dbs_to_copy = argv[db_to_keep_idx+1:]

    try:
        writeable_db = WritableDatabase(path_db_to_keep)
        print('Creating backup of {}'.format(path_db_to_keep))
        copyfile(path_db_to_keep, 'BACKUP-'+path_db_to_keep)
        for path in paths_dbs_to_copy:
            try:
                read_database = ReadableDatabase(path)
                DatabasesMerger(writeable_db, read_database).merge()
                read_database.close()
            except ValueError as e:
                print(e)
        writeable_db.commit()
        writeable_db.close()
        print('database {} has been copied in {}'.format(path, path_db_to_keep))
    except ValueError as e:
        print(e)

if __name__ == '__main__':
    main()
