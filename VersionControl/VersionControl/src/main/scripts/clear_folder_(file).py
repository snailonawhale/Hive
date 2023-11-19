import platform
import os
import sys

def clear_folder(folder_dir):
    files_in_folder = os.listdir(folder_dir)
    for file in files_in_folder:
        if platform.system()[:7] == "Windows":
            os.remove(folder_dir + "\\" + file)
        else:
            os.remove(folder_dir + "/" + file)

if __name__ == "__main__":
    clear_folder(sys.argv[1])
    sys.exit(0)

