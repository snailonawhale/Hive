import shutil
import os
import sys
import platform


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

if __name__ == "__main__":
    copy_folder(sys.argv[1], sys.argv[2])
    sys.exit(0)