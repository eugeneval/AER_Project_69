import serial

class Arduino_Interface(serial.Serial):
    def __init__(self, port):
        serial.Serial.__init__(port)

    def update(self, rpm):
        pass
