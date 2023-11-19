import sys
from TCP_server import Server


host = '10.122.231.147'
port = 3000

save_directory_1 = 'C:\\Users\\serap\\Desktop\\CodeJam2023\\Hive\\Hive_Database'
save_directory_2 = 'C:\\Users\\jrog1\\OneDrive\\Desktop\\CodeJam2023\\Hive\\Hive_Database\\manifest.txt'

sample_node_info = 'Root johnny2011-12-03T10:15:30'


def run_server(host, port, save_directory):
    server = Server()

    server.s_sock.bind((host,port)) # bind IPv4 adress and port on host PC. 
    print(f'Bound: {host}:{port}')

    while True:
        try: 
            server.est_conn(host,port)
            #                   ^arguments for print statement only

            name = server.recv_file_name(1024)
            server.est_conn(host,port)
            print(f'recieved file name: {name}')

            size = server.recv_file_size(1024)
            server.est_conn(host,port)
            print(f'recieved file size: {size}B')

            pull_req = server.server_recv(save_directory, name, size, 1024)

            print('Fuck')


            if pull_req:
                nodehash = name[:-4] # pull request
                server.est_conn(host,port) 
                server.server_send_zip(nodehash, save_directory, 1024)
            elif name[-4:] == '.zip': # push request
                server.est_conn(host,port)
                server.server_recv(save_directory, name, size, 1024)
                server.est_conn(host,port)
                server.server_send_manifest(save_directory, 1024)
            else: # get_manifest request
                server.est_conn(host,port) 
                server.server_send_manifest(save_directory, 1024)
        except IOError as e:
            print(f'IO Error caused server to close: {e}')
            break
        except Exception as e:
            print(f'Error caused server to close: {e}')
            break
    
    server.s_sock.close()

if __name__ == '__main__':
#    run_server(sys.argv[1], sys.argv[2], sys.argv[3])
    run_server(host, port, save_directory_2)