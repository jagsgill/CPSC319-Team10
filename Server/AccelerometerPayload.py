class AccelerometerPayload:
    def __init__(self, x, y, z):
        self.x = x
        self.y = y
        self.z = z

    def __str__(self):
        return ("\"accelerometer_payload\": {\"x\": " + str(self.x)
                + ",\"y\": " + str(self.y) + ",\"z\": " + str(self.z)+ "} ")


    def getX(self):
        return self.x

    def getY(self):
        return self.y

    def getZ(self):
        return self.z


# test
# accPyld = AccelerometerPayload(1,2,3)
# print(str(accPyld))