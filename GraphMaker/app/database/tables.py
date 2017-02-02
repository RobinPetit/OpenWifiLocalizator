class BuildingTable:
    def __init__(self, name, ppm, on_parent, bg_coord, angle):
        assert type(on_parent) is type(bg_coord) is tuple
        self.name = name
        self.ppm = ppm
        self.on_parent = on_parent
        self.bg_coord = bg_coord
        self.angle = angle
