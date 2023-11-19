import sys
from TCP_client import Client
from os.path import join, abspath, basename

host = '10.122.231.147'
port = 3000

request_path_1 = "C:\\Users\\jrog1\\OneDrive\\Desktop\\CodeJam2023\\request.txt"

sample_path = 'C:\\Users\\jrog1\\OneDrive\\Desktop\\CodeJam2023\\Hive\\main_files'

sample_node_info = 'Root johnny2011-12-03T10:15:30'

# Note: all 3 of the client 0 connections are for communication to the server
# what type of request is called. For pull(), a .zip file is sent first, thus
# the connection is known to be a pull. For get_manifest, request.txt file is
# sent first, thus clearly denoting it as a get manifest request. For a pull,
# it is the else clause. 

def push(host, port, folder_path, zip_file_name):

    client0 = Client()
    client0.est_conn(host,port)
    client0.send_file_name(zip_file_name)
    client0.c_sock.close()

    client1 = Client()
    client1.est_conn(host,port)
    client1.client_send(join(folder_path,zip_file_name), 1024) # zip file send
    client1.c_sock.close()

    client2 = Client()
    client2.est_conn(host, port)
    client2.client_send(join(folder_path,'update.txt'), 1024) # update.txt file send
    client2.c_sock.close()    

    client3 = Client()
    client3.est_conn(host, port)
    client3.client_recv(abspath('client_side.py'), 1024) 
    # puts manifest in folder that script is being run from
    client3.c_sock.close()

def pull(host, port, nodehash_path):

    client0 = Client()
    client0.est_conn(host, port)
    client0.send_file_name(nodehash_path)
    client0.c_sock.close()

    client1 = Client()
    client1.est_conn(host, port)
    client1.client_send(nodehash_path, 1024)
    client1.c_sock.close()

    client2 = Client()
    client2.est_conn(host, port)
    client2.client_recv(abspath('client_side.py'), basename(nodehash_path), 1024) 
    # puts hash.zip in folder that script is being run from
    client2.c_sock.close()

def get_manifest(host, port, request_path):
    client0 = Client()
    client0.est_conn(host, port)
    client0.send_file_name(request_path)
    client0.c_sock.close()
    print(f'sent file name: {basename(request_path)}')

    client4 = Client()
    client4.est_conn(host, port)
    client4.send_file_size(request_path)
    client4.c_sock.close()
    print(f'sent file size: {basename(request_path)}')

    client1 = Client() #sends request.txt
    client1.est_conn(host, port)
    client1.client_send(request_path, 1024)
    client1.c_sock.close()
    print(f'sent file: {request_path}')

    try:
        client2 = Client() # recieves manifest
        client2.est_conn(host, port)
        client2.client_recv(sample_path, 'manifest.txt', 1024) 
        # puts manifest in folder that script is being run from
        client2.c_sock.close()
        print(f'Recieved manifest.txt at {sample_path}')
    except Exception as e:
        print(f'Error: {e}')
        client2.c_sock.close()
    
if __name__ =='__main__':
#    get_manifest(sys.argv[1], sys.argv[2], sys.argv[3])
    get_manifest(host, port, request_path_1)


