# -*- coding: utf-8 -*-

# Code from https://sakshambhatla.wordpress.com/2014/08/11/simple-mqtt-broker-and-client-in-python/
from DataManager import DataManager
import paho.mqtt.client as mqtt

# The callback for when the client receives a CONNACK response from the server.
# Subscribing in on_connect() means that if we lose the connection and
# reconnect then subscriptions will be renewed.
def on_connect(client, userdata, rc):
    print ('Connected with result code ', str(rc))
    client.subscribe('hello/world') # TODO : use topics devices publish to
    return

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    print ('Topic: ', msg.topic, '\nMessage: ', str(msg.payload))
    print("Peter:" + str(msg.payload))
    arr = [x.strip() for x in str(msg.payload).split(',')]
    devId = (arr[0])[2:]
    tmStmp = arr[1]
    x = arr[2]
    y = arr[3]
    z = arr[4]
    lat = arr[5]
    long = arr[6]
    dm = DataManager()
    dm.insertDeviceData(devId,tmStmp,x,y,z,lat,long)
    return

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

# ----------------------------------------------------
# Choose same broker as devices

# Heroku broker:
#client.username_pw_set('ehcxlgcl', 'AQsUmTw6wYee')
#client.connect('m10.cloudmqtt.com', 10975, 60)

# Google Cloud broker:
client.connect('130.211.153.252', 1883, 60) # unencrypted
# client.connect('130.211.153.252', 8883, 60) # encrypted

# Other public testing brokers:
#client.connect('test.mosquitto.org', 1883, 60)
#client.connect('mq.thingmq.com', 1883, 60)
#client.connect('broker.mqttdashboard.com', 1883, 60)

# ----------------------------------------------------

# Blocking call that processes network traffic, dispatches callbacks and
# handles reconnecting.
# Other loop*() functions are available that give a threaded interface and a
# manual interface.
client.loop_forever()
