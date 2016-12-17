from math import sqrt
from os.path import relpath, splitext

def euclidian_distance(a, b):
    return sqrt(sum([(x-y)*(x-y) for x, y in zip(a, b)]))

def purge_plan_name(plan, src):
    return relpath(splitext(plan)[0], src)

