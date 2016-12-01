#!/usr/bin/python3

# tkinter
import tkinter as t
from tkinter import filedialog as fdialog
from tkinter import messagebox as mbox
from PIL import Image, ImageTk
from time import time, sleep
# std
from os import system, remove
from os.path import relpath, splitext
from xml.etree import ElementTree
from math import sqrt
# internal
from Config import Config

SET_TABS_IN_XML = True
TAB = {True: '\t', False: ''}[SET_TABS_IN_XML]

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

def euclidian_distance(a, b):
    return sqrt(sum([(x-y)*(x-y) for x, y in zip(a, b)]))

def purge_plan_name(plan, src):
    return relpath(splitext(plan)[0], src)

class AP:
    def __init__(self, key):
        self.key = key
        self.values = []

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

class Node:
    def __init__(self, name, coords, access_points, aliases=tuple()):
        self.name_ = name
        self.coords = coords
        self.access_points_ = access_points
        self.color = 'green' if access_points is not None else 'red'
        self.aliases_ = list(aliases)

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
            return self.access_points_
        else:
            self.access_points_ = ap

    def aliases(self, a=None):
        if a is None:
            return self.aliases_
        else:
            self.aliases_ = list(a)

    def text(self, nb_tab=0):
        text = (TAB * (nb_tab+1)) + '<coord x="{}" y="{}" />\n'.format(*self.coord())
        if self.access_points() is not None:
            text += self.access_points().text(nb_tab+1)
        if self.aliases() and len(self.aliases()) > 0:
            text += (TAB*(nb_tab+1)) + '<aliases>\n'
            for alias in self.aliases():
                text += (TAB*(nb_tab+2)) + '<alias>{}</alias>\n'.format(alias)
            text += (TAB*(nb_tab+1)) + '</aliases>\n'
        return '{0}<node id="{1}">\n{2}{0}</node>\n'.format(TAB*nb_tab, self.name(), text)

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
        return (TAB*nb_tab) + '<edge beg="{}" end="{}" weight="{}" />\n' \
                              .format(*self.extremity_ids, self.weight())

class PlanData:
    def __init__(self):
        self.nodes = dict()
        self.internal_edges = dict()
        self.external_edges = list()
        self.bg_image = None

    def add_node(self, node_id, node):
        assert isinstance(node, Node)
        self.nodes[node_id] = node

    def add_edge(self, edge_id, edge):
        assert isinstance(edge, Edge)
        self.internal_edges[edge_id] = edge

    def add_external_edge(self, internal_node, plan_name, external_node):
        self.external_edges.append((internal_node, plan_name, external_node))

    def set_bg_image(self, bg_image):
        self.bg_image = bg_image

    def get_nodes(self):
        return self.nodes

    def get_edges(self):
        return self.internal_edges

    def get_external_edges(self):
        return self.external_edges

