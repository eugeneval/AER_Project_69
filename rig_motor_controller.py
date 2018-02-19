from Tkinter import *
from tkFileDialog import askopenfilename
import timeit, time

from csv_reader import CSV_Reader
import arduino_controller

root = Tk()

class Application(Frame):
    def __init__(self, master=None):
        Frame.__init__(self, master)
        self.root = master
        self.rpm = IntVar()

        self.createWidgets()

    def createWidgets(self):
        self.menu = Menu(self.root)
        self.root.config(menu=self.menu)

        self.profilemenu = Menu(self.menu)
        self.menu.add_cascade(label="Profile", menu=self.profilemenu)
        self.profilemenu.add_command(label="Load RPM Profile", command=self.loadCSV)

        self.arduinomenu = Menu(self.menu)
        self.menu.add_cascade(label="Arduino", menu=self.arduinomenu)
        self.connectmenu = Menu(self.menu)
        self.arduinomenu.add_cascade(label="Connect", menu=self.connectmenu)
        for port in arduino_controller.return_serial_ports():
            self.connectmenu.add_command(label=port, command=lambda p=str(port): self.connect(p))

        self.arduino_connected_label = Label(self.master, text="ARDUINO NOT CONNECTED", fg='red')
        self.arduino_connected_label.grid(column=0, row=0)

        self.tachometer_canvas = Canvas(self.root, width=200, height=200)
        self.tachometer = self.tachometer_canvas.create_arc(10, 10, 190, 190, style=ARC, start=180, width=5, extent=0)
        self.tachometer_canvas.grid(row=1, column=0)

        self.slider = Scale(self.root, from_=8640, to=0, length=200, variable=self.rpm, command=self.change_rpm)
        self.slider.grid(row=1, column=1)

        self.tachometer_label_1 = Label(self.master, textvariable=self.rpm).grid(row=2, column=0)
        self.tachometer_label_2 = Label(self.master, text="RPM").grid(row=3, column=0)

        self.stop = Button(self.root, text="STOP", command=self.stop, bg="red")
        self.stop.grid(row=4, column=2)



    def start(self):
        self.profile.timer.start()
        self.update_callback_id = self.master.after(5, self.update)

    def update(self):
        self.change_rpm(self.profile.getRPM())
        self.update_callback_id = self.master.after(5, self.update)

    def stop(self):
        if hasattr(self, 'update_callback_id'):
            self.after_cancel(self.update_callback_id)
            self.profile.timer.stop()
        self.change_rpm(0)

    def change_rpm(self, rpm):
        self.rpm.set(rpm)
        self.sendRPM(rpm)
        start = 180-self.rpm.get()*0.025
        end = self.rpm.get()*0.025
        self.tachometer_canvas.itemconfig(self.tachometer, start=start, extent=end)

    def loadCSV(self):
        csv_file = askopenfilename()
        self.profile = CSV_Reader(csv_file)

        self.start = Button(self.root, text="START", bg="green", command=self.start)
        self.start.grid(row=3, column=0)

    def connect(self, port):
        self.arduino = arduino_controller.connect(port)
        if hasattr(self, 'arduino'):
            self.arduino_connected_label.config(text="ARDUINO CONNECTED", fg='green')


    def sendRPM(self, rpm):
        if hasattr(self, 'arduino'):
            self.arduino.write(str(int(float(rpm)*0.474)))
            self.arduino.write("n")

if __name__ == "__main__":

    app = Application(master=root)
    app.mainloop()
