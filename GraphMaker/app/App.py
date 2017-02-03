from app.general.tkinter_imports import *
from app.general.functions import *
from app.general.constants import *
from os.path import splitext, relpath, basename
from app.widgets.canvas import EditableGraphCanvas
from app.Config import Config
from app.database.database import Database
# std
import sqlite3

'''
    Available operations:
        + left click to create a new node
        + right click on a node to edit it
        + left click on a node + move to create an edge
        + left click on the image + move to move the background
        + right click on a node + move to move the node and the edges related to it
'''
class App(t.Frame):
    ALPHA_INITIAL_VALUE = 128

    def __init__(self, master, **options):
        super().__init__(master)
        self.options = options
        self.database = Database()
        self.plan_exists_in_db = False

        self.master = master
        self.bind('<Control-s>', self.save)
        self.create_widgets(**options)

    def on_exit(self):
        self.database.close()

    def create_widgets(self, **options):
        self.canvas = EditableGraphCanvas(self, self.database, width=options['c_width'], height=options['c_height'])
        self.canvas.pack(fill="both", expand="YES")
        self.open_file()
        self.alpha_scale = t.Scale(self, from_=1, to=255,
            command=lambda v: self.canvas.set_bg_image(v), orient=t.HORIZONTAL)
        self.alpha_scale.set(App.ALPHA_INITIAL_VALUE)
        self.alpha_scale.pack()
        self.createMenu()

    def open_file(self):
        self.file_name = t.filedialog.askopenfilename(initialdir=Config.MAPS_PATH,
            filetypes=[('PNG Files', '.png')])
        ext = splitext(self.file_name)[1].lower()[1:]
        filename = path_to_building_name(self.file_name)
        if self.database.exists_plan(filename):
            self.plan_exists_in_db = True
            self.background_file_name = self.file_name
            self.canvas.load_plan(self.file_name)
        else:
            new_plan_data = self.ask_new_plan_data()
            if None not in new_plan_data:
                self.canvas.set_pixels_per_metre(new_plan_data.ppm)
                self.canvas.set_angle_with_parent(new_plan_data.angle)
                self.canvas.set_position_on_parent([new_plan_data.x, new_plan_data.y])
                self.background_file_name = self.file_name
                self.canvas.set_bg_image(App.ALPHA_INITIAL_VALUE, self.background_file_name)
                self.database.save_plan(filename, new_plan_data)
            else:
                self.destroy()

    class NewPlanData:
        def __init__(self, ppm, angle, pos):
            self.ppm = ppm
            self.angle = angle
            self.x = pos[0]
            self.y = pos[1]

        def __iter__(self):
            return [self.ppm, self.angle, self.x, self.y].__iter__()

    def open_new_file(self):
        self.canvas.destroy()
        self.canvas = None
        self.alpha_scale.destroy()
        self.alpha_scale = None
        self.create_widgets(**self.options)


    def ask_new_plan_data(self):
        toplevel = t.Toplevel(self)
        # Pixels per metre
        nb_pixels = t.StringVar()
        t.Label(toplevel, text='Enter length (in pixels) of a meter on the given plan: ').grid(row=0, column=0)
        ppm_entry = t.Entry(toplevel, textvariable=nb_pixels)
        ppm_entry.grid(row=0, column=1, columnspan=2)
        # Angle with parent
        angle = t.StringVar()
        t.Label(toplevel, text='Enter the angle (trigonometric direction but in degrees) of\n'
                               'this building relative to its parent plan (Solbosch or Plaine): ') \
                .grid(row=1, column=0)
        angle_entry = t.Entry(toplevel, textvariable=angle)
        angle_entry.grid(row=1, column=1, columnspan=2)
        # Position on parent plan
        x_on_parent = t.StringVar()
        y_on_parent = t.StringVar()
        t.Label(toplevel, text='Enter the building position on parent plan: ').grid(row=2, column=0)
        x_entry = t.Entry(toplevel, textvariable=x_on_parent)
        y_entry = t.Entry(toplevel, textvariable=y_on_parent)
        x_entry.grid(row=2, column=1)
        y_entry.grid(row=2, column=2)
        t.Button(toplevel, text='Ok', command=toplevel.destroy).grid(row=3, column=0, columnspan=3)
        toplevel.bind('<Return>', lambda _: toplevel.destroy())
        toplevel.wait_window()
        try:
            ppm = float(nb_pixels.get())
            angle = float(angle.get())
            x = int(x_on_parent.get())
            y = int(y_on_parent.get())
        except:
            ppm = angle = x = y = None
        return self.NewPlanData(ppm, angle, [x, y])

    def scan(self):
        self.toplevel.wm_title('Scanning access points...')
        self.ap = AccessPointList(iterations=5)
        self.ap.scan()
        self.toplevel.wm_title('access points scanned')

    def createMenu(self):
        menubar=t.Menu(self.master)
        filemenu=t.Menu(menubar,tearoff=0)
        filemenu.add_command(label="Open a new", command=self.open_new_file)
        filemenu.add_command(label="Save", command=self.save)
        filemenu.add_separator()
        filemenu.add_command(label="Quit", command=self.master.destroy)
        menubar.add_cascade(label="File", menu=filemenu)

        self.master.config(menu=menubar)


    # Save functions

    def save(self):
        self.database.update_plan(self.canvas.get_bg_coord(), path_to_building_name(self.file_name))

    class PlanNameData:
        def _init__(self):
            self.campus = None
            self.name = None

        def __str__(self):
            return 'campus: {}\tname: {}'.format(self.campus, self.name)

    def parse_plan_name(self):
        file_name = splitext(relpath(self.canvas.background_file_name, Config.MAPS_PATH))[0]
        plan_data = App.PlanNameData()
        plan_data.campus = file_name.split("_")[0]
        plan_data.name = file_name
        return plan_data