class GraphCanvas(t.Canvas):
    NODE_SIZE = 10
    EDGE_WIDTH = 2.5

    def __init__(self, master, **options):
        super().__init__(master, **options)
        self.plan_data = PlanData()
        self.left_moved = False
        self.cv_image_coord = [0, 0]
        self.px_p_m = 0
        self.bind_events()

    def bind_events(self):
        callbacks = {
            LEFT_RELEASE: self.handle_left_release,
            LEFT_CLICK: self.handle_left_click,
            LEFT_CLICK_MOTION: self.handle_left_click_mvt
        }
        for event in callbacks:
            self.bind(event, callbacks[event])

    def add_node(self, name, node_id, access_points, aliases=tuple()):
        node = Node(name, self.coords(node_id), access_points, aliases)
        self.plan_data.add_node(node_id, node)

    def add_edge(self, weight, edge_id, extremities):
        edge = Edge(weight, self.coords(edge_id), extremities)
        self.plan_data.add_edge(edge_id, edge)

    def refresh(self):
        pass

    def get_selected_el(self, x, y, d=3):
        tmp = self.find_overlapping(x-d, y-d, x+d, y+d)
        i = 1
        while i < len(tmp) and tmp[i] <= self.cv_image_id:
            i += 1
        try:
            return tmp[i]
        except:
            return None

    def check_image_coords(self, coords):
        if coords[0] > 0:
            coords[0] = 0
        elif coords[0] < int(self.width()) - self.bg_template.size[0]:
            coords[0] = int(self.width()) - self.bg_template.size[0]
        if coords[1] > 0:
            coords[1] = 0
        elif coords[1] < int(self.height()) - self.bg_template.size[1]:
            coords[1] = int(self.height()) - self.bg_template.size[1]

    # data setters

    def set_bg_coord(self, coord):
        self.cv_image_coord = coord[:]
        self.coords(self.cv_image_id, *self.cv_image_coord)

    def set_bg_image(self, alpha, image_path=None):
        if not hasattr(self, 'bg_template'):
            if image_path is None:
                raise ValueError('first call to `EditableGraphCanvas.set_bg_image` '
                                 'needs to precise a non-`None` image')
        if image_path is not None:
            assert isinstance(image_path, str)
            self.bg_template = Image.open(image_path)
        self.bg_template.putalpha(int(alpha))
        self.bg_image = ImageTk.PhotoImage(self.bg_template)
        if not hasattr(self, 'cv_image_id'):
            self.cv_image_id = self.create_image(self.cv_image_coord[0],
                self.cv_image_coord[1], image=self.bg_image, anchor='nw')
        else:
            self.itemconfig(self.cv_image_id, image=self.bg_image)

    def set_pixels_per_metre(self, px_p_m):
        assert isinstance(px_p_m, int)
        self.px_p_m = px_p_m

    # data getters

    def image_coord(self):
        return self.cv_image_coord

    def width(self):
        return self['width']

    def height(self):
        return self['height']

    def nodes(self):
        return self.plan_data.get_nodes()

    def edges(self):
        return self.plan_data.get_edges()

    def external_edges(self):
        return self.plan_data.get_external_edges()

    def get_pixels_per_metre(self):
        return self.px_p_m

    # events

    def handle_left_click(self, ev):
        self.click_coord = [ev.x, ev.y]
        self.left_click_time = time()

    def handle_left_release(self, ev):
        ret = False
        if self.left_moved:
            ret = True
            self.cv_image_coord = self.coords(self.cv_image_id)
            for node_id in self.nodes():
                self.nodes()[node_id].coord(self.coords(node_id))
            for edge_id in self.edges():
                self.edges()[edge_id].coord(self.coords(edge_id))
        self.left_moved = False
        return ret

    def handle_left_click_mvt(self, ev):
        self.left_moved = True
        self.cv_image_new_coord = [self.cv_image_coord[0]+ev.x-self.click_coord[0], self.cv_image_coord[1]+ev.y-self.click_coord[1]]
        self.check_image_coords(self.cv_image_new_coord)
        self.coords(self.cv_image_id, *self.cv_image_new_coord)
        x_offset, y_offset = (self.cv_image_new_coord[i]-self.cv_image_coord[i] for i in range(2))
        for edge_id in self.edges():
            self.coords(edge_id, self.edges()[edge_id].coord()[0]+x_offset, self.edges()[edge_id].coord()[1]+y_offset,
                        self.edges()[edge_id].coord()[2]+x_offset, self.edges()[edge_id].coord()[3]+y_offset)
        for node_id in self.nodes():
            self.coords(node_id, self.nodes()[node_id].coord()[0]+x_offset, self.nodes()[node_id].coord()[1]+y_offset,
                        self.nodes()[node_id].coord()[2]+x_offset, self.nodes()[node_id].coord()[3]+y_offset)

    # XML loading

    def load_xml(self, path):
        xml_tree = ElementTree.parse(path)
        root = xml_tree.getroot()
        self.set_pixels_per_metre(int(root.find('distance_unit').get('value')))
        bg_image = root.find('background_image')
        background_file_name = Config.MAPS_PATH + root.get('name') + '.png'
        self.set_bg_image(App.ALPHA_INITIAL_VALUE, background_file_name)
        self.set_bg_coord([float(value.strip()) for value in bg_image.get('coord')[1:-1].split(',')])
        self.load_nodes(root.find('nodes'))
        self.load_edges(root.find('edges'))

    def load_nodes(self, xml_tree):
        for point in xml_tree.findall('node'):
            coord = point.find('coord')
            x, y = float(coord.get('x')), float(coord.get('y'))
            coord = x, y, x+2*GraphCanvas.NODE_SIZE, y+2*GraphCanvas.NODE_SIZE
            listWifi = point.find('listWifi')
            if listWifi is None:
                access_points = None
            else:
                access_points = StaticAccessPointList()
                access_points.fromXml(listWifi)
            node_id = self.create_oval(*coord, fill='green' if access_points is not None else 'red')
            aliases = point.find('aliases')
            if aliases is not None:
                loaded_aliases = list()
                for alias in aliases.findall('alias'):
                    loaded_aliases.append(alias.text)
            else:
                loaded_aliases = list()
            self.add_node(point.attrib['id'], node_id, access_points, loaded_aliases)


    def load_edges(self, xml_tree):
        internal_edge = xml_tree.find('internal')
        for edge in internal_edge.findall('edge'):
            extremities = edge.get('beg'), edge.get('end')
            extremities_ids = [[node_id for node_id in self.nodes() \
                if self.nodes()[node_id].name() == extremity][0] for extremity in extremities]
            end_coord = [c + GraphCanvas.NODE_SIZE for c in self.nodes()[extremities_ids[1]].coord()[:2]]
            beg_coord = [c + GraphCanvas.NODE_SIZE for c in self.nodes()[extremities_ids[0]].coord()[:2]]
            edge_id = self.create_line(*beg_coord, *end_coord, width=GraphCanvas.EDGE_WIDTH)
            self.add_edge(float(edge.get('weight')), edge_id, extremities)

        external_edge = xml_tree.find('external')
        # TODO load external edges

class SelectableGraphCanvas(GraphCanvas):
    def __init__(self, master, **options):
        super().__init__(master, **options)

    def handle_left_release(self, ev):
        if not super().handle_left_release(ev):
            if float(time() - self.left_click_time) <= EditableGraphCanvas.CLICK_TIME_SENSIBILITY:
                element_id = self.get_selected_el(ev.x, ev.y)
                if element_id in self.nodes():
                    self.selected = self.nodes()[element_id].name()
                    self.master.destroy()

    def selected_node_name(self):
        try:
            return self.selected
        except:
            return None

class EditableGraphCanvas(GraphCanvas):
    CLICK_TIME_SENSIBILITY = 0.1  # maximum time before click and release to be accepted

    def __init__(self, master, **options):
        super().__init__(master, **options)
        self.master = master
        self.init_variables()
        self.bind_events()

    def init_variables(self):
        self.left_src = None
        self.tmp_line_id = None

    def bind_events(self):
        canvas_callbacks = {
            LEFT_CLICK: self.handle_left_click,
            WHEEL_CLICK: self.handle_wheel_click,
            RIGHT_CLICK: self.handle_right_click,
            WHEEL_DOWN: self.handle_wheel_down,
            WHEEL_UP: self.handle_wheel_up,
            LEFT_RELEASE: self.handle_left_release,
            RIGHT_RELEASE: self.handle_right_release,
            WHEEL_RELEASE: self.handle_wheel_release,
            LEFT_CLICK_MOTION: self.handle_left_click_mvt,
            RIGHT_CLICK_MOTION: self.handle_right_click_mvt,
            WHEEL_CLICK_MOTION: self.handle_wheel_click_mvt
        }
        # bind canvas events
        for event in canvas_callbacks:
            self.bind(event, canvas_callbacks[event])

    # events handling code

    def handle_left_click_mvt(self, ev):
        self.left_moved = True
        if self.left_src is not None and self.tmp_line_id is not None:
            current_coords = self.coords(self.tmp_line_id)
            self.coords(self.tmp_line_id, current_coords[0], current_coords[1], ev.x, ev.y)
        else:
            self.cv_image_new_coord = [self.cv_image_coord[0]+ev.x-self.click_coord[0], self.cv_image_coord[1]+ev.y-self.click_coord[1]]
            self.check_image_coords(self.cv_image_new_coord)
            self.coords(self.cv_image_id, *self.cv_image_new_coord)
            x_offset, y_offset = (self.cv_image_new_coord[i]-self.cv_image_coord[i] for i in range(2))
            for edge_id in self.edges():
                self.coords(edge_id, self.edges()[edge_id].coord()[0]+x_offset, self.edges()[edge_id].coord()[1]+y_offset,
                            self.edges()[edge_id].coord()[2]+x_offset, self.edges()[edge_id].coord()[3]+y_offset)
            for node_id in self.nodes():
                self.coords(node_id, self.nodes()[node_id].coord()[0]+x_offset, self.nodes()[node_id].coord()[1]+y_offset,
                            self.nodes()[node_id].coord()[2]+x_offset, self.nodes()[node_id].coord()[3]+y_offset)

    def verify_in(self, coords):
        return 0 >= coords[0] >= int(self.width())-self.bg_template.size[0] and \
               0 >= coords[1] >= int(self.height())-self.bg_template.size[1]

    def handle_right_click_mvt(self, ev):
        if Config.DEBUG:
            print('RC MOVING')

    def handle_wheel_click_mvt(self, ev):
        if Config.DEBUG:
            print('WC MOVING')

    def handle_left_click(self, ev):
        self.left_src = self.get_selected_el(ev.x, ev.y)
        if self.left_src is not None:
            self.initial_click_coord = [self.nodes()[self.left_src].coord()[0]+GraphCanvas.NODE_SIZE,
                self.nodes()[self.left_src].coord()[1]+GraphCanvas.NODE_SIZE]
            _ = self.initial_click_coord + self.initial_click_coord
            self.tmp_line_id = self.create_line(*_, width=GraphCanvas.EDGE_WIDTH)
        else:
            self.click_coord = [ev.x, ev.y]
        self.left_click_time = time()

    def handle_wheel_click(self, ev):
        selected = self.get_selected_el(ev.x, ev.y)
        if selected is None:
            return
        if selected in self.nodes():
            node_center = self.nodes()[selected].coord()[:2]
            node_center = [c+GraphCanvas.NODE_SIZE for c in node_center]
            self.delete(selected)
            for edge_id in self.edges():
                if selected in self.edges()[edge_id].extreimity_ids:
                    self.delete(edge_id)
            del self.nodes()[selected]
        elif selected in self.edges():
            self.delete(selected)
            del self.edges()[selected]
        else:
            print('\t\tERROR')

    def handle_right_click(self, ev):
        selected = self.get_selected_el(ev.x, ev.y)
        if selected is None:
            return
        if selected in self.nodes():
            name, access_points, aliases = self.configure_node(self.nodes()[selected].name(), self.nodes()[selected].aliases())
            self.nodes()[selected].name(name)
            self.nodes()[selected].aliases(aliases)
            if access_points is not None:
                self.nodes()[selected].access_points(access_points)
                self.color = 'green'
                self.itemconfig(selected, fill='green')
        elif selected in self.edges():
            self.edges()[selected].weight(self.configure_edge(self.edges()[selected].weight()))
        else:
            print('\t\tERROR')

    def handle_wheel_up(self, ev):
        if Config.DEBUG:
            print('WU')

    def handle_wheel_down(self, ev):
        if Config.DEBUG:
            print('WD')

    def handle_left_release(self, ev):
        if self.left_moved:
            if self.tmp_line_id is not None:
                end = self.get_selected_el(ev.x, ev.y)
                self.delete(self.tmp_line_id)
                self.tmp_line_id = None
                if end is not None:
                    node_coord = self.nodes()[end].coord()
                    final_point = [node_coord[i]+GraphCanvas.NODE_SIZE for i in range(2)]
                    distance = euclidian_distance(self.initial_click_coord, final_point)
                    try:
                        weight = self.configure_edge(distance/self.px_p_m)
                    except Exception as e:
                        print(e)
                        return
                    edge_id = self.create_line(*self.initial_click_coord,
                        self.nodes()[end].coord()[0]+GraphCanvas.NODE_SIZE, self.nodes()[end].coord()[1]+GraphCanvas.NODE_SIZE,
                        width=2.5)
                    extremity_ids = (self.nodes()[self.get_selected_el(*self.initial_click_coord)].name(), self.nodes()[end].name())
                    self.add_edge(weight, edge_id, extremity_ids)
            else:
                self.cv_image_coord = self.coords(self.cv_image_id)
                for node_id in self.nodes():
                    self.nodes()[node_id].coord(self.coords(node_id))
                for edge_id in self.edges():
                    self.edges()[edge_id].coord(self.coords(edge_id))
        elif float(time() - self.left_click_time) <= EditableGraphCanvas.CLICK_TIME_SENSIBILITY:
            self.create_node(ev.x, ev.y)
        self.left_moved = False

    def handle_right_release(self, ev):
        if(Config.DEBUG):
            print('RR')

    def handle_wheel_release(self, ev):
        if(Config.DEBUG):
            print('WR')

    def get_external_node(self, name):
        assert hasattr(self, 'toplevel') and self.toplevel is not None
        plan_path = t.filedialog.askopenfilename(initialdir=Config.XMLS_PATH,
                                                 filetypes=[('XML Files', '.xml')])
        if plan_path == '' or plan_path is None:
            print('ERROR')  # TODO: handle properly with a popup
        node_name = ExternalNodeFinder.find(self.toplevel, plan_path)
        self.create_external_edge(name, purge_plan_name(plan_path, Config.XMLS_PATH), node_name)

    def configure_node(self, current_name='', current_aliases=tuple()):
        # TODO: Refactor this into a brand new class
        self.toplevel = t.Toplevel(self)
        # Name
        t.Label(self.toplevel, text='Node Name: ').grid(row=0, column=0)
        name = t.StringVar()
        name.set(current_name)
        t.Entry(self.toplevel, textvariable=name).grid(row=0, column=1)
        self.aliases = list(current_aliases)
        # Aliases
        self.aliases_group = t.LabelFrame(self.toplevel, text='Aliases Management', padx=5, pady=5, relief=t.SUNKEN, borderwidth=3)
        self.aliases_group.grid(row=1, column=0, columnspan=2)
        self.lb = t.Listbox(self.aliases_group, listvar=self.aliases)
        self.lb.grid(row=1, column=0, rowspan=3)
        for alias in self.aliases:
            self.lb.insert(t.END, alias)
        self.alias = t.StringVar()
        t.Entry(self.aliases_group, textvariable=self.alias).grid(row=1, column=1)
        t.Button(self.aliases_group, text='Add alias', command=lambda: (self.lb.insert(t.END, self.alias.get()), self.aliases.append(self.alias.get()))).grid(row=2, column=1)
        t.Button(self.aliases_group, text='Remove alias', command=lambda: self.lb.delete(t.ANCHOR)).grid(row=3, column=1)
        if current_name != '':
            # External edges
            self.ext_edges_group = t.LabelFrame(self.toplevel, text='External edges', padx=5, pady=5, relief=t.SUNKEN, borderwidth=3)
            self.ext_edges_group.grid(row=4, column=0, columnspan=2)
            t.Button(self.ext_edges_group, text='Add external edge from this node', command=lambda: self.get_external_node(current_name)).grid(row=4, column=0)
        # Validation & scan
        t.Button(self.toplevel, text='Ok', command=self.toplevel.destroy).grid(row=5, column=0)
        self.ap = None
        t.Button(self.toplevel, text='Scan access points', command=self.scan).grid(row=5, column=1)
        self.toplevel.wait_window()
        ap = self.ap
        aliases = self.aliases
        del self.ap
        del self.lb
        del self.alias
        del self.aliases
        del self.toplevel
        return name.get(), ap, aliases

    def configure_edge(self, current_weight=''):
        toplevel = t.Toplevel(self)
        t.Label(toplevel, text='Edge Weight').grid(row=0, column=0)
        value = t.StringVar()
        value.set('{:.2f}'.format(current_weight))
        t.Entry(toplevel, textvariable=value).grid(row=0, column=1)
        t.Button(toplevel, text='Ok', command=toplevel.destroy).grid(row=1)
        toplevel.wait_window()
        return float(value.get())

    def create_node(self, x, y):
        name, access_points, aliases = self.configure_node()
        if name == '' or name in [self.nodes()[n].name() for n in self.nodes()]:
            return
        node_coord = (x-GraphCanvas.NODE_SIZE, y-GraphCanvas.NODE_SIZE,
                      x+GraphCanvas.NODE_SIZE, y+GraphCanvas.NODE_SIZE)
        node_id = self.create_oval(*node_coord, fill='green' if access_points is not None else 'red')
        self.add_node(name, node_id, access_points, aliases)

    def create_external_edge(self, internal_node, plan_name, external_node):
        self.plan_data.add_external_edge(internal_node, plan_name, external_node)

    def scan(self):
        self.toplevel.wm_title('Scanning access points...')
        self.ap = AccessPointList(iterations=5)
        self.ap.scan()
        self.toplevel.wm_title('access points scanned')

class ExternalNodeFinder:
    @staticmethod
    def find(tkinter_master, plan_path):
        top = t.Toplevel(tkinter_master)
        top.title('Select the node to link with current one')
        cv = SelectableGraphCanvas(top)
        cv.load_xml(plan_path)
        cv.pack(fill='both', expand='yes')
        top.wait_window()
        return cv.selected_node_name()

'''
    Available operations:
        + left click to create a new node
        + right click on a node to edit it
        + left click on a node + move to create an edge
        + left click on the image + move to move the background
'''
class App(t.Frame):
    ALPHA_INITIAL_VALUE=128

    def __init__(self, master, **options):
        super().__init__(master)
        self.create_widgets(**options)

    def on_exit(self):
        if mbox.askquestion('Quit', 'Do you want to save before leaving?') == 'yes':
            self.save_to_xml(fdialog.asksaveasfilename(defaultextension='xml',
                             filetypes=[('XML Files', '.xml')],
                             initialdir=Config.XMLS_PATH))

    def create_widgets(self, **options):
        self.canvas = EditableGraphCanvas(self, width=options['c_width'], height=options['c_height'])
        self.canvas.pack(fill="both", expand="YES")
        self.open_file()
        self.alpha_scale = t.Scale(self, from_=1, to=255,
            command=lambda v: self.canvas.set_bg_image(v), orient=t.HORIZONTAL)
        self.alpha_scale.set(App.ALPHA_INITIAL_VALUE)
        self.alpha_scale.pack()

    def open_file(self):
        self.file_name = t.filedialog.askopenfilename(initialdir=Config.MAPS_PATH)
        ext = splitext(self.file_name)[1].lower()[1:]

        if ext == 'xml':
            self.canvas.load_xml(self.file_name)

        elif ext in ['bmp', 'jpg', 'jpe', 'jpeg', 'png', 'tif', 'tiff']:
            px_p_m = self.ask_metre_length()
            if px_p_m != None:
                self.canvas.set_pixels_per_metre(px_p_m)
                self.background_file_name = self.file_name
                self.canvas.set_bg_image(App.ALPHA_INITIAL_VALUE, self.background_file_name)
            else:
                self.destroy()

    def ask_metre_length(self):
        toplevel = t.Toplevel(self)
        nb_pixels = t.StringVar()
        t.Label(toplevel, text='Enter length (in pixels) of a meter on the given plan: ').grid(row=0, column=0)
        entry = t.Entry(toplevel, textvariable=nb_pixels)
        entry.grid(row=1, column=0)
        entry.focus_set()
        t.Button(toplevel, text='Ok', command=toplevel.destroy).grid(row=2)
        toplevel.bind('<Return>', lambda _: toplevel.destroy())
        toplevel.wait_window()
        return int(nb_pixels.get()) if (nb_pixels.get().isnumeric() and nb_pixels.get() != "") else None

    def scan(self):
        self.toplevel.wm_title('Scanning access points...')
        self.ap = AccessPointList(iterations=5)
        self.ap.scan()
        self.toplevel.wm_title('access points scanned')

    # Save functions

    def text(self, nb_tab=0):
        text = (TAB * (nb_tab+1)) + '<background_image coord="{}" />\n'.format(tuple(self.canvas.image_coord()))
        text += (TAB * (nb_tab+1)) + '<distance_unit value="{}" />\n'.format(self.canvas.get_pixels_per_metre())
        plan_name = purge_plan_name(self.background_file_name, Config.MAPS_PATH)
        text += (TAB * (nb_tab+1)) + '<nodes>\n'
        for node_id in self.canvas.nodes():
            text += self.canvas.nodes()[node_id].text(nb_tab+2)
        text += (TAB * (nb_tab+1)) + '</nodes>\n'

        text += (TAB * (nb_tab+1)) + '<edges>\n'
        text += (TAB * (nb_tab+2)) + '<internal>\n'
        for edge_id in self.canvas.edges():
            text += self.canvas.edges()[edge_id].text(nb_tab+3)
        text += (TAB * (nb_tab+2)) + '</internal>\n'
        text += (TAB * (nb_tab+2)) + '<external>\n'
        for ext_edge in self.canvas.external_edges():
            # internal_node_name, plan_name, external_node_name = ext_edge
            text += (TAB * (nb_tab+3)) + '<edge src="{}" plan="{}" dest="{}">\n'.format(*ext_edge)
        text += (TAB * (nb_tab+2)) + '</external>\n'
        text += (TAB * (nb_tab+1)) + '</edges>\n'

        return '<plan name="{}">\n{}</plan>\n'.format(plan_name, text)


    def save_to_xml(self, path):
        content = self.text()
        with open(path, 'w') as save_file:
            save_file.write(content)

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
