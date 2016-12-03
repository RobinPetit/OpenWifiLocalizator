#!/usr/bin/python3

from glob import glob
from os import system

pdf_files = glob('**/*.pdf', recursive=True)
for i in range(len(pdf_files)):
    print('\r{:2.2f}%'.format(100*i/len(pdf_files)), end='')
    system('convert -density 150 "{}" -quality 100 "{}".png'.format(pdf_files[i], pdf_files[i][:-4]))
print('\r100.00% completed!')
