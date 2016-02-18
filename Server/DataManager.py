from pymongo import MongoClient
from Session import Sessions
from Session import Session
from Device import Devices
from Device import Device


class DataManager:

    def insertDevices(self, devices):
        connection = MongoClient('mongodb://192.168.1.65:27017')
        db = connection.vandrico.devices
        devicesString = str(devices)
       # print(devicesString)
        db.devices.insert(devicesString)

    def printAllData(self):
        connection = MongoClient('mongodb://192.168.1.65:27017')
        db = connection.vandrico.devices
        results = db.find()
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

#The following two lines are for testing
#dm = DataManager()
#dm.printAllData()
dvID = 1234
ses = Session("10:55",10,20,30,1.1,2.2)
ses2 = Session("12:55",15,10,40,1.1,2.2)
los = Sessions([ses, ses2])
dev = Device(dvID, los)
dev2 = Device(dvID,los)
devs = Devices([dev,dev2])
dm = DataManager()
dm.insertDevices(devs)