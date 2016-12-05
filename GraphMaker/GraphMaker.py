#!/usr/bin/python3

# tkinter
from app.general.tkinter_imports import *
from time import time, sleep
# std
from os.path import splitext
from xml.etree import ElementTree
# internal
from app.Config import Config
from app.general.functions import euclidian_distance, purge_plan_name
from app.App import App

def main():
    root = t.Tk()
    root.wm_title("GraphMaker")
    app = App(root, c_width=800, c_height=800)
    app.pack(fill='both', expand='yes')
    root.protocol("WM_DELETE_WINDOW", lambda: (app.on_exit(),root.destroy()))
    root.mainloop()

if __name__ == '__main__':
    print('Be sure to run this script as root to be able to scan for networks')
    main()
