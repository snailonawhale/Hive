import os
import shutil
import sys

def zipfolder(source, destination):
    base_name = '.'.join(destination.split('.')[:-1])
    format = destination.split('.')[-1]
    root_dir = os.path.dirname(source)
    base_dir = os.path.basename(source.strip(os.sep))
    shutil.make_archive(base_name, format, root_dir, base_dir)

# must be given raw strings or will break!!!
# source is a path
# destination is NOT only the path to save to, it is the path plus .zip (IMPORTANT)

if __name__ == "__main__":
    zipfolder(sys.argv[1], sys.argv[2])