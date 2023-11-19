
from os.path import join, getsize, basename
import socket

class Client(object):

    def __init__(self):
        self.c_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 

    def est_conn(self,host,port):
        #print(self.c_sock)    
        self.c_sock.connect((host,port))
        #print(self.c_sock)
        print(f'Connected to server at IPv4 = {host} & port = {port}')

    def send_file_name(self, local_path):
        name = basename(local_path)
        self.c_sock.send(name.encode('utf-8'))    # Send file name over socket

    def send_file_size(self, local_path):
        self.c_sock.send(str(getsize(local_path)).encode('utf-8'))

    def client_send(self, local_path, buffer_size):
        file_type = basename(local_path)[-4:]
        ''' # Send the file data
        with open(local_path, 'rb') as file:
            for data in iter(lambda: file.read(buffer_size), b''):
                self.c_sock.sendall(data)'''
        try:
            if file_type == '.txt':
                with open(local_path, 'rb') as file:
                    data = file.read(buffer_size) 
                    print(data)
                    while data:
                        if not data:
                            break
                        else:
                            self.c_sock.sendall(data) 
                            data = file.read(buffer_size)

                        print(f".txt file sent successfully")
                        file.close() 
            
            elif file_type =='.zip':
                with open(local_path, 'rb') as file:
                    data = file.read(buffer_size) 
                    while data:
                            self.c_sock.sendall(data) 
                            data = file.read() 
                    # File is closed after data is sent 
                    print(f"ZIP File sent successfully")
                    file.close()
        except IOError as e:
            print(f'IO error occurred: {e}')
        except:
            print("Something's wrong...")


    def client_recv(self, local_path, name, buffer_size):

#        print(f'Recieved {name} ({size}B) from {self.c_adress}')

        recieved_to = join(local_path, name)

        try:    
            with open(recieved_to, 'wb') as file:
                data = self.c_sock.recv(buffer_size)
                while data:
                    file.write(str(data).decode('utf-8'))                    
                    data = self.c_sock.recv(buffer_size)
            print(f"File {name} received and saved to {recieved_to}")
            file.close()
        except IOError as e:
            print(f'IO Error: {e}')

if __name__ == '__main__':
    testcli = Client()
    testcli.est_conn('10.122.231.147', 3000)
    testcli.client_recv('C:\\Users\\jrog1\\OneDrive\\Desktop\\CodeJam2023\\Hive\\main_files', 'manifest.txt', 1024)



