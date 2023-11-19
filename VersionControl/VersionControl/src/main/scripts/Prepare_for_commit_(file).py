import shutil
import os
import datetime
import sys
import platform

#prepares a folder for commit by zipping folder, moving that zipped folder
# to a temp directory and creating the ID file
# The message has to be encased in " ... "

def create_ID_file(folder_dir, parent_hash, own_hash, author, Date, commit_msg):
    if platform.system()[:7] == "Windows":
        fobj = open(folder_dir + "\\" + "update.txt", "w")
    else:
        fobj = open(folder_dir + "/" + "update.txt", "w")
    fobj.write(parent_hash + " " + own_hash + " " + author + " " + Date + " " + commit_msg)
    fobj.close()

def zipfolder(source, destination):
    base_name = '.'.join(destination.split('.')[:-1])
    format = destination.split('.')[-1]
    root_dir = os.path.dirname(source)
    base_dir = os.path.basename(source.strip(os.sep))
    shutil.make_archive(base_name, format, root_dir, base_dir)

def clear_folder(folder_dir):
    files_in_folder = os.listdir(folder_dir)
    for file in files_in_folder:
        if platform.system()[:7] == "Windows":
            os.remove(folder_dir + "\\" + file)
        else:
            os.remove(folder_dir + "/" + file)

def prepare_for_commit(committed_folder_path, output_folder_path, parent_hash, author, commit_msg):
    # creates a zip file containing the node and a txt ID file
    #Clears the output folder before copying, so be careful!
    
    clear_folder(output_folder_path)
    date = str(datetime.date.today().isoformat()) + " " + str(datetime.time())
    own_hash = author + str(datetime.datetime.now().isoformat()).split(".")[0][:-8]
    own_hash = own_hash.replace(":", "-", 1000)
    create_ID_file(output_folder_path, parent_hash, own_hash, author, date, commit_msg)

    if platform.system()[:7] == "Windows":
        zipfolder(committed_folder_path, output_folder_path + "\\" + own_hash + ".zip")
    else:
        zipfolder(committed_folder_path, output_folder_path + "/" + own_hash + ".zip")


if __name__ == "__main__":
    prepare_for_commit(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])