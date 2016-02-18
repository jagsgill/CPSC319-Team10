class Location:
    def __init__(self, lat = 0, long = 0):
        self.latitude = lat
        self.longitude = long

    def getLat(self):
        return self.latitude

    def getLong(self):
        return self.longitude

    def __str__(self):
        return ("\"location\": {\"latitude\": " + str(self.latitude)
                +  ", \"longitude\": " + str(self.longitude) +"} ")

# test
# loc = Location(12312,13)
# print(str(loc))