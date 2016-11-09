#!/usr/bin/python3

import tkinter as t
from tkinter import filedialog as fdialog
from tkinter import messagebox as mbox
from PIL import Image, ImageTk
from time import time, sleep
from os import system, remove
from os.path import relpath, splitext
from xml.etree import ElementTree
from math import sqrt

SET_TABS_IN_XML = True
TAB = {True: '\t', False: ''}[SET_TABS_IN_XML]

class AP:
    def __init__(self, key):
        self.key = key
        self.values = []

    def avg (self):
        return sum(self.values)/len(self.values)

    def add (self, dbm):
        self.values.append(dbm)

    def text (self):
        return '<wifi BSS="{}" max="{:2.1f}" min="{:2.1f}" avg="{:2.1f}" />' \
               .format(self.key, -min(self.values), -max(self.values), -self.avg())

class AccessPointList:
    def __init__(self, network = "wlp3s0", tmpfile = "temp.txt", iterations = 5, wait = 2):
        self.network = network
        self.tmpfile = tmpfile
        self.iters = iterations
        self.wait = wait
        self.elements = []

    def text (self, nb_tab=0):
        output = (TAB * nb_tab) + '<listWifi>\n'
        for elem in self.elements:
            output += (TAB * (nb_tab+1)) + elem.text()+'\n'
        output += (TAB * nb_tab) + '</listWifi>'
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
        cmd = "iw dev {}  scan > {}".format(self.network, self.tmpfile)
        for i in range(self.iters):
            system(cmd)
            with open(self.tmpfile) as file:
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
        output += (TAB * nb_tab) + '</listWifi>'
        return output

# À utiliser de la manière suivante :
#    tmp = AccessPointList()
#    tmp.scan()
#    tmp.text()

class Node:
    def __init__(self, name, coords, access_points):
        self.name_ = name
        self.coords = coords
        self.access_points_ = access_points
        self.color = 'green' if access_points is not None else 'red'

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

    def access_points(self, ap=None):
        if ap is None:
            return self.acces_points_
        else:
            self.access_points_ = ap

    # TODO: handle aliases
    def text(self, nb_tab=0):
        text = (TAB * (nb_tab+1)) + '<coord x="{}" y="{}" />\n'.format(*self.coord())
        if self.access_points_ is not None:
            text += self.access_points_.text(nb_tab+1)
        return '{0}<point id="{1}">\n{2}\n{0}</point>\n'.format(TAB*nb_tab, self.name(), text)

