from app.general.tkinter_imports import *
from app.general.functions import purge_plan_name
from app.general.constants import *

from app.data.NodeData import *
from app.widgets import canvas
from app.Config import Config
from app.network.access_points import AccessPointList
# std
from xml.etree import ElementTree
from os.path import splitext, relpath
import sqlite3

class ExternalNodeFinder:
    @staticmethod
    def find(tkinter_master, plan_path, database):
        top = t.Toplevel(tkinter_master)
        top.title('Select the node to link with current one')
        cv = canvas.SelectableGraphCanvas(top, database, width=500, height=500)
        cv.load_plan(plan_path)
        #cv.load_xml(plan_path)
        cv.pack(fill='both', expand='yes')
        top.wait_window()
        return cv.selected_node_name()

class NodeConfigurationToplevel(t.Toplevel):
    def __init__(self, master, plan, database, node_id='', node_aliases=tuple(), handle_external=False):
        super().__init__(master)
        self.database = database
        self.master = master
        self.plan_name = plan
        self.node_data = NodeData(node_id, node_aliases)
        self.handle_external_edges = handle_external
        self.init_variables()
        self.create_widgets()
        # Add first plan
        self.wm_attributes("-topmost", 1)
        self.focus_force()

    #@TODO Use this when improving code
    def init_variables(self):
        # start at -1 so that increment makes it start from 0
        self.row = -1

    def create_widgets(self):
        # Do not show main window, only let this toplevel
        #self.master.master.master.withdraw()
        self.create_widgets_aliases()
        if self.handle_external_edges:
            self.create_widgets_external_edges()
        self.create_widgets_validation()

    def create_widgets_aliases(self):
        self.aliases = list(self.node_data.aliases)

        # Information about node
        if(self.node_data.name != ""):
            print("Information about node: " + str(self.node_data.name))
            nodeInfo = t.Label(self, text="Node: " + str(self.node_data.name))
            nodeInfo.grid(row=0, column=0, columnspan=2)

        # Aliases
        self.aliases_group = t.LabelFrame(self, text='Aliases Management', padx=5, pady=5, relief=t.SUNKEN, borderwidth=3)
        self.aliases_group.grid(row=1, column=0, columnspan=2)
        self.lb = t.Listbox(self.aliases_group, listvar=self.aliases)
        self.lb.grid(row=1, column=0, rowspan=3)
        self.lb.delete(0, t.END)
        for alias in self.aliases:
            self.lb.insert(t.END, alias)
        self.alias = t.StringVar()
        entry = t.Entry(self.aliases_group, textvariable=self.alias)
        entry.grid(row=1, column=1)
        entry.focus_set()
        t.Button(self.aliases_group, text='Add alias', command=self.add_alias).grid(row=2, column=1)
        t.Button(self.aliases_group, text='Remove alias', command=self.delete_alias).grid(row=3, column=1)

    def add_alias(self):
        alias = self.alias.get()
        if alias == '':
            return
        self.lb.insert(t.END, alias)
        self.aliases.append(alias)

    def delete_alias(self):
        for sel in self.lb.curselection():
            del self.aliases[sel]
        self.lb.delete(t.ANCHOR)

    def create_widgets_external_edges(self):
        # External edges
        self.ext_edges_group = t.LabelFrame(self, text='External edges', padx=5, pady=5, relief=t.SUNKEN, borderwidth=3)
        self.ext_edges_group.grid(row=4, column=0, columnspan=2, rowspan=3)
        # This list contains all the external edges going out of the current_node
        self.ext_edges = list()
        self.ext_edges_lb = t.Listbox(self.ext_edges_group, listvar=self.ext_edges)
        already_existing_external_edges = self.database.load_external_edges_from_node(self.node_data.name)
        for edge in already_existing_external_edges:
            other_id = edge[1] if edge[2] == self.node_data.name else edge[2]
            plan = self.database.get_plan_name_from_node(other_id)
            self.ext_edges_lb.insert(t.END, plan + EXTERNAL_EDGES_SEPARATOR + str(other_id))
        self.ext_edges_lb.grid(row=5, column=0, rowspan=2)
        t.Button(self.ext_edges_group, text='Add external edge \nfrom this node',
                 command=self.get_external_node).grid(row=5, column=1)
        t.Button(self.ext_edges_group, text='Remove external edge',
                 command=self.remove_ext_edge).grid(row=6, column=1)
        # Display at the end to have the correct row number

    def create_widgets_validation(self):
        # Validation & scans
        t.Button(self, text='Ok', command=self.destroy).grid(row=7, column=0)
        self.ap = None
        t.Button(self, text='Scan access points', command=self.scan).grid(row=7, column=1)

    def get_current_row(self):
        self.row += 1
        return self.row

    def remove_ext_edge(self):
        for sel in self.ext_edges_lb.curselection():
            print(sel)
            # remove edge from db
            str_edge = self.ext_edges_lb.get(sel)
            other_node_id = int(str_edge.split(EXTERNAL_EDGES_SEPARATOR)[1])
            self.database.remove_edge_by_nodes(other_node_id, self.node_data.name)
            # remove edge from listbox
            del self.ext_edges[sel]
        self.ext_edges_lb.delete(t.ANCHOR)

    def configure(self):
        self.wait_window()
        self.master.master.master.deiconify()
        ap = self.ap
        aliases = list(set(self.aliases))
        return list() if ap is None else ap, aliases

    def get_external_node(self):
        name = self.node_data.name
        # ask plan (PNG)
        plan_path = t.filedialog.askopenfilename(initialdir=Config.MAPS_PATH,
                                                 filetypes=[('PNG Files', '.png')])
        if plan_path == '' or plan_path is None:
            print('ERROR')  # TODO: handle properly with a popup
            return
        try:
            node_name = ExternalNodeFinder.find(self, plan_path, self.database)
            plan_short_name =  purge_plan_name(plan_path, Config.MAPS_PATH)
            edge = canvas.ExternalEdge([node_name, name], plan_short_name)
            # add edge to db
            self.database.save_edge(edge)
            self.ext_edges.append(edge)
            str_to_add = plan_short_name + EXTERNAL_EDGES_SEPARATOR + str(node_name)
            self.ext_edges_lb.insert(t.END, str_to_add)
        except sqlite3.IntegrityError as e:
            print('SQLite error:', e)

    def scan(self):
        self.wm_title('Scanning access points...')
        self.ap = AccessPointList(iterations=5)
        self.ap.scan()
        self.wm_title('access points scanned')

