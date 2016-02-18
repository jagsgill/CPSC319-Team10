from Session import Sessions
from Session import Session

class Device:
    def __init__(self, devID, sess):
        self.deviceID = devID
        self.sessions = sess

    def __str__(self):
        return "{\"device_id\": " + str(self.deviceID) + "," + str(self.sessions) + "}"

class Devices:
    def __init__(self, listOfDevices):
        self.devices = listOfDevices

    def __str__(self):
        retString = "{\"devices\": ["
        for d in self.devices:
            retString += (str(d) + ",")
        retString = retString[:-1] + "]}"
        return retString
# test
#dvID = 1234
#ses = Session(9,1,2,3,100,200)
#ses2 = Session(9,1,0,13,500,300)
#los = Sessions([ses, ses2])
#dev = Device(dvID, los)
#dev2 = Device(dvID,los)
#devs = Devices([dev,dev2])
#print(str(devs))
