#!/usr/bin/python3

# tkinter
from app.general.tkinter_imports import *
from time import time, sleep
# std
from os import system, remove
from os.path import splitext
from xml.etree import ElementTree
# internal
from app.Config import Config
from app.general.functions import euclidian_distance, purge_plan_name
from app.App import App

class AP:
    def __init__(self, key):
        self.key = key
        self.values = list()

    def avg (self):
        return sum(self.values)/len(self.values)

    def add(self, dbm):
        self.values.append(dbm)

    def text(self):
        return '<wifi BSS="{}" max="{:2.1f}" min="{:2.1f}" avg="{:2.1f}" />' \
               .format(self.key, -min(self.values), -max(self.values), -self.avg())

class AccessPointList:
    def __init__(self, tmpfile = "temp.txt", iterations = 5, wait = 2):
        self.network = Config.NETWORK_INTERFACE
        self.tmpfile = tmpfile
        self.iters = iterations
        self.wait = wait
        self.elements = []

    def text(self, nb_tab=0):
        output = (TAB * nb_tab) + '<listWifi>\n'
        for elem in self.elements:
            output += (TAB * (nb_tab+1)) + elem.text()+'\n'
        output += (TAB * nb_tab) + '</listWifi>\n'
        return output

    def findAP(self, key):
        for elem in self.elements:
            if (key == elem.key):
                return elem
        return None

    def extractData(self, lines):
        key = ""
        for line in lines:
            line = line.strip()
            if (line[:3] == "BSS" and line[:8] != "BSS Load"):
                key = line[4:21]
                if (self.findAP(key) == None):
                    self.elements.append(AP(key))
            elif (line[:6] == "signal"):
                tmp = line[8:]
                tmp = float(tmp[:len(tmp)-4])
                elem = self.findAP(key)
                elem.add(tmp)
                print(key + " signal found with " + str(tmp))

    def scan(self):
        cmd = "iw dev {}  scan > {}".format(self.network, self.tmpfile)
        for i in range(self.iters):
            system(cmd)
            with open(self.tmpfile) as file:
                print("Test " + str(i))
                self.extractData(file.readlines())
            sleep(self.wait)
        remove(self.tmpfile)


class StaticAccessPointList:
    def fromXml(self, xml_tree):
        self.elements = list()
        for wifi in xml_tree.iter('wifi'):
            self.elements.append(wifi)

    def text(self, nb_tab=0):
        output = (TAB * nb_tab) + '<listWifi>\n'
        for elem in self.elements:
            _ = '<wifi BSS="{}" max="{}" min="{}" avg="{}" />' \
                .format(elem.get('BSS'), elem.get('max'), elem.get('min'), elem.get('avg'))
            output += (TAB * (nb_tab+1)) + _ + '\n'
        output += (TAB * nb_tab) + '</listWifi>\n'
        return output

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
