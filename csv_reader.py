import csv, timeit

class CSV_Reader():
    def __init__(self, csv_file):
        with open(csv_file) as f:
            self.rpm_profile = csv.DictReader(f)
            self.data = []
            for row in self.rpm_profile:
                line = RPM(row['time'], row['rpm'])
                self.data.append(line)
        self.timer = Timer()

    def __str__(self):
        string = "Time\t RPM\n"
        for row in self.data:
            string += row.__str__() + "\n"

        return string

    def getRPM(self):
        time = self.timer.currentTime()
        for row in self.data:
            if row.time - 0.005 < time < row.time + 0.005:
                return row.rpm
        print "No row"
        return 0

class RPM():
    def __init__(self, time, rpm):
        self.time = float(time)
        self.rpm = float(rpm)

    def __str__(self):
        return "%s\t%s" %(self.time, self.rpm)

class Timer():
    def __init__(self):
        self.timeRunningThisLoop = 0
        self.timeRunningTotal = 0
        self.running = False

    def start(self):
        if not self.running:
            self.startTime = timeit.default_timer()
            self.running = True

    def currentTime(self):
        if self.running:
            self.timeRunningThisLoop = timeit.default_timer() - self.startTime
            return self.timeRunningThisLoop + self.timeRunningTotal
        else:
            return self.timeRunningTotal

    def stop(self):
        if self.running:
            self.timeRunningThisLoop = timeit.default_timer() - self.startTime
            self.timeRunningTotal += self.timeRunningThisLoop
            self.timeRunningThisLoop = 0
            self.startTime = 0
            self.running = False

if __name__ == "__main__":
    profile = CSV_Reader("RPM_Profiles/SebringLeMansData.csv")
    print profile
