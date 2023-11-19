from TCP_client import Client
from os.path import join, abspath, basename, getsize, dirname
from os import system#COMMENT
import sys

host = '10.122.33.50'
port = 3000

other_host = '10.122.231.147'
other_port = 3001

#JAVA_DIR_PATH = 'C:\\Users\\jrog1\\OneDrive\\Desktop\\CodeJam2023\\Hive\\JAVA_DIR_SAMPLE'
JAVA_DIR_PATH = r'C:\Users\vince\Desktop\VersionControl\VersionControl\src\main\scripts\staging'
SERVER_DIR = 'C:\\Users\\jrog1\\OneDrive\\Desktop\\CodeJam2023\\Hive\\Hive_Database'


def run_sender(sourcepath, hostStatus):
    print('running sender, sending ' + sourcepath + ' amHost = ' + hostStatus)
    client0 = Client()#name client
    client0.est_conn(other_host, other_port)
    client0.send_file_name(sourcepath)
    client0.c_sock.close()

    client1 = Client()#size client
    client1.est_conn(other_host, other_port)
    client1.send_file_size(sourcepath)
    client1.c_sock.close()

    client2 = Client()#name client
    client2.est_conn(other_host, other_port)
    client2.client_send(sourcepath, 1024)
    client2.c_sock.close()

    receptionPath = ''
    if hostStatus != 'H' and sourcepath[-4:] == '.zip':
        #run_sender(join(dirname(sourcepath), 'update.txt'), amHost)
        system('py send.py '+ join(dirname(sourcepath), 'update.txt') + ' ' + hostStatus)
        return
    if hostStatus != 'H':
        SAVEPATH = JAVA_DIR_PATH
    else:
        SAVEPATH = SERVER_DIR
    system('py listening.py ' + SAVEPATH + ' ' + hostStatus)

if __name__ == '__main__':
    run_sender(sys.argv[1], sys.argv[2])
    #sourcepath, FULL FILE PATH OF WHAT IS BEING SENT
    #True/False to know whether user or server
    sys.exit()