import socket
import time
import random
from picamera import PiCamera
import sys as sys
import base64
import cv2
import torch as torch
import torch.nn as nn
import torchvision.transforms.functional as TF

#arsitektur LCNN Deeplearning
class Net(nn.Module):
    def __init__(self):
        super(Net, self).__init__()
        
        self.conv1 = nn.Conv2d(3, 8, 3, padding=0, stride=1)
        
        self.pool = nn.MaxPool2d(2, 2)
        
        self.fc1 = nn.Linear(23*23*8, 2)

        self.dropout = nn.Dropout(0.1)
        
        self.log_softmax = nn.LogSoftmax(dim=1)
        
        self.relu = nn.ReLU()
        
        self.sigmoid = nn.Sigmoid()

    def forward(self, x):
        
        x = self.pool(self.relu(self.conv1(x)))
        
        x = x.view(-1, 23*23*8)
        
        x = self.sigmoid(self.fc1(x))
        
        x = self.dropout(x)
        
        x = self.log_softmax(x)
        
        return x


model = Net()
model.load_state_dict(torch.load('model_rice.pt'))

def classify(path):
    start_time = time.time()
    #image = Image.open(path)
    img = cv2.imread(path)
    crop_img = img[0:48,0:48]
#     cv2.imshow("cropped", crop_img)
#     cv2.waitKey(0)
    
    x = TF.to_tensor(crop_img)
    x.unsqueeze_(0)
    print(x.shape)

    with torch.no_grad():
        output = model.forward(x)

    ps = torch.exp(output)
    ps = ps.data.numpy().squeeze()
    time_spent = time.time() - start_time
    print(ps)
    print("-- {:.3f} seconds --" .format(time_spent))
    if ps[0] < ps[1]:
        label = "gambar padi"
        return label
    else :
        label = "bukan padi"
        return label

#Instruksi Drone
x = 0
HEADERSIZE = 10
lat = random.randint(0, 3)
lang = random.randint(0, 3)
fullmsg = str(lat)+","+str(lang)
serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

serverSocket.bind(('', 9001))
print('bind')

serverSocket.listen(5)
print('listen')

while True:
    print('waiting...')
    try:
        connectionSocket, addr_info = serverSocket.accept()
        print('accept')
        print('--client information--')
        print(connectionSocket)
        
        data = connectionSocket.recv(1024).decode('utf-8')
        print(data)
 
        if (data == "Take Data Position"):
            connectionSocket.send(fullmsg .encode())
            connectionSocket.shutdown(socket.SHUT_RDWR)
            connectionSocket.close()
            print('connection closed')
            
        elif (data == "Take a Pict"):
            camera = PiCamera()
            time.sleep(1)
            camera.capture(f"/home/pi/Pictures/picamera/img{x}.jpg")
            print("Done taking a pict")
            #classification and getting lable
            label = classify(f"/home/pi/Pictures/picamera/img{x}.jpg")
            with open(f"/home/pi/Pictures/picamera/img{x}.jpg", "rb") as image2string:
                imageString = base64.b64encode(image2string.read())
            print(imageString)
            imageData = imageString.decode("ascii") + "," + label 
            with open(f"/home/pi/Pictures/picamera/img{x}.jpg", 'rb') as f:
                connectionSocket.send(imageData.encode())
            camera.stop_preview()
            camera.close()
            x += 1
#             #classification and getting lable
#             label = "bareland"
#             connectionSocket.send(label .encode())
            connectionSocket.shutdown(socket.SHUT_RDWR)
            connectionSocket.close()

    except KeyboardInterrupt:
        sys.exit(0)
        
       
#         while True :
#             time.sleep(3)
# #         mydata = "data_dummy"
#             msg = f"The time is: {time.time()}"
#             msg = f'{len(msg):<{HEADERSIZE}}' + msg
#             connectionSocket.send(msg.encode())
#             connectionSocket.shutdown(socket.SHUT_RDWR)
#             connectionSocket.close()
#           connectionSocket.close()
#     img = open("./img.jpg", 'wb')
#     img_data = connectionSocket.recv(BUFSIZE)
#     data = img_data
#     firstPacketLen = len(img_data)
#     print("receiving data...")
#     while len(img_data) > 0:
#     img_data = connectionSocket.recv(BUFSIZE)
#     data += img_data
#     if len(img_data) < firstPacketLen:
#         break
#     print("finish img recv")
#     img.write(data)
#     img.close()