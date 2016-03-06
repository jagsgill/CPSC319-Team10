from pymongo import MongoClient
from Session import Sessions
from Session import Session
from AccelerometerPayload import AccelerometerPayload
from Location import Location
from Device import Device
import json
import pymongo

class DataManager(json.JSONEncoder):


    def insertDeviceData(self, devId, time, x, y, z, lat, long):
        connection = MongoClient('localhost:27017')#'mongodb://192.168.1.65:27017')
        db = connection.vandrico
        dev = Device(devId, time, x, y, z, lat, long)
        serializedDev = json.dumps(dev.__dict__)
        deserializedDev = json.loads(serializedDev)
        db.devices.insert_one(deserializedDev)
        connection.close()



    # def insertDevice(self, device):
    #     connection = MongoClient('localhost:27017')#'mongodb://192.168.1.65:27017')
    #     db = connection.vandrico
    #     sessions = device.getSessions()
    #     sesx = Session("2014-12-17T21:11:24.148Z",10,20,30,1.1,2.2)
    #     sesx2 = json.dumps(sesx.__dict__)
    #     sesx3 = json.loads(sesx2)
    #     sessions.append(sesx3)
        #print(sessions)
        # sessions.addSession({
        #                         "timestamp" : "2014-12-17T21:11:24.148Z",
        #                         "accelerometer_payload" : {
        #                                 "x" : 0.229,
        #                                 "y" : 0.571,
        #                                 "z" : 9.009
        #                         },
        #                         "location" : {
        #                                 "latitude" : 22222,
        #                                 "longitude" : 11111
        #                         }
        #                 })
        #device.setSessions(sessions)
        #print(sessions)

        #json = sessions.encodeSessions()
        #m = {'id': 2, 'name': 'hussain'}
        # n = json.dumps(device.__dict__)#m)
        # o = json.loads(n)


    # Returns device with matching id
    def selectDevicesFromDb(self, devID):
        connection = MongoClient('localhost:27017')#'mongodb://192.168.1.65:27017')
        db = connection.vandrico
        devices = []
        for doc in db.devices.find({"deviceID":devID}):
            dev = Device(str(doc['deviceID']),str(doc['timestamp']),str(doc['x']),str(doc['y']),
                         str(doc['z']),str(doc['latitude']),str(doc['longitude']))
            devices.append(dev)
        connection.close()
        return devices

    # Returns true if device id is in database
    def devInDb(self, devID):
        return self.selectDevicesFromDb(devID) is not None

    # Prints all data
    def printAllData(self):
        connection = MongoClient('localhost:27017')#'mongodb://192.168.1.65:27017')
        db = connection.vandrico
        results = db.devices.find()
        print()
        print('+-+-+-+-+-+-+-+-+-+-+-+-+-+-')
        for record in results:
            print("Device ID: " + str(record['device_id']))
            for subRecord in record['sessions']:
                print("TimeStamp: " + str(subRecord['timestamp']))
                print("x: " + str(subRecord['accelerometer_payload']['x']))
                print("y: " + str(subRecord['accelerometer_payload']['y']))
                print("z: " + str(subRecord['accelerometer_payload']['z']))
                print("Latitude: " + str(subRecord['location']['latitude']))
                print("Longitude: " + str(subRecord['location']['longitude']))
            print('+-+-+-+-+-+-+-+-+-+-+-+-+-+-')

        print()
        connection.close()

    # Return all device Ids
    def findUniqueIds(self):
        connection = MongoClient('localhost:27017')#'mongodb://192.168.1.65:27017')
        db = connection.vandrico
        ids = []
        results = db.devices.find()
        for record in results:
            if record['deviceID'] not in ids:
                ids.append(record['deviceID'])
                print("Device ID: " + str(record['deviceID']))
        connection.close()
        return ids

    # Returns a JSON string
    def formatDataForWebApp(self):
        ids = self.findUniqueIds()
        connection = MongoClient('localhost:27017')#'mongodb://192.168.1.65:27017')
        db = connection.vandrico
        retString = "{\"devices\":["
        for id in ids:
            records = self.selectDevicesFromDb(id)
            sessions = []
            for device in records:
                session = Session(device.getTimestamp(),device.getX(),device.getY(),device.getZ(),device.getLatitude(),device.getLongitude())
                sessions.append(session)
            retString += self.getDeviceAsJSON(id,Sessions(sessions))
            retString += ","
        connection.close()
        retString = retString[:-1]
        retString += "]}"
        print(retString)
        return retString

    # Takes device ID, pulls the matching sessions from the database and returns in a JSON string
    def getDeviceAsJSON(self,devID,sessions):
        retStr = "{\"device_id\": " + str(devID) + "," + str(sessions) + "}"
        print(retStr)
        return retStr

#The following two lines are for testing
#dm = DataManager()
#dm.printAllData()
#dvID = 1234
#dev = dm.selectDeviceFromDb(dvID)
#print(dev)

#print(dm.devInDb(dvID))
# ses = Session("2014-12-17T21:11:24.148Z",10,20,30,1.1,2.2)
# ses2 = Session("2014-12-17T21:11:24.145Z",15,10,40,7.1,6.2)
# los = Sessions([ses, ses2])
#dev = Device(6666, ses)
#dev2 = Device(1236,los)
#devs = Devices([dev,dev2])
dm = DataManager()
dm.formatDataForWebApp()
#dm.insertDeviceData(123456,"2014-12-17T21:11:24.148Z",1.98877,2.222,3.444,6.99,7.64)