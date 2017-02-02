from app.Config import Config
from os import system, remove
from time import sleep

from app.general.constants import *

class AP:
    def __init__(self, bss, variance=.0, avg=.0):
        self.key = bss
        self.values = list()
        self.variance = variance
        self.avg = avg

    def k():
        return len(self.values)

    def avg (self):
        if (self.avg == .0):
            self.avg = sum(self.values)/self.k()
        return self.avg

    def add(self, dbm):
        self.values.append(dbm)

    def text(self):
        return '<wifi BSS="{}" max="{:2.1f}" min="{:2.1f}" avg="{:2.1f}" />' \
               .format(self.key, -min(self.values), -max(self.values), -self.avg())

    def get_bss(self):
        return self.key

    def get_min(self):
        return min(self.values)

    def get_max(self):
        return max(self.values)

    def get_variance(self):
        if (self.variance == .0):
            for i in range(self.k()):
                self.variance += (self.values[i]-self.avg())**2
            self.variance *= 1/(self.k()-1)
        return self.variance

    def sql(self):
        # @TODO format looks awful
        res = "INSERT INTO Wifi (Bss,NodeId,Min,Max,Avg) VALUES('{0}',{1},{2},{3},{4})"
        return res.format(self.key, "{0}", -min(self.values), -max(self.values), -self.avg())

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

    def sql(self):
        output = ""
        n = len(self.elements)
        for i in range(n):
            output += (self.elements[i]).sql()
            if (i < n-1):
                output += ";"
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
        # @TODO
        output = (TAB * nb_tab) + '<listWifi>\n'
        for elem in self.elements:
            _ = '<wifi BSS="{}" max="{}" min="{}" avg="{}" />' \
                .format(elem.get('BSS'), elem.get('max'), elem.get('min'), elem.get('avg'))
            output += (TAB * (nb_tab+1)) + _ + '\n'
        output += (TAB * nb_tab) + '</listWifi>\n'
        return output
