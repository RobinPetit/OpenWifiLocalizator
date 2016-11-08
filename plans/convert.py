#!/usr/bin/python3

from glob import glob
from os import system

for pdf_file in glob('**/*.pdf', recursive=True):
    print('Converting {}'.format(pdf_file))
    system('convert -density 150 "{}" -quality 100 "{}".png'.format(pdf_file, pdf_file[:-4]))
