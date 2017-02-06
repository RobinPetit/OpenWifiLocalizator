# OWL
from app.general.constants import *
from app.general.functions import *
from app.Config import Config

class Node:
    def __init__(self, nb, coords, access_points, aliases=tuple()):
        self.nb = nb
        self.coords = coords
        self.access_points_ = access_points
        self.color = Config.COLOR_VALID if access_points is not None else 'red'
        self.aliases_ = list(aliases)

    def coord(self, c=None):
        if c is None:
            return self.coords
        else:
            self.coords = c

    def id(self, nb=None):
        if nb is None:
            return self.nb
        else:
            self.nb = nb

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

class Edge:
    def __init__(self, coords, extremity_ids, nb=0):
        self.coords = coords
        self.extremity_ids = extremity_ids
        self.nb = nb

    def coord(self, c=None):
        if c is None:
            return self.coords
        else:
            self.coords = c
    
    def id(self, nb=None):
        if nb is None:
            return self.nb
        else:
            self.nb = nb
            
    def get_extremity_ids(self):
        return self.extremity_ids

class ExternalEdge(Edge):

    def __init__(self, extremity_ids, plan):
        super().__init__([0, 0], extremity_ids)
        self.plan = plan

    def extremities(self, ext=None):
        if ext is not None:
            self.extremity_ids = ext[:]
        else:
            return self.extremity_ids

class PlanData:
    def __init__(self):
        self.nodes = dict()
        self.internal_edges = dict()
        self.external_edges = list()
        self.bg_image = None

    def add_node(self, node_id, node):
        self.nodes[node_id] = node

    def add_edge(self, edge_id, edge):
        self.internal_edges[edge_id] = edge

    def add_external_edge(self, edge):
        self.external_edges.append(edge)

    def set_bg_image(self, bg_image):
        self.bg_image = bg_image

    def get_nodes(self):
        return self.nodes

    def get_edges(self):
        return self.internal_edges

    def get_external_edges(self):
        return self.external_edges

    def remove_external_edges_from(self, name):
        self.external_edges = [edge for edge in self.external_edges if name not in edge.extremities()]
