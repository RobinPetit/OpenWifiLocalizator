from app.Config import Config
from os import system, remove
from time import sleep

from app.general.constants import *

class AP:
    def __init__(self, bss, variance=.0, avg=None):
        self.key = bss
        self.values = list()
        self.variance = variance
        self.avg = avg

    def __len__(self):
        return len(self.values)

    def get_avg(self):
        if self.avg == None:
            self.avg = sum(self.values)/len(self)
        return self.avg

    def add(self, dbm):
        self.values.append(dbm)

    def get_bss(self):
        return self.key

    def get_variance(self):
        if (self.variance == .0):
            for i in range(len(self)):
                self.variance += (self.values[i]-self.get_avg())**2
            self.variance /= (len(self)-1)
        return self.variance

class AccessPointList:
    def __init__(self, tmpfile = "temp.txt", iterations = 5, wait = 2, threshold = 1):
        self.threshold = threshold
        self.network = Config.NETWORK_INTERFACE
        self.tmpfile = tmpfile
        self.iters = iterations
        self.wait = wait
        self.elements = []
        
    def __iter__(self):
        """iterates over wifis, checking on a threshold to avoir irrelevant access points"""
        for e in self.elements:
            if len(e) > self.threshold:
                yield e

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

