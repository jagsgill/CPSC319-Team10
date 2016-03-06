from pymongo import MongoClient
from Session import Sessions
from Session import Session
from Device import Device
import json

class DataManager(json.JSONEncoder):


    def insertDeviceData(self, devId, time, x, y, z, lat, long):
        connection = MongoClient('localhost:27017')#'mongodb://192.168.1.65:27017')
        db = connection.vandrico
        dev = Device(devId, time, x, y, z, lat, long)
        serializedDev = json.dumps(dev.__dict__)
        deserializedDev = json.loads(serializedDev)
        db.devices.insert_one(deserializedDev)
        connection.close()

    # Returns an array of records matching the device ID
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

    # Returns true if device ID is in database
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
            print("Device ID: " + str(record['deviceID']))
            print("TimeStamp: " + str(record['timestamp']))
            print("x: " + str(record['x']))
            print("y: " + str(record['y']))
            print("z: " + str(record['z']))
            print("Latitude: " + str(record['latitude']))
            print("Longitude: " + str(record['longitude']))
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
dm = DataManager()
dm.printAllData()
dm.formatDataForWebApp()
#dm.insertDeviceData(123456,"2014-12-17T21:11:24.148Z",1.98877,2.222,3.444,6.99,7.64)