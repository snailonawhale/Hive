import os
import platform
import sys

#opens all solidworks files in a folder

def launch_sldprt(folder_path):
    files_in_folder = os.listdir(folder_path)
    for file in files_in_folder:
        if platform.system()[:7] == "Windows":
            file_path = folder_path + "\\" + file
        else:
            file_path = folder_path + "/" + file

        if file[-7:] == ".SLDPRT" or file[-7:] == ".SLDASM":
            os.startfile(file_path)

if __name__ == "__main__":
    launch_sldprt(sys.argv[1])