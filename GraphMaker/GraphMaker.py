#!/usr/bin/python3

import tkinter as t
from tkinter import filedialog as fdialog
from PIL import Image, ImageTk
from time import time, sleep
from os import system

class AP:

    def __init__(self, key):
        self.key = key
        self.values = []

    def avg (self):
        res = 0
        for elem in self.values:
            res += elem
        return res/len(self.values)

    def add (self, dbm):
        self.values.append(dbm)

    def text (self):
        return '<wifi BSS="'+self.key+'" max="'+str(abs(min(self.values)))+'" min="'+str(abs(max(self.values)))+'" avg="'+str(abs(self.avg()))+'"/wifi>'

class AccesPointList:

    def __init__(self, network = "wlp3s0", tmpfile = "temp.txt", iterations = 5, wait = 2):
        self.network = network
        self.tmpfile = tmpfile
        self.iter = iterations
        self.wait = wait
        self.elements = []

    def text (self):
        output = '<listWifi>\n'
        for elem in self.elements:
            output += elem.text()+'\n'
        output += '</listWifi>'
        return output

    def findAP (self, key):
        for elem in self.elements:
            if (key == elem.key):
                return elem
        return None

    def extractData (self, lines):
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

    def scan (self):
        for i in range(self.iter):
            system("iw dev "+self.network+" scan > "+self.tmpfile)
            file = open(self.tmpfile)
            lines = file.readlines()
            file.close()
            self.extractData(lines)
            sleep(self.wait)

# À utiliser de la manière suivante :
#    tmp = AccesPointList()
#    tmp.scan()
#    tmp.text()

class Node:
    def __init__(self, name, coords):
        self.name_ = name
        self.coords = coords

    def coord(self, c=None):
        if c is None:
            return self.coords
        else:
            self.coords = c

    def name(self, n=None):
        if n is None:
            return self.name_
        else:
            self.name_ = n

class Edge:
    def __init__(self, weight, coords):
        self.weight_ = weight
        self.coords = coords

    def coord(self, c=None):
        if c is None:
            return self.coords
        else:
            self.coords = c

    def weight(self, w=None):
        if w is None:
            return self.weight_
        else:
            self.weight_ = w

'''
    Available operations:
        + left click to create a new node
        + right click on a node to edit it
        + left click on a node + move to create an edge
        + left click on the image + move to move the background
'''
class App(t.Tk):
    ALPHA_INITIAL_VALUE=128
    LEFT_CLICK = '<Button-1>'
    WHEEL_CLICK = '<Button-2>'
    RIGHT_CLICK = '<Button-3>'
    WHEEL_UP = '<Button-4>'
    WHEEL_DOWN = '<Button-5>'
    LEFT_RELEASE = '<ButtonRelease-1>'
    WHEEL_RELEASE = '<ButtonRelease-2>'
    RIGHT_RELEASE = '<ButtonRelease-3>'
    LEFT_CLICK_MOTION = '<B1-Motion>'
    WHEEL_CLICK_MOTION = '<B2-Motion>'
    RIGHT_CLICK_MOTION = '<B3-Motion>'

    CLICK_TIME_SENSIBILITY = 0.1  # maximum time before click and release to be accepted

    NODE_SIZE = 10

    def __init__(self, **options):
        super().__init__()
        self.init_variables()
        self.create_widgets(**options)

    def init_variables(self):
        self.nodes = dict()
        self.edges = dict()
        self.left_moved = False
        self.left_src = None
        self.tmp_line_id = None
        self.cv_image_coord = [0, 0]

    def create_widgets(self, **options):
        self.canvas = t.Canvas(self, width=options['c_width'], height=options['c_height'])
        self.canvas.pack()
        self.chose_background_image()
        self.alpha_scale = t.Scale(self, from_=1, to=255,
            command=lambda v: self.make_bg_image(v), orient=t.HORIZONTAL)
        self.alpha_scale.set(App.ALPHA_INITIAL_VALUE)
        self.alpha_scale.pack()
        self.bind_events()

    def bind_events(self):
        canvas_callbacks = {
            App.LEFT_CLICK: self.handle_left_click,
            App.WHEEL_CLICK: self.handle_wheel_click,
            App.RIGHT_CLICK: self.handle_right_click,
            App.WHEEL_DOWN: self.handle_wheel_down,
            App.WHEEL_UP: self.handle_wheel_up,
            App.LEFT_RELEASE: self.handle_left_release,
            App.RIGHT_RELEASE: self.handle_right_release,
            App.WHEEL_RELEASE: self.handle_wheel_release,
            App.LEFT_CLICK_MOTION: self.handle_left_click_mvt,
            App.RIGHT_CLICK_MOTION: self.handle_right_click_mvt,
            App.WHEEL_CLICK_MOTION: self.handle_wheel_click_mvt}
        # bind canvas events
        for event in canvas_callbacks:
            self.canvas.bind(event, canvas_callbacks[event])

    def chose_background_image(self):
        self.background_file_name = t.filedialog.askopenfilename()
        self.bg_template = Image.open(self.background_file_name)
        self.bg_image_size = self.bg_template.size
        self.make_bg_image(App.ALPHA_INITIAL_VALUE)

    def make_bg_image(self, alpha):
        self.bg_template.putalpha(int(alpha))
        self.bg_image = ImageTk.PhotoImage(self.bg_template)
        if not hasattr(self, 'cv_image_id'):
            self.cv_image_id = self.canvas.create_image(self.cv_image_coord[0],
                self.cv_image_coord[1], image=self.bg_image, anchor='nw')
        else:
            self.canvas.itemconfig(self.cv_image_id, image=self.bg_image)

    def add_node(self, x, y):
        node_coord = x-App.NODE_SIZE, y-App.NODE_SIZE, x+App.NODE_SIZE, y+App.NODE_SIZE
        node_id = self.canvas.create_oval(*node_coord, fill='red')
        # print('Adding new node with id', new_id)
        self.nodes[node_id] = Node(self.configure_node(), self.canvas.coords(node_id))

    # events handling code

    def get_selected_el(self, x, y, d=3):
        tmp = self.canvas.find_overlapping(x-d, y-d, x+d, y+d)
        i = 1
        while i < len(tmp) and (tmp[i] <= self.cv_image_id or tmp[i] == self.tmp_line_id):
            i += 1
        try:
            return tmp[i]
        except:
            return None

    def handle_left_click_mvt(self, ev):
        self.left_moved = True
        if self.left_src is not None and self.tmp_line_id is not None:
            current_coords = self.canvas.coords(self.tmp_line_id)
            self.canvas.coords(self.tmp_line_id, current_coords[0], current_coords[1], ev.x, ev.y)
        else:
            self.cv_image_new_coord = [self.cv_image_coord[0]+ev.x-self.click_coord[0], self.cv_image_coord[1]+ev.y-self.click_coord[1]]
            self.check_image_coords(self.cv_image_new_coord)
            self.canvas.coords(self.cv_image_id, *self.cv_image_new_coord)
            x_offset, y_offset = (self.cv_image_new_coord[i]-self.cv_image_coord[i] for i in range(2))
            for edge_id in self.edges:
                self.canvas.coords(edge_id, self.edges[edge_id].coord()[0]+x_offset, self.edges[edge_id].coord()[1]+y_offset,
                    self.edges[edge_id].coord()[2]+x_offset, self.edges[edge_id].coord()[3]+y_offset)
            for node_id in self.nodes:
                self.canvas.coords(node_id, self.nodes[node_id].coord()[0]+x_offset, self.nodes[node_id].coord()[1]+y_offset,
                    self.nodes[node_id].coord()[2]+x_offset, self.nodes[node_id].coord()[3]+y_offset)

    def verify_in(self, coords):
        return 0 >= coords[0] >= int(self.canvas['width'])-self.bg_image_size[0] and \
               0 >= coords[1] >= int(self.canvas['height'])-self.bg_image_size[1]

    def check_image_coords(self, coords):
        if coords[0] > 0:
            coords[0] = 0
        elif coords[0] < int(self.canvas['width']) - self.bg_image_size[0]:
            coords[0] = int(self.canvas['width']) - self.bg_image_size[0]
        if coords[1] > 0:
            coords[1] = 0
        elif coords[1] < int(self.canvas['height']) - self.bg_image_size[1]:
            coords[1] = int(self.canvas['height']) - self.bg_image_size[1]

    def handle_right_click_mvt(self, ev):
        print('RC MOVING')

    def handle_wheel_click_mvt(self, ev):
        print('WC MOVING')

    def handle_left_click(self, ev):
        self.left_src = self.get_selected_el(ev.x, ev.y)
        if self.left_src is not None:
            self.initial_click_coord = [self.nodes[self.left_src].coord()[0]+App.NODE_SIZE,
                self.nodes[self.left_src].coord()[1]+App.NODE_SIZE]
            self.tmp_line_id = self.canvas.create_line(*self.initial_click_coord,
                *self.initial_click_coord, width=2.5)
        else:
            self.click_coord = [ev.x, ev.y]
        self.left_click_time = time()

    def handle_wheel_click(self, ev):
        selected = self.get_selected_el(ev.x, ev.y)
        if selected is None:
            return
        if selected in self.nodes:
            node_center = self.nodes[selected].coord()[:2]
            node_center = [c+App.NODE_SIZE for c in node_center]
            self.canvas.delete(selected)
            for edge_id in self.edges:
                if node_center in (self.edges[edge_id].coord()[:2], self.edges[edge_id].coord()[2:4]):
                    self.canvas.delete(edge_id)
        elif selected in self.edges:
            self.canvas.delete(selected)
            del self.edges[selected]
        else:
            print('\t\tERROR')

    def handle_right_click(self, ev):
        selected = self.get_selected_el(ev.x, ev.y)
        if selected is None:
            return
        if selected in self.nodes:
            self.nodes[selected].name(self.configure_node(self.nodes[selected].name()))
        elif selected in self.edges:
            self.edges[selected].weight(self.configure_edge(self.edges[selected].weight()))
        else:
            print('\t\tERROR')

    def handle_wheel_up(self, ev):
        print('WU')

    def handle_wheel_down(self, ev):
        print('WD')

    def handle_left_release(self, ev):
        if self.left_moved:
            if self.tmp_line_id is not None:
                end = self.get_selected_el(ev.x, ev.y)
                self.canvas.delete(self.tmp_line_id)
                self.tmp_line_id = None
                if end is not None:
                    # to do, store the link somewhere to be saved in file
                    edge_id = self.canvas.create_line(*self.initial_click_coord,
                        self.nodes[end].coord()[0]+App.NODE_SIZE, self.nodes[end].coord()[1]+App.NODE_SIZE,
                        width=2.5)
                    self.edges[edge_id] = Edge(self.configure_edge(), self.canvas.coords(edge_id))
            else:
                self.cv_image_coord = self.canvas.coords(self.cv_image_id)
                for node_id in self.nodes:
                    self.nodes[node_id].coord(self.canvas.coords(node_id))
                for edge_id in self.edges:
                    self.edges[edge_id].coord(self.canvas.coords(edge_id))
        elif float(time() - self.left_click_time) <= App.CLICK_TIME_SENSIBILITY:
            self.add_node(ev.x, ev.y)
        self.left_moved = False

    def handle_right_release(self, ev):
        print('RR')

    def handle_wheel_release(self, ev):
        print('WR')

    def configure_node(self, current_name=''):
        toplevel = t.Toplevel(self)
        t.Label(toplevel, text='Node Name: ').grid(row=0, column=0)
        name = t.StringVar()
        name.set(current_name)
        t.Entry(toplevel, textvariable=name).grid(row=0, column=1)
        t.Button(toplevel, text='Ok', command=toplevel.destroy).grid(row=1)
        toplevel.wait_window()
        return name.get()

    def configure_edge(self, current_weight=''):
        toplevel = t.Toplevel(self)
        t.Label(toplevel, text='Edge Weight').grid(row=0, column=0)
        value = t.StringVar()
        value.set(str(current_weight))
        t.Entry(toplevel, textvariable=value).grid(row=0, column=1)
        t.Button(toplevel, text='Ok', command=toplevel.destroy).grid(row=1)
        toplevel.wait_window()
        return float(value.get())

def main():
    app = App(c_width=400, c_height=400)
    app.mainloop()

if __name__ == '__main__':
    main()
