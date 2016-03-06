from Session import Sessions
from Session import Session
from AccelerometerPayload import AccelerometerPayload
from Location import Location
import json

class Device:
    # def __init__(self, devid, sess):
    #     self.deviceID = devid
    #     #self.sessions = sess
    #     self.session = sess

    # def __str__(self):
    #     loc = Location(self.latitude,self.longitude)
    #     accPyld = AccelerometerPayload(self.x,self.y,self.z)
    #     ses =
    #     return "{\"device_id\": " + str(self.deviceID) + "," + str(self.session) + "}"

    # def getDevID(self):
    #     return self.deviceID
    #
    # def getSessions(self):
    #     return self.session
    #
    # def setSessions(self, sess):
    #     self.session = sess

    def __init__(self, devId, time,x,y,z,lat,long):
        self.deviceID = devId
        self.timestamp = time
        self.x = x
        self.y = y
        self.z = z
        self.latitude = lat
        self.longitude = long

    def getDeviceId(self):
        return self.deviceID

    def getTimestamp(self):
        return self.timestamp

    def getX(self):
        return self.x

    def getY(self):
        return self.y

    def getZ(self):
        return self.z

    def getLatitude(self):
        return self.latitude

    def getLongitude(self):
        return self.longitude

# test
#dvID = 1234
#ses = Session(9,1,2,3,100,200)
#ses2 = Session(9,1,0,13,500,300)
#los = Sessions([ses, ses2])
#dev = Device(dvID, los)
#dev2 = Device(dvID,los)
#devs = Devices([dev,dev2])
#print(str(devs))
