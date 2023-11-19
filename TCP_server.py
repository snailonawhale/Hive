from os import listdir
from os.path import join, getsize, basename

from socket import AF_INET, SOCK_STREAM, socket


class Server(object):

    def __init__(self):
        self.s_sock = socket(AF_INET, SOCK_STREAM)

    def est_conn(self,host,port):
        #print(self.s_sock)
        self.s_sock.listen(1)
        print(f'Server listening on {host}:{port}')

        # Wait for server to recieve client
        self.c_sock, self.c_address = self.s_sock.accept()
        #self.s_sock.raddr = self.c_sock.laddr # is what the above line achieves now that it's no longer blocking
        #print(self.s_sock)
        #print(self.c_sock)
        print(f'Connected with client at {self.c_address} on IPv4 = {host} & port = {port}')

    def recv_file_name(self, buffer_size):
        # Receive file name and size
        name = self.c_sock.recv(buffer_size).decode('utf-8')
        return name
    
    def recv_file_size(self, buffer_size):
        # Receive file name and size
        size = int(self.c_sock.recv(buffer_size).decode('utf-8'))
        return size

    def server_recv(self, save_directory, name, size, buffer_size):
        pull_req = False
        manifest_path = join(save_directory,'manifest.txt')
        # This is the raw data of the file being recieved. 
        # The buffer size should depend on whether you are recieving a .txt of a .zip

        if str(name)[:-4] == 'update':
            hash = self.c_sock.recv(buffer_size).decode()
            # Create the new file name, read the ID, and append manifest
            with open(manifest_path, 'a') as manifest:
                manifest.write(str(hash) + '\n')
                manifest.close()
            print(f'New entry in manifest: {hash}')

        elif str(name)[-4:] == '.zip':
            ''' This is some code I'm not ready to delete...
            
            #Create a new file with name node_x.
            filename = 'node_' + num_files + file_type
            print(f"File {filename} received and identifier saved to manifest.txt")
            file_new = open(filename, 'w')
            while data:
                if not data:
                    break
                else:
                    file_new.write(data)
                    data = self.conn.recv(buffer_size).decode()
            '''
            # Receive and save the file to node folder
#            new_name = 'node_' + str(0) + str(file_type)
            path = join(save_directory, name)
            '''if exists(path):
                answer = input(f"The file {name} already exists. Do you want to override it? (Y/N): ").upper()
                if answer != 'Y':
                    print(f'File upload to database aborted. Old version of {name} remains in {save_directory}.')
                    return'''
            try:    
                with open(path, 'wb') as file:
                    data_recv = 0
                    while data_recv < size:
                        data = self.c_sock.recv(buffer_size)
                        data_recv += len(data)
                        file.write(data)
            except IOError as e:
                print(f'IO Error: {e}')
            print(f"File {name} received and saved to {path}")   
         
        elif str(name) == 'request.txt':
            print(f'{name} passed to server_recv')
        else:   
            pull_req = True
            print(str(name))
        return pull_req

    def RECIEVE(self, savepath, size, buffer_size):
        try:    
            with open(savepath, 'wb') as file:
                data_recv = 0
                while data_recv < size:
                    data = self.c_sock.recv(buffer_size)
                    data_recv += len(data)
                    file.write(data)
        except IOError as e:
                print(f'IO Error: {e}')
        print(f"File {basename(savepath)} received and saved to {savepath}")   

    def RCV_UPDATETXT(self, update_path, manifest_path, buffer_size):
        update_path = join(update_path,'update.txt')
        #after this line above both paths point to files, not directories
        with open(update_path, 'r') as file:
            hash = file.read(buffer_size)
            file.close()
        print('line read is ' + hash)
        # Create the new file name, read the ID, and append manifest
        with open(manifest_path, 'a') as manifest:
            manifest.write('\n' + hash)
            manifest.close()
        print(f'New entry in manifest: {hash}')


    def server_send_zip(self, full_hash, save_directory, buffer_size):
        file_found = find_zip_file(full_hash,save_directory)

        if file_found is not True:
            return
        else:
            path = join(save_directory,file_found)
            
            size = getsize(path) #size of file located at path.
            print(f'size of {file_found}: {size} Bytes')
            self.s_sock.send(file_found.encode())    # Send file name over socket
            self.s_sock.send(str(size).encode()) # Send file size over socket
        
            with open(path, 'rb') as file:
                data = file.read(buffer_size)
                while data:
                    self.s_sock.sendall(data)
                    data = file.read(buffer_size)
                print(f"File {file_found} sent successfully to {self.c_adress}")
                file.close()

    def server_send_manifest(self, save_directory, buffer_size):
        with open(save_directory, 'rb') as file:
            data = file.read(buffer_size)
            print(str(data))
        try:    
            print(self.c_address)
            print(self.c_sock)
            print(self.s_sock)
            while data:
                self.s_sock.sendall(str(data).encode('utf-8')) 
                data = file.read(buffer_size)              
            print(f"Manifest sent successfully")
            file.close()
        except IOError as e:
            print(f'Exception {e}')

    

def find_zip_file(full_hash, save_directory):
    # Determine what hash has been sent, and select it from a zip file in save directory.
    # Hashes are of the form: (Root) (user_hash) (node hash) (data)
    user_hash = full_hash.split(' ')[1] + '.zip'
    # Look in the folder "hive_database" for the hash that matches user_hash
    all_files = listdir(save_directory)

    for i in range(len(all_files)):
        # check if all_files[i] == user_hash, and break out of function if so. else, continue.
        if all_files[i] == user_hash:
            print(f'file {user_hash} found')
            return user_hash
        else:
            continue
    print(f'No file found {user_hash}.')
    return False


if __name__ == '__main__':
    test = Server()
    test.s_sock.bind(('10.122.231.147', 3000))
    test.est_conn('10.122.231.147', 3000)
    test.server_send_manifest('C:\\Users\\jrog1\\OneDrive\\Desktop\\CodeJam2023\\Hive\\Hive_Database\\manifest.txt', 1024)