#!/usr/bin/python3

# tkinter
from app.general.tkinter_imports import *
from time import time, sleep
# std
from os import getuid
from os.path import splitext
from xml.etree import ElementTree
from sys import argv
# internal
from app.Config import Config
from app.general.functions import euclidian_distance, purge_plan_name
from app.App import App

def main():
    root = t.Tk()
    root.wm_title("GraphMaker")
    
    try:
        app = App(root, c_width=800, c_height=800)
    except IOError as e:
        print("Stop application: " + str(e))
    else:
        app.pack(fill='both', expand='yes')
        root.protocol("WM_DELETE_WINDOW", lambda: (app.on_exit(), root.destroy()))
        root.mainloop()

if __name__ == '__main__':
    if getuid() == 0 or (len(argv) > 1 and argv[1] == "--force"):
        main()
    else:
        print('GraphMaker must be start with root privileges (to be able to scan for networks)')
        print('To execute anyway, add `--force`')
