#!/usr/bin/python3

import tkinter as t
from tkinter import filedialog as fdialog
from PIL import Image, ImageTk
from time import time

'''
    Available operations:
        + left click to create a new node
        + right click on a node to edit
'''
class App(t.Tk):
    ALPHA_INITIAL_VALUE=128
    LEFT_CLICK = '<Button-1>'
    WHEEL_CLICK = '<Button-2>'
    RIGHT_CLICK = '<Button-3>'
    WHEEL_UP = '<Button-4>'
    WHEEL_DOWN = '<Button-5>'
    LEFT_RELEASE = '<ButtonRelease-1>'
    WHEEL_RELEASE = '<ButtonRelease-2>'
    RIGHT_RELEASE = '<ButtonRelease-3>'
    LEFT_CLICK_MOTION = '<B1-Motion>'
    WHEEL_CLICK_MOTION = '<B2-Motion>'
    RIGHT_CLICK_MOTION = '<B3-Motion>'

    CLICK_TIME_SENSIBILITY = 0.1  # maximum time before click and release to be accepted

    NODE_SIZE = 10

    def __init__(self, **options):
        super().__init__()
        self.init_variables()
        self.create_widgets(**options)

    def init_variables(self):
        self.nodes = dict()
        self.left_moved = False
        self.left_src = None
        self.tmp_line_id = None

    def create_widgets(self, **options):
        self.canvas = t.Canvas(self, width=options['c_width'], height=options['c_height'])
        self.canvas.pack()
        self.chose_background_image()
        self.alpha_scale = t.Scale(self, from_=0, to=255,
            command=lambda v: self.make_bg_image(v), orient=t.HORIZONTAL)
        self.alpha_scale.set(App.ALPHA_INITIAL_VALUE)
        self.alpha_scale.pack()
        self.bind_events()

    def bind_events(self):
        canvas_callbacks = {
            App.LEFT_CLICK: self.handle_left_click,
            App.WHEEL_CLICK: self.handle_wheel_click,
            App.RIGHT_CLICK: self.handle_right_click,
            App.WHEEL_DOWN: self.handle_wheel_down,
            App.WHEEL_UP: self.handle_wheel_up,
            App.LEFT_RELEASE: self.handle_left_release,
            App.RIGHT_RELEASE: self.handle_right_release,
            App.WHEEL_RELEASE: self.handle_wheel_release,
            App.LEFT_CLICK_MOTION: self.handle_left_click_mvt,
            App.RIGHT_CLICK_MOTION: self.handle_right_click_mvt,
            App.WHEEL_CLICK_MOTION: self.handle_wheel_click_mvt}
        # bind canvas events
        for event in canvas_callbacks:
            self.canvas.bind(event, canvas_callbacks[event])

    def chose_background_image(self):
        self.background_file_name = t.filedialog.askopenfilename()
        self.bg_template = Image.open(self.background_file_name)
        self.make_bg_image(App.ALPHA_INITIAL_VALUE)

    def make_bg_image(self, alpha):
        self.bg_template.putalpha(int(alpha))
        self.bg_image = ImageTk.PhotoImage(self.bg_template)
        self.cv_image_id = self.canvas.create_image(0, 0, image=self.bg_image, anchor='nw')

    def add_node(self, x, y):
        new_id = self.canvas.create_oval(x-App.NODE_SIZE, y-App.NODE_SIZE, x+App.NODE_SIZE, y+App.NODE_SIZE, fill='red')
        print('Adding new node with id', new_id)
        self.nodes[new_id] = (x, y)

    # events handling code

    def get_selected_el(self, x, y, d=3):
        tmp = self.canvas.find_overlapping(x-d, y-d, x+d, y+d)
        print(tmp)
        i = 1
        while i < len(tmp) and (tmp[i] <= self.cv_image_id or tmp[i] == self.tmp_line_id):
            i += 1
        try:
            return tmp[i]
        except:
            return None

    def handle_left_click_mvt(self, ev):
        self.left_moved = True
        if self.left_src is not None and self.tmp_line_id is not None:
            self.canvas.delete(self.tmp_line_id)
            self.tmp_line_id = self.canvas.create_line(self.initial_click_coord[0], self.initial_click_coord[1], ev.x, ev.y)

    def handle_right_click_mvt(self, ev):
        print('RC MOVING')

    def handle_wheel_click_mvt(self, ev):
        print('WC MOVING')

    def handle_left_click(self, ev):
        self.left_src = self.get_selected_el(ev.x, ev.y)
        if self.left_src is not None:
            self.initial_click_coord = self.nodes[self.left_src]
            print(self.initial_click_coord)
            self.tmp_line_id = 0  # self.canvas.create_line(self.initial_click_coord[0], self.initial_click_coord[1], ev.x, ev.y)
        self.left_click_time = time()

    def handle_wheel_click(self, ev):
        print('WC')

    def handle_right_click(self, ev):
        print('RC')

    def handle_wheel_up(self, ev):
        print('WU')

    def handle_wheel_down(self, ev):
        print('WD')

    def handle_left_release(self, ev):
        if self.left_moved and self.tmp_line_id is not None:
            end = self.get_selected_el(ev.x, ev.y)
            self.canvas.delete(self.tmp_line_id)
            if end is not None:
                # to do, store the link somewhere to be saved in file
                self.canvas.create_line(self.initial_click_coord[0], self.initial_click_coord[1], self.nodes[end][0], self.nodes[end][1])
        elif float(time() - self.left_click_time) <= App.CLICK_TIME_SENSIBILITY:
            self.add_node(ev.x, ev.y)
        self.left_moved = False

    def handle_right_release(self, ev):
        print('RR')

    def handle_wheel_release(self, ev):
        print('WR')

def main():
    app = App(c_width=600, c_height=600)
    app.mainloop()

if __name__ == '__main__':
    main()