class Edge:
    def __init__(self, weight, coords, extremity_ids):
        self.weight_ = weight
        self.coords = coords
        self.extremity_ids = extremity_ids

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

    def text(self, nb_tab=0):
        return (TAB*nb_tab) + '<edge beg="{}" end="{}" weight="{}" />\n'.format(*self.extremity_ids, self.weight())

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
    EDGE_WIDTH = 2.5

    NETWORK_ID = 'wlp2s0'  # Change this according to your network device id

    def __init__(self, **options):
        super().__init__()
        self.init_variables()
        self.create_widgets(**options)
        self.protocol("WM_DELETE_WINDOW", self.on_exit)

    def on_exit(self):
        if mbox.askquestion('Quit', 'Do you want to save before leaving?') == 'yes':
            self.save_to_xml(fdialog.asksaveasfilename(defaultextension='xml', filetypes=[('XML Files', '.xml')], initialdir='./'))
        self.destroy()

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
        self.open_file()  # self.chose_background_image()
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

    def open_file(self):
        self.file_name = t.filedialog.askopenfilename(initialdir='../plans/')
        ext = splitext(self.file_name)[1].lower()[1:]
        if ext == 'xml':
            self.load_xml()
        elif ext in ['bmp', 'jpg', 'jpe', 'jpeg', 'png', 'tif', 'tiff']:
            self.metre_length_on_plan = self.ask_metre_length()
            print('One metre is then {} pixels'.format(self.metre_length_on_plan))
            self.background_file_name = self.file_name
            self.chose_background_image()

    def ask_metre_length(self):
        toplevel = t.Toplevel(self)
        nb_pixels = t.StringVar()
        t.Label(toplevel, text='Enter length (in pixels) of a meter on the given plan: ').grid(row=0, column=0)
        t.Entry(toplevel, textvariable=nb_pixels).grid(row=0, column=1)
        t.Button(toplevel, text='Ok', command=toplevel.destroy).grid(row=1)
        toplevel.wait_window()
        return int(nb_pixels.get())

    def chose_background_image(self):
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

    def create_node(self, x, y):
        name, access_points = self.configure_node()
        if name == '' or name in [self.nodes[n].name() for n in self.nodes]:
            return
        node_coord = x-App.NODE_SIZE, y-App.NODE_SIZE, x+App.NODE_SIZE, y+App.NODE_SIZE
        node_id = self.canvas.create_oval(*node_coord, fill='green' if access_points is not None else 'red')
        self.add_node(name, node_id, access_points)

    def add_node(self, name, node_id, access_points):
        self.nodes[node_id] = Node(name, self.canvas.coords(node_id), access_points)

    def add_edge(self, weight, edge_id, extremities):
        self.edges[edge_id] = Edge(weight, self.canvas.coords(edge_id), extremities)

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
            _ = self.initial_click_coord + self.initial_click_coord
            self.tmp_line_id = self.canvas.create_line(*_, width=App.EDGE_WIDTH)
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
                if selected in self.edges[edge_id].extreimity_ids:
                    self.canvas.delete(edge_id)
            del self.nodes[selected]
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
            name, access_points = self.configure_node(self.nodes[selected].name())
            self.nodes[selected].name(name)
            if access_points is not None:
                self.nodes[selected].access_points(access_points)
                self.color = 'green'
                self.canvas.itemconfig(selected, fill='green')
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
                    node_coord = self.nodes[end].coord()
                    final_point = [node_coord[i]+App.NODE_SIZE for i in range(2)]
                    distance = App.dist(self.initial_click_coord, final_point)
                    try:
                        weight = self.configure_edge(distance/self.metre_length_on_plan)
                    except:
                        return
                    edge_id = self.canvas.create_line(*self.initial_click_coord,
                        self.nodes[end].coord()[0]+App.NODE_SIZE, self.nodes[end].coord()[1]+App.NODE_SIZE,
                        width=2.5)
                    extremity_ids = (self.nodes[self.get_selected_el(*self.initial_click_coord)].name(), self.nodes[end].name())
                    self.add_edge(weight, edge_id, extremity_ids)
            else:
                self.cv_image_coord = self.canvas.coords(self.cv_image_id)
                for node_id in self.nodes:
                    self.nodes[node_id].coord(self.canvas.coords(node_id))
                for edge_id in self.edges:
                    self.edges[edge_id].coord(self.canvas.coords(edge_id))
        elif float(time() - self.left_click_time) <= App.CLICK_TIME_SENSIBILITY:
            self.create_node(ev.x, ev.y)
        self.left_moved = False

    def handle_right_release(self, ev):
        print('RR')

    def handle_wheel_release(self, ev):
        print('WR')

    def configure_node(self, current_name=''):
        self.toplevel = t.Toplevel(self)
        t.Label(self.toplevel, text='Node Name: ').grid(row=0, column=0)
        name = t.StringVar()
        name.set(current_name)
        t.Entry(self.toplevel, textvariable=name).grid(row=0, column=1)
        t.Button(self.toplevel, text='Ok', command=self.toplevel.destroy).grid(row=1)
        self.ap = None
        t.Button(self.toplevel, text='Scan access points', command=self.scan).grid(row=1, column=1)
        self.toplevel.wait_window()
        ap = self.ap
        del self.ap
        return name.get(), ap

    def scan(self):
        self.toplevel.wm_title('Scanning access points...')
        self.ap = AccessPointList(network=App.NETWORK_ID, iterations=2)
        self.ap.scan()
        self.toplevel.wm_title('access points scanned')

    def configure_edge(self, current_weight=''):
        toplevel = t.Toplevel(self)
        t.Label(toplevel, text='Edge Weight').grid(row=0, column=0)
        value = t.StringVar()
        value.set('{:.2f}'.format(current_weight))
        t.Entry(toplevel, textvariable=value).grid(row=0, column=1)
        t.Button(toplevel, text='Ok', command=toplevel.destroy).grid(row=1)
        toplevel.wait_window()
        return float(value.get())

    # Save functions

    def text(self, nb_tab=0):
        text = (TAB * (nb_tab+1)) + '<background_image path="{}" coord="{}" />\n'.format(relpath(self.background_file_name), tuple(self.cv_image_coord))
        text += (TAB * (nb_tab+1)) + '<distance_unit value="{}" />\n'.format(self.metre_length_on_plan)
        plan_name = 'XXX'
        for node_id in self.nodes:
            text+= self.nodes[node_id].text(nb_tab+1)
        for edge_id in self.edges:
            text += self.edges[edge_id].text(nb_tab+1)
        return '<plan nom="{}">\n{}\n</plan>\n'.format(plan_name, text)

    def save_to_xml(self, path):
        content = self.text()
        with open(path, 'w') as save_file:
            save_file.write(content)

    def load_xml(self):
        xml_tree = ElementTree.parse(self.file_name)
        root = xml_tree.getroot()
        self.metre_length_on_plan = int(root.find('distance_unit').get('value'))
        bg_image = root.find('background_image')
        self.background_file_name = bg_image.get('path')
        self.chose_background_image()
        self.cv_image_coord = [float(value.strip()) for value in bg_image.get('coord')[1:-1].split(',')]
        self.canvas.coords(self.cv_image_id, *self.cv_image_coord)
        self.load_points(xml_tree)
        self.load_edges(xml_tree)

    def load_points(self, xml_tree):
        for point in xml_tree.findall('point'):
            coord = point.find('coord')
            x, y = float(coord.get('x')), float(coord.get('y'))
            coord = x-App.NODE_SIZE, y-App.NODE_SIZE, x+App.NODE_SIZE, y+App.NODE_SIZE
            listWifi = point.find('listWifi')
            if listWifi is None:
                access_points = None
            else:
                access_points = StaticAccessPointList()
                access_points.fromXml(listWifi)
            node_id = self.canvas.create_oval(*coord, fill='green' if access_points is not None else 'red')
            self.add_node(point.attrib['id'], node_id, access_points)

    def load_edges(self, xml_tree):
        for edge in xml_tree.findall('edge'):
            extremities = edge.get('beg'), edge.get('end')
            extremities_ids = [[node_id for node_id in self.nodes \
                if self.nodes[node_id].name() == extremity][0] for extremity in extremities]
            end_coord = [c + App.NODE_SIZE for c in self.nodes[extremities_ids[1]].coord()[:2]]
            beg_coord = [c + App.NODE_SIZE for c in self.nodes[extremities_ids[0]].coord()[:2]]
            edge_id = self.canvas.create_line(*beg_coord, *end_coord, width=App.EDGE_WIDTH)
            self.add_edge(float(edge.get('weight')), edge_id, extremities)

    @staticmethod
    def dist(a, b):
        return sqrt((a[0]-b[0])**2 + (a[1]-b[1])**2)

def main():
    app = App(c_width=800, c_height=800)
    app.mainloop()

if __name__ == '__main__':
    print('Be sure to run this script as root to be able to scan for networks')
    main()