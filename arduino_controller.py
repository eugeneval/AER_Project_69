import serial, sys, glob
import serial.tools.list_ports

class Arduino_Interface(serial.Serial):
    def __init__(self, port):
        port = str(port)
        serial.Serial.__init__(self, port, 19200)

    def update(self, rpm):
        pass

def connect(port):
    p, s = port.split(" - ")
    return Arduino_Interface(p)

def return_serial_ports():
    return serial.tools.list_ports.comports()

def serial_ports():
    """ Lists serial port names

        :raises EnvironmentError:
            On unsupported or unknown platforms
        :returns:
            A list of the serial ports available on the system
    """
    if sys.platform.startswith('win'):
        ports = ['COM%s' % (i + 1) for i in range(256)]
    elif sys.platform.startswith('linux') or sys.platform.startswith('cygwin'):
        # this excludes your current terminal "/dev/tty"
        ports = glob.glob('/dev/tty[A-Za-z]*')
    elif sys.platform.startswith('darwin'):
        ports = glob.glob('/dev/tty.*')
    else:
        raise EnvironmentError('Unsupported platform')

    result = []
    for port in ports:
        try:
            s = serial.Serial(port)
            s.close()
            result.append(port)
        except (OSError, serial.SerialException):
            pass
    return result

if __name__ == "__main__":
    for p in serial.tools.list_ports.comports():
        print p
