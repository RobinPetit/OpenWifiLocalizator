from PIL import Image, ImageTk
from app.general.functions import *
from app.general.tkinter_imports import *
from app.general.constants import *
from app.data.PlanData import *
from app.widgets.toplevel import NodeConfigurationToplevel
from xml.etree import ElementTree

from app import App
from app.Config import Config
from app.network.access_points import StaticAccessPointList

from time import time

class GraphCanvas(t.Canvas):
    NODE_SIZE = 10
    EDGE_WIDTH = 2.5

    def __init__(self, master, **options):
        super().__init__(master, **options)
        self.init_variables()
        self.bind_events()

    def init_variables(self):
        self.plan_data = PlanData()
        self.left_moved = False
        self.cv_image_coord = [0, 0]
        self.px_p_m = 0
        self.node_idx = 0

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

    def add_external_edge(self, weight, extremities, plan):
        edge = ExternalEdge(weight, extremities, plan)
        self.plan_data.add_external_edge(edge)

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
            self.background_file_name = image_path
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
        self.background_file_name = Config.MAPS_PATH + str(root.get('name')) + '.png'
        self.set_bg_image(App.App.ALPHA_INITIAL_VALUE, self.background_file_name)
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
            if int(point.attrib['id'])  >= self.node_idx:
                self.node_idx = int(point.attrib['id']) + 1


    def load_edges(self, xml_tree):
        internal_edge = xml_tree.find('internal')
        for edge in internal_edge.findall('edge'):
            extremities = edge.get('beg'), edge.get('end')
            extremities_ids = [[node_id for node_id in self.nodes() \
                if self.nodes()[node_id].id() == extremity][0] for extremity in extremities]
            end_coord = [c + GraphCanvas.NODE_SIZE for c in self.nodes()[extremities_ids[1]].coord()[:2]]
            beg_coord = [c + GraphCanvas.NODE_SIZE for c in self.nodes()[extremities_ids[0]].coord()[:2]]
            edge_id = self.create_line(*beg_coord, *end_coord, width=GraphCanvas.EDGE_WIDTH)
            self.add_edge(float(edge.get('weight')), edge_id, extremities)

        external_edge = xml_tree.find('external')
        for edge in external_edge.findall('edge'):
            self.add_external_edge(float(edge.get('weight')), [edge.get('beg'), edge.get('end')], edge.get('plan'))

class SelectableGraphCanvas(GraphCanvas):
    def __init__(self, master, **options):
        super().__init__(master, **options)

    def handle_left_release(self, ev):
        if not super().handle_left_release(ev):
            if float(time() - self.left_click_time) <= EditableGraphCanvas.CLICK_TIME_SENSIBILITY:
                element_id = self.get_selected_el(ev.x, ev.y)
                if element_id in self.nodes():
                    self.selected = self.nodes()[element_id].id()
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
        super().init_variables()
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

    def get_node_id(self):
        _ = self.node_idx
        self.node_idx += 1
        return _

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
            access_points, aliases = self.configure_node(self.nodes()[selected].id(), self.nodes()[selected].aliases())
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
                if end == self.tmp_line_id:
                    end = None
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
                    extremity_ids = (self.nodes()[self.get_selected_el(*self.initial_click_coord)].id(), self.nodes()[end].id())
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

    def configure_node(self, current_name='', current_aliases=tuple()):
        return NodeConfigurationToplevel(self, self.background_file_name, current_name, current_aliases, handle_external=True).configure()

    def remove_ext_edge(self):
        del self.ext_edges[self.ext_edges_lb.curselection()]
        self.ext_edges_lb.delete(t.ANCHOR)

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
        access_points, aliases = NodeConfigurationToplevel(self, self.background_file_name).configure()
        node_coord = (x-GraphCanvas.NODE_SIZE, y-GraphCanvas.NODE_SIZE,
                      x+GraphCanvas.NODE_SIZE, y+GraphCanvas.NODE_SIZE)
        node_id = self.create_oval(*node_coord, fill='green' if access_points is not None else 'red')
        self.add_node(self.get_node_id(), node_id, access_points, aliases)

    def create_external_edge(self, internal_node, plan_name, external_node, weight=.0):
        self.add_external_edge(weight, [internal_node, external_node], plan_name)

