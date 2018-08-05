# NearBy Connection API
Nearby Connections enables advertising and discovery of nearby devices, as well as high-bandwidth low-latency encrypted data transfers between these devices in a fully-offline P2P manner.It achieves this by using a combination of classic Bluetooth, BLE, and Wi-Fi hotspots.
For more details [Click here](https://developers.google.com/nearby/connections/overview)

# P2P_STAR
P2P_STAR is a peer-to-peer strategy that supports a 1-to-N, or star-shaped, connection topology. In other words, this enables connecting devices within radio range (~100m) in a star shape, where each device can, at any given time, play the role of either a hub (where it can accept incoming connections from N other devices), or a spoke (where it can initiate an outgoing connection to a single hub), but not both. 

<img width="300px" height="300px" src="https://github.com/riontech-xten/NearByConnectionAPI/blob/master/P2P_STAR.png" height="600" alt="P2P_STAR"/>

This repostitory containt the sample code of `persistence connection across the activities` with chat implementation using the `P2P_STAR` Strategy. In this example you can see that how we can achived the persistence connection across the activities with implementation of a `singletone` class.

This example containt the module named `P2PStarConnection` which containt the Singletone class and some interfaces, you can directly import it into you project. 
