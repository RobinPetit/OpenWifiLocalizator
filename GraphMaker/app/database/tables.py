class PlanTable:
    def __init__(self, name, ppm, on_parent, bg_coord, angle, image_dir):
        assert type(on_parent) is type(bg_coord) is tuple
        self.image_dir = image_dir
        self.name = name
        self.ppm = ppm
        self.on_parent = on_parent
        self.bg_coord = bg_coord
        self.angle = angle
