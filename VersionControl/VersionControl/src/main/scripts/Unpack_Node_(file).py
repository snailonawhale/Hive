import os
from zipfile import ZipFile
import sys
import platform
import shutil

def unzip(location_of_zip,location_to_unzip_to):
    ZipFile(location_of_zip).extractall(location_to_unzip_to)

def copy_file(original_path, copy_path):
    #copies a file. Thats it. Thats all it does. nothing interesting to see
    #only takes raw strings
    try:
        shutil.copy2(original_path, copy_path)
    except shutil.SameFileError:
        print("Source and destination represents the same file.")

    except PermissionError:
        print("Permission denied.")

    except:
        print("Error occurred while copying file.")

def copy_folder(origin, destination):
    # only takes in raw strings
    # Finds all the files In zip_directory and copies them over to sw_directory

    files_in_folder = os.listdir(origin)

    if platform.system()[:7] == "Windows":
        for file in files_in_folder:
            copy_file(origin + "\\" + file, destination + "\\" + file)
    else:
        for file in files_in_folder:
             copy_file(origin + "/" + file, destination + "/" + file)


def clear_folder(folder_dir):
    files_in_folder = os.listdir(folder_dir)
    for file in files_in_folder:
        if platform.system()[:7] == "Windows":
            os.remove(folder_dir + "\\" + file)
        else:
            os.remove(folder_dir + "/" + file)

def eviscerate_folder(folder_path):
    if platform.system()[:7] == "Windows":
        target_path = folder_path[ : len(folder_path) - folder_path[::-1].index("\\") -1]
    else:
        target_path = folder_path[: len(folder_path) - folder_path[::-1].index("/") - 1]

    copy_folder(folder_path, target_path)
    clear_folder(folder_path)
    os.rmdir(folder_path)

def unpack_node(input_folder_path, output_folder_path):
    if platform.system()[:7] == "Windows":
        sep = "\\"
    else:
        sep = "/"

    clear_folder(output_folder_path)

    for file in os.listdir(input_folder_path):
        if file[-4:len(file)] == ".zip":
            unzip(input_folder_path + sep + file, output_folder_path)
            break

    sub_folder_name = os.listdir(output_folder_path)[0]
    eviscerate_folder(output_folder_path + sep + sub_folder_name)


if __name__ == "__main__":
    unpack_node(sys.argv[1], sys.argv[2])