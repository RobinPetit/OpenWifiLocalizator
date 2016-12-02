
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

    def add_external_edge(self, internal_node, plan_name, external_node, weight):
        self.external_edges.append(ExternalEdge(weight, [internal_node, external_node], plan_name))

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

