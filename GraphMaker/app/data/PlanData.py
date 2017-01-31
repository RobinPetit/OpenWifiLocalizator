from app.general.constants import *

class Node:
    INSERT_NODE_QUERY = \
        """
        INSERT INTO Node(buildingId, X, Y)
            VALUES({0}, {1}, {2})
        """
    INSERT_ALIAS_QUERY = \
        """
        INSERT INTO Aliases(Id, Name)
            VALUES({0}, '{1}')
        """

    def __init__(self, nb, coords, access_points, aliases=tuple()):
        self.nb = nb
        self.coords = coords
        self.access_points_ = access_points
        self.color = 'green' if access_points is not None else 'red'
        self.aliases_ = list(aliases)

    def coord(self, c=None):
        if c is None:
            return self.coords
        else:
            self.coords = c

    def id(self):
        return self.nb

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
        return '{0}<node id="{1}">\n{2}{0}</node>\n'.format(TAB*nb_tab, self.id(), text)

    def sql(self, building_id):
        query = Node.INSERT_NODE_QUERY.format(
            building_id,
            *self.coord()
            #self.id()
        )
        queries = [query]
        for alias in self.aliases():
            queries.append(Node.INSERT_ALIAS_QUERY.format(
                self.id(),
                alias
            ))
        return queries

class Edge:
    INSERT_EDGE_QUERY = \
        """
        INSERT INTO Edge(Node1Id, Node2Id, Weight)
            VALUES({0}, {1}, {2})
        """ # -BuildingId

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

    def sql(self, building_id):
        query = Edge.INSERT_EDGE_QUERY.format(
            #building_id,
            *self.extremity_ids,
            self.weight()
        )
        return query

class ExternalEdge(Edge):
    INSERT_EXT_EDGE_QUERY = \
        """
        INSERT INTO Edge(Node1Id, Node2Id, Weight)
            VALUES({0}, {1}, {2})
        """ # -BuildingId et BuildingId2

    def __init__(self, weight, extremity_ids, plan):
        super().__init__(weight, [0, 0], extremity_ids)
        self.plan = plan

    def extremities(self, ext=None):
        if ext is not None:
            self.extremity_ids = ext[:]
        else:
            return self.extremity_ids

    def text(self, nb_tab=0):
        return (TAB*(nb_tab)) + '<edge beg="{}" end="{}" weight="{}" plan="{}" />' \
                                .format(*self.extremity_ids, self.weight(), self.plan)

    def sql(self, building_id):
        query = ExternalEdge.INSERT_EXT_EDGE_QUERY.format(
            #building_id,
            *self.extremity_ids,
            #"(SELECT id FROM Building WHERE name='{}')".format(self.plan),  # TODO check if self.plan contains really the name TODO check if necessary
            self.weight()
        )
        return query

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
        # internal_node, plan_name, external_node, weight):
        self.external_edges.append(edge)  #ExternalEdge(weight, [internal_node, external_node], plan_name))

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
