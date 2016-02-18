from AccelerometerPayload import AccelerometerPayload
from Location import Location

class Session:
    def __init__(self, tmstmp, x, y, z, lat, long):
        self.timestamp = tmstmp
        self.accelerometerPayload = AccelerometerPayload(x,y,z)
        self.location = Location(lat,long)

    def __str__(self):
        return ("{\"timestamp\": \"" + str(self.timestamp) + "\","
                + str(self.accelerometerPayload) + ","
                + str(self.location)+"}")

    def getTimestamp(self):
        return self.timestamp

    def getAccelerometerPayload(self):
        return self.accelerometerPayload

    def getLocation(self):
        return self.location

class Sessions:
    def __init__(self, listOfSessions):
        self.sessions = listOfSessions

    def __str__(self):
        retString = "\"sessions\": ["
        for s in self.sessions:
            retString += str(s) + ","
        retString = (retString[:-1]) + "]"
        return retString

# test
#ses = Session(9,1,2,3,100,200)
#ses2 = Session(9,1,0,13,500,300)
#los = Sessions([ses, ses2])
#print(str(los))