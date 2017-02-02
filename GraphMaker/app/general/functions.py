from math import sqrt
from os.path import basename, relpath, splitext

def euclidian_distance(a, b):
    return sqrt(sum([(x-y)*(x-y) for x, y in zip(a, b)]))

def purge_plan_name(plan, src):
    return relpath(splitext(plan)[0], src)

def path_to_building_name(path):
    return splitext(basename(path))[0]
    
def center_of_rectangle(rectangle):
    return [(rectangle[i]+rectangle[2+i])//2 for i in (0, 1)]
