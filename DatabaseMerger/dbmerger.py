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
        self.connection = sqlite3.connect(path)

    def close(self):
        self.connection.close()

class ReadableDatabase(AbstractDatabase):
    def __init__(self, path):
        super().__init__(path)

class WritableDatabase(ReadableDatabase):
    def __init__(self, path):
        super().__init__(path)

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
                read_database.close()
            except ValueError as e:
                print(e)
        writeable_db.close()
        print('database {} has been copied in {}'.format(path, path_db_to_keep))
    except ValueError as e:
        print(e)

if __name__ == '__main__':
    main()
