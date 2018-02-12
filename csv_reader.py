import csv

class CSV_Reader():
    def __init__(self, csv_file):
        with open(csv_file) as f:
            self.rpm_profile = csv.DictReader(f)
            self.data = []
            for row in self.rpm_profile:
                line = RPM(row['time'], row['rpm'])
                self.data.append(line)

    def __str__(self):
        string = "Time\t RPM\n"
        for row in self.data:
            string += row.__str__() + "\n"

        return string

class RPM():
    def __init__(self, time, rpm):
        self.time = time
        self.rpm = rpm

    def __str__(self):
        return "%s\t%s" %(self.time, self.rpm)

if __name__ == "__main__":
    profile = CSV_Reader("RPM_Profiles/SebringLeMansData.csv")
    print profile
