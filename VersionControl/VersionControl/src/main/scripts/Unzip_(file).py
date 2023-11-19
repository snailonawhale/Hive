from zipfile import ZipFile

def unzip(location_of_zip,location_to_unzip_to):
    ZipFile(location_of_zip).extractall(location_to_unzip_to)
# must be given raw strings or will break!!!
# locations are paths

if __name__ == "__main__":
    unzip(sys.argv[1], sys.argv[2])