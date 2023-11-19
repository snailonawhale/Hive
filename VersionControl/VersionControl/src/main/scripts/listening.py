import sys
from os import remove, system
from TCP_client import Client
from TCP_server import Server
from os.path import join, abspath, basename, getsize

SERVER_DIR = 'C:\\Users\\jrog1\\OneDrive\\Desktop\\CodeJam2023\\Hive\\Hive_Database'#\\manifest.txt
#JAVA_DIR_PATH = 'C:\\Users\\jrog1\\OneDrive\\Desktop\\CodeJam2023\\Hive\\JAVA_DIR_SAMPLE' #NOTE: in the future, System.getPropert("user.dir") + "\\src\\main\\scripts\\" is the directory
JAVA_DIR_PATH = r'C:\Users\vince\Desktop\VersionControl\VersionControl\src\main\scripts\staging'
host = '10.122.33.50'
port = 3000
#above is the listener
#below is the sender
other_host = '10.122.231.147'
other_port = 3001 #NOTE: never used for listening, only to SEND


def run_listener(savepath, hostStatus):
    print('running listener, saving to ' + savepath + ' hostStatus = ' + hostStatus)
    listener = Server()

    listener.s_sock.bind((host,port)) # bind IPv4 adress and port on host PC. 
    print(f'Bound: {host}:{port}')

    #while True:###REMOVE THIS???
    listener.est_conn(host, port)#<---arguments for print statement only
    name = listener.recv_file_name(1024)#we need the name to identify our mass of data (file)
    print(f'recieved file name: {name}')

    listener.est_conn(host, port)#<---arguments for print statement only
    size = listener.recv_file_size(1024)#we need the size to to use .RECIEVE to make our file.
    print(f'recieved file size: {size}')

    listener.est_conn(host, port)#<---arguments for print statement only
    #listener.server_recv(savepath, basename(savepath), getsize(savepath), 1024)
    listener.RECIEVE(join(savepath, name), size, 1024)
    print(f'recieved file')

    listener.s_sock.close()
    ##listener.s_sock.close()
    #file is of size {size}, name {name} in directory {save_directory}
    #toBeSentPath = ''
    toBeSentName = ''
    #toBeSentSize = 0
    
    if hostStatus == 'H':#NOTE here SERVER_DIR == savepath bc server side
        if name[-4:] == '.zip':#join...update.txt' is what the NEXT file it recieves will be saved as
            system('py listening.py ' + SERVER_DIR + ' ' + hostStatus)
            #run_listener(SERVER_DIR, hostStatus)
            return#handle zip, client sends .zip and update.txt, host sends manifest.txt back
        elif name == 'update.txt':
            listener.RCV_UPDATETXT(SERVER_DIR, join(SERVER_DIR, 'manifest.txt'), 1024)
            remove(join(savepath, 'update.txt'))
            #the line above incorporates the new line into manifest.txt
            #, NOW, they send back manifest.txt
            toBeSentName = 'manifest.txt'
            #toBeSentPath = save_directory_2
            #toBeSentSize = getsize(join(toBeSentPath,toBeSentName))
        elif name == 'request.txt':#just a flag, once this case has been passed it's done its job
            #print('removing ' + savepath + '/+/request.txt')
            remove(join(savepath, 'request.txt'))
            toBeSentName = 'manifest.txt'
            #toBeSentPath = save_directory_2
            #toBeSentSize = getsize(join(toBeSentPath,toBeSentName))
        else:
            #we recieve hash.txt, we send back hash.zip
            toBeSentName = name[:-4] + '.zip'
            remove(join(savepath, name))
            #toBeSentPath = save_directory_2
            #toBeSentSize = getsize(join(toBeSentPath, toBeSentName))
            #remove file join(toBeSentPath, toBeSentName)

        #ONLY the host goes from listening to sending...
        #client ALWAYS goes from sending to listening, then nothing.
        #send.run_sender(join(SERVER_DIR, toBeSentName), hostStatus)#, toBeSentSize
        system('py send.py ' + join(SERVER_DIR, toBeSentName) + ' ' + hostStatus)
    else:
        if name == 'manifest.txt':#note to java: delete manifest.txt BEFORE requesting udpate
            pass #if manifest.txt is now in save_directory, java's turn to take control
        else:#we just got the zip file we requested
            pass#hash.zip was just recieved in save_directory as requested
    
if __name__ == '__main__':
    run_listener(sys.argv[1], sys.argv[2])
    #when run script, first arg is where unknown file will be downloaded
    #second arg is True/False, hostStatus
    sys.exit()