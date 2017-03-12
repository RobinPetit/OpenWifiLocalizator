# tkinter
from PIL import Image, ImageTk
# OWL
from app import App
from app.Config import Config
from app.general.functions import *
from app.general.tkinter_imports import *
from app.general.constants import *
from app.data.PlanData import *
from app.widgets.toplevel import NodeConfigurationToplevel
from app.network.access_points import AccessPointList
from app.database.database import Database
# std
import sqlite3
from xml.etree import ElementTree
from time import time


class GraphCanvas(t.Canvas):
    def __init__(self, master, database, **options):
        super().__init__(master, **options)
        self.database = database
        self.master = master
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

    def add_node(self, node_id, access_points, aliases=tuple(), node_name=0):
        node = Node(node_name, self.coords(node_id), access_points, aliases=aliases)
        self.plan_data.add_node(node_id, node)
        print('name: ' + str(node_name))
        if node_name == 0:
            node.id(self.database.save_node(node, path_to_plan_name(self.master.file_name)))

    def add_edge(self, edge_id, extremities, nb=0):
        edge = Edge(self.coords(edge_id), extremities, nb=nb)
        self.plan_data.add_edge(edge_id, edge)
        if nb == 0:
            edge.id(self.database.save_edge(edge))

    def add_external_edge(self, extremities, plan, save=False):
        edge = ExternalEdge(extremities, plan)
        self.plan_data.add_external_edge(edge)
        if save:
            edge.id(self.database.save_edge(edge))

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
        assert isinstance(px_p_m, (int, float))
        self.px_p_m = px_p_m

    def set_angle_with_parent(self, angle):
        assert isinstance(angle, float)
        self.parent_angle = angle

    def set_position_on_parent(self, c):
        assert isinstance(c, (list, tuple)) and len(list(filter(lambda e: isinstance(e, (int, float)), c))) == len(c)
        self.pos_on_parent = c

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

    def get_angle_with_parent(self):
        return self.parent_angle

    def get_position_on_parent(self):
        return self.pos_on_parent

    def get_bg_coord(self):
        return self.cv_image_coord

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

    def load_plan(self, background_file_name):
        filename = path_to_plan_name(background_file_name)
        plan = self.database.load_plan(filename)
        self.set_pixels_per_metre(plan.ppm)
        self.set_angle_with_parent(plan.angle)
        self.set_position_on_parent(plan.on_parent)
        self.set_bg_image(App.App.ALPHA_INITIAL_VALUE, background_file_name)
        self.set_bg_coord(plan.bg_coord)
        for node_id, x, y, aliases, has_ap in self.database.load_nodes_from_plan(filename):
            self.create_node_from_db(x, y, aliases, has_ap, node_id)
        for edge in self.database.load_edges_from_plan(filename):
            self.create_edge_from_db(*edge)

    def create_node_from_db(self, x, y, aliases, has_ap, db_id):
        node_coord = (x-NODE_SIZE, y-NODE_SIZE,
                      x+NODE_SIZE, y+NODE_SIZE)
        # node_id = self.create_oval(*node_coord, fill='green' if has_ap else 'red')
        node_id = self.create_oval(*node_coord, fill=Config.COLOR_VALID if has_ap else 'red')
        self.add_node(node_id, [], aliases=aliases, node_name=db_id)

    def create_edge_from_db(self, nb, id1, id2):
        n1 = n2 = None
        for n in self.nodes():
            if self.nodes()[n].id() == id1:
                n1 = n
            elif self.nodes()[n].id() == id2:
                n2 = n
        if None in (n1, n2):
            err = ''
            if n1 is None:
                err += ' {} does not exist'.format(id1)
            if n2 is None:
                err += ' {} does not exist'.format(id2)
            print('unable to create an edge between nodes {} and {}. Reason is:{}' \
                  .format(id1, id2, err))
        beg_coord = [c + NODE_SIZE for c in self.nodes()[n1].coord()[:2]]
        end_coord = [c + NODE_SIZE for c in self.nodes()[n2].coord()[:2]]
        edge_id = self.create_line(*beg_coord, *end_coord, width=EDGE_WIDTH)
        self.add_edge(edge_id, [id1, id2], nb=nb)

    def update_nodes_position(self):
        nodes_list = [self.nodes()[n] for n in self.nodes()]
        self.database.update_all_nodes_position(nodes_list)

class SelectableGraphCanvas(GraphCanvas):
    def __init__(self, master, database, **options):
        super().__init__(master, database, **options)

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

    def __init__(self, master, database, **options):
        super().__init__(master, database, **options)
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
        self.right_moved = True
        offset = [ev.x - self.click_coord[0], ev.y - self.click_coord[1]]
        new_pos = [offset[i%2]+self.moving_node_original_coords[i] for i in range(4)]
        self.coords(self.selected_node, *new_pos)
        for edge in self.moving_edges_edit_idx:
            tmp = self.moving_edges_edit_idx[edge][1]
            tmp[self.moving_edges_edit_idx[edge][0]] = (new_pos[2] + new_pos[0]) // 2
            tmp[self.moving_edges_edit_idx[edge][0]+1] = (new_pos[3] + new_pos[1]) // 2
            self.coords(edge, *tmp)

    def handle_wheel_click_mvt(self, ev):
        if Config.DEBUG:
            print('WC MOVING')

    def handle_left_click(self, ev):
        self.left_src = self.get_selected_el(ev.x, ev.y)
        if self.left_src is not None:
            self.initial_click_coord = [self.nodes()[self.left_src].coord()[0]+NODE_SIZE,
                                        self.nodes()[self.left_src].coord()[1]+NODE_SIZE]
            coords = self.initial_click_coord + self.initial_click_coord
            self.tmp_line_id = self.create_line(*coords, width=EDGE_WIDTH)
        else:
            self.click_coord = [ev.x, ev.y]
        self.left_click_time = time()

    def handle_wheel_click(self, ev):
        selected = self.get_selected_el(ev.x, ev.y)
        if selected is None:
            return
        if selected in self.nodes():
            node_center = self.nodes()[selected].coord()[:2]
            node_center = [c+NODE_SIZE for c in node_center]
            self.delete(selected)
            # remove node from db
            self.database.remove_node(self.nodes()[selected])
            edges_to_remove = list()
            for edge_id in self.edges():
                if self.nodes()[selected].id() in self.edges()[edge_id].get_extremity_ids():
                    self.delete(edge_id)
                    edges_to_remove.append(edge_id)
            for edge_id in edges_to_remove:
                del self.edges()[edge_id]
            del self.nodes()[selected]
        elif selected in self.edges():
            self.delete(selected)
            self.database.remove_edge(self.edges()[selected])
            del self.edges()[selected]
        else:
            print('\t\tERROR')

    def handle_right_click(self, ev):
        self.right_clicked = True
        self.right_moved = False
        self.click_coord = [ev.x, ev.y]
        self.selected_node = self.get_selected_el(ev.x, ev.y)
        if self.selected_node not in self.nodes():
            return
        self.moving_node_original_coords = self.coords(self.selected_node)
        if Config.DEBUG:
            print('selected node: {} with position {}'.format(self.nodes()[self.selected_node].id(), self.moving_node_original_coords))
        self.moving_edges_edit_idx = dict()
        for edge in self.edges():
            if self.nodes()[self.selected_node].id() in self.edges()[edge].extremity_ids:
                if self.edges()[edge].coord()[0] == self.moving_node_original_coords[0] + NODE_SIZE:
                    self.moving_edges_edit_idx[edge] = [0]
                elif self.edges()[edge].coord()[2] == self.moving_node_original_coords[0] + NODE_SIZE:
                    self.moving_edges_edit_idx[edge] = [2]
                else:
                    print('ERROR')
                    continue
                self.moving_edges_edit_idx[edge].append(self.coords(edge)[:])
                if Config.DEBUG:
                    print(self.coords(edge))
                    print('adding edge {}'.format(edge))

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
                    final_point = [node_coord[i]+NODE_SIZE for i in range(2)]
                    edge_id = self.create_line(*self.initial_click_coord,
                        self.nodes()[end].coord()[0]+NODE_SIZE, self.nodes()[end].coord()[1]+NODE_SIZE,
                            width=2.5)
                    extremity_ids = (self.nodes()[self.get_selected_el(*self.initial_click_coord)].id(), self.nodes()[end].id())
                    if len(set(extremity_ids)) == 2:
                        self.add_edge(edge_id, extremity_ids)
            else:
                self.cv_image_coord = self.coords(self.cv_image_id)
                for node_id in self.nodes():
                    self.nodes()[node_id].coord(self.coords(node_id))
                for edge_id in self.edges():
                    self.edges()[edge_id].coord(self.coords(edge_id))
        elif float(time() - self.left_click_time) <= EditableGraphCanvas.CLICK_TIME_SENSIBILITY and self.left_src is None:
            self.create_node(ev.x, ev.y)
        self.left_moved = False
        self.left_src = None

    def handle_right_release(self, ev):
        if self.right_moved:
            self.nodes()[self.selected_node].coord(self.coords(self.selected_node))
            self.database.update_node_position(self.nodes()[self.selected_node])
            for edge in self.moving_edges_edit_idx:
                self.edges()[edge].coord(self.coords(edge))
            return
        selected = self.get_selected_el(ev.x, ev.y)
        if selected is None:
            return
        if selected in self.nodes():
            access_points, aliases = self.configure_node(self.nodes()[selected].id(), self.nodes()[selected].aliases())
            # update aliases
            set_new_aliases = set(aliases)
            set_old_aliases = set(self.nodes()[selected].aliases())
            removed_aliases = set_old_aliases - set_new_aliases
            new_aliases = set_new_aliases - set_old_aliases
            assert (removed_aliases | new_aliases) == (set_new_aliases ^ set_old_aliases)
            self.database.update_node_aliases(self.nodes()[selected], removed_aliases, new_aliases)
            aliases = list(set_new_aliases)
            self.nodes()[selected].aliases(aliases)
            # update access points
            if type(access_points) is AccessPointList:
                self.nodes()[selected].access_points(access_points)
                self.database.set_node_access_points(self.nodes()[selected], access_points)
                # self.color = 'green'
                self.color = Config.COLOR_VALID
                # self.itemconfig(selected, fill='green')
                self.itemconfig(selected, fill=Config.COLOR_VALID)
        self.right_clicked = self.right_moved = False

    def handle_wheel_release(self, ev):
        if(Config.DEBUG):
            print('WR')

    def configure_node(self, current_name='', current_aliases=tuple()):
        return NodeConfigurationToplevel(self, self.background_file_name, self.database, current_name, current_aliases, handle_external=True).configure()

    def remove_ext_edge(self):
        del self.ext_edges[self.ext_edges_lb.curselection()]
        self.ext_edges_lb.delete(t.ANCHOR)

    def create_node(self, x, y):
        access_points, aliases = NodeConfigurationToplevel(self, self.background_file_name, self.database).configure()
        node_coord = (x-NODE_SIZE, y-NODE_SIZE,
                      x+NODE_SIZE, y+NODE_SIZE)
        #node_id = self.create_oval(*node_coord, fill='green' if type(access_points) is not list else 'red')
        node_id = self.create_oval(*node_coord, fill=Config.COLOR_VALID if type(access_points) is not list else 'red')
        self.add_node(node_id, access_points, aliases)

    def create_external_edge(self, internal_node, plan_name, external_node):
        self.add_external_edge([internal_node, external_node], plan_name)
