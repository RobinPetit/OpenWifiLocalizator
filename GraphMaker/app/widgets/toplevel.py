from app.general.tkinter_imports import *
from app.general.functions import purge_plan_name
from app.general.constants import *

from app.data.NodeData import *
import app.widgets as app_widgets
from app.Config import Config

from xml.etree import ElementTree
from os.path import splitext, relpath

class ExternalNodeFinder:
    @staticmethod
    def find(tkinter_master, plan_path):
        top = t.Toplevel(tkinter_master)
        top.title('Select the node to link with current one')
        cv = app_widgets.canvas.SelectableGraphCanvas(top)
        cv.load_xml(plan_path)
        cv.pack(fill='both', expand='yes')
        top.wait_window()
        return cv.selected_node_name()


class NodeConfigurationToplevel(t.Toplevel):
    def __init__(self, master, plan, node_id='', node_aliases=tuple(), handle_external=False):
        super().__init__(master)
        self.plan_name = plan
        self.node_data = NodeData(node_id, node_aliases)
        self.handle_external_edges = handle_external
        self.init_variables()
        self.create_widgets()

    def init_variables(self):
        # start at -1 so that increment makes it start from 0
        self.row = -1

    def create_widgets(self):
        # Do not show main window, only let this toplevel
        self.master.master.master.withdraw()
        self.create_widgets_aliases()
        if self.handle_external_edges:
            print('handling external edges')
            self.create_widgets_external_edges()
        self.create_widgets_validation()

    def create_widgets_aliases(self):
        self.aliases = list(self.node_data.aliases)
        # Aliases
        self.aliases_group = t.LabelFrame(self, text='Aliases Management', padx=5, pady=5, relief=t.SUNKEN, borderwidth=3)
        self.aliases_group.grid(row=1, column=0, columnspan=2)
        self.lb = t.Listbox(self.aliases_group, listvar=self.aliases)
        self.lb.grid(row=1, column=0, rowspan=3)
        for alias in self.aliases:
            self.lb.insert(t.END, alias)
        self.alias = t.StringVar()
        t.Entry(self.aliases_group, textvariable=self.alias).grid(row=1, column=1)
        t.Button(self.aliases_group, text='Add alias', command=lambda: (self.lb.insert(t.END, self.alias.get()), self.aliases.append(self.alias.get()))).grid(row=2, column=1)
        t.Button(self.aliases_group, text='Remove alias', command=lambda: self.lb.delete(t.ANCHOR)).grid(row=3, column=1)

    def create_widgets_external_edges(self):
        # External edges
        self.ext_edges_group = t.LabelFrame(self, text='External edges', padx=5, pady=5, relief=t.SUNKEN, borderwidth=3)
        self.ext_edges_group.grid(row=4, column=0, columnspan=2, rowspan=3)
        # This list contains all the external edges going out of the current_node
        self.ext_edges = list()
        self.ext_edges_lb = t.Listbox(self.ext_edges_group, listvar=self.ext_edges)
        for edge in self.master.external_edges():
            ext = edge.extremities()
            beg, end = (ext[0], ext[1]) if ext[0] in [master.nodes()[n].name() for n in master.nodes()] else (ext[1], ext[0])
            # Only consider external edges related to the current node
            if self.node_data.name == beg:
                self.ext_edges_lb.insert(t.END, edge.plan + EXTERNAL_EDGES_SEPARATOR + end)
                self.ext_edges.append(edge)
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
        del self.ext_edges[self.ext_edges_lb.curselection()]
        self.ext_edges_lb.delete(t.ANCHOR)

    def configure(self):
        self.wait_window()
        self.master.master.master.deiconify()
        ap = self.ap
        aliases = self.aliases
        if hasattr(self, 'ext_edges'):
            self.configure_external_edges()
        return ap, aliases

    def configure_external_edges(self):
        current_name = self.node_data.name
        self.master.plan_data.remove_external_edges_from(current_name)
        edges_dict = dict()
        for external_edge in self.ext_edges:
            extremities = external_edge.extremities()
            if current_name == extremities[0]:
                end = extremities[1]
            else:
                end = extremities[0]
            beg = current_name
            plan = external_edge.plan
            weight = external_edge.weight()
            #path, node, weight = external_edge.split(EXTERNAL_EDGES_SEPARATOR)
            if plan in edges_dict:
                edges_dict[plan].append((end, weight))
            else:
                edges_dict[plan] = [(end, weight)]
        for plan in edges_dict:
            xml_path = Config.XMLS_PATH + plan + '.xml'
            tree = ElementTree.ElementTree()
            root = tree.parse(xml_path)
            edges_markup = root.find('edges')
            XML_external_edges = edges_markup.find('external')
            if XML_external_edges is None:
                XML_external_edges = ElementTree.SubElement(edges_markup, 'external')
            for edge in XML_external_edges:
                 if edge.get('dest') == current_name:
                    XML_external_edges.remove(edge)
            for node, weight in edges_dict[plan]:
                #path, node = external_edge.split(EXTERNAL_EDGES_SEPARATOR)
                self.master.create_external_edge(current_name, plan, node, weight)
                # verify edge is in the other XML as well
                _ = ElementTree.SubElement(XML_external_edges, 'edge')
                _.attrib = {'weight': str(weight), 'beg': node, 'plan': splitext(relpath(self.plan_name, Config.MAPS_PATH))[0], 'end': current_name}
            tree.write(xml_path)

    def get_external_node(self):
        name = self.node_data.name
        # ask what plan to look for the node on (only XMLs since the nodes must already exist)
        plan_path = t.filedialog.askopenfilename(initialdir=Config.XMLS_PATH,
                                                 filetypes=[('XML Files', '.xml')])
        if plan_path == '' or plan_path is None:
            print('ERROR')  # TODO: handle properly with a popup
            return
        node_name = ExternalNodeFinder.find(self, plan_path)
        plan_short_name =  purge_plan_name(plan_path, Config.XMLS_PATH)
        self.plan_name = plan_short_name
        weight = askfloat('Edge weight', 'How long is this edge? (metres)', minvalue=.0)
        str_to_add = plan_short_name + EXTERNAL_EDGES_SEPARATOR + node_name + EXTERNAL_EDGES_SEPARATOR + str(weight)
        self.ext_edges_lb.insert(t.END, str_to_add)
        edge = app_widgets.canvas.ExternalEdge(weight, [node_name, name], plan_short_name)
        self.ext_edges.append(edge)

    def scan(self):
        self.toplevel.wm_title('Scanning access points...')
        self.ap = AccessPointList(iterations=5)
        self.ap.scan()
        self.toplevel.wm_title('access points scanned')

