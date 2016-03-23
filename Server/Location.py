import json

class Location:
    def __init__(self, lat, long):
        self.latitude = lat
        self.longitude = long

    def getLat(self):
        return self.latitude

    def getLong(self):
        return self.longitude

    def setLat(self, lat):
        self.latitude = lat

    def setLong(self, long):
        self.longitude = long

    def __str__(self):
        return ("\"location\": {\"latitude\": " + str(self.latitude)
                +  ", \"longitude\": " + str(self.longitude) +"} ")

# test
# loc = Location(12312,13)
# print(str(loc))