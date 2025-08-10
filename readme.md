<h2>Socket</h2>

<h3>Execute : Server mode</h3>

java -Djdk.attach.allowAttachSelf=true -cp bin;jna\\* socket.server [port-number]

- jna folder: Copy jna jar files, swt jar file, and bthlib.dll (build bthlib with VS2022)
- [port-number]: (defualt) 9999

<h3>Execute : Client mode (socket)</h3>

java -Djdk.attach.allowAttachSelf=true -cp bin;jna\\* socket.client {server-name} [port-number]

- jna folder: Copy jna jar files, swt jar file, and bthlib.dll (build bthlib with VS2022)
- {server-name}: Host-Name or IP-Address (ex. vs2022)
- [port-number]: 9999 (defualt) 

<h3>Execute : Client mode (BLE socket)</h3>

java -Djdk.attach.allowAttachSelf=true -cp bin;jna\\* socket.client BLE {ble-name}

- jna folder: Copy jna jar files, swt jar file, and bthlib.dll (build bthlib with VS2022)
- {ble-name}: Bluetoot-Name or IP-Address (ex. vs2022)

<ul>
Setting:

jna\bthlib.{ble-name}.ini

[Detect]<br>
mac=(mac-address)

Specify mac as Ethernet adapter Bluetooth - Physical Address

ex) jna\bthlib.vs2022.ini

[Detect]<br>
mac=(00:1B:DC:06:3E:BD)

Inspect as ipconfig -all

Ethernet adapter Bluetooth xxxxx:<br>
&nbsp; &nbsp; Physical Address. . . . . . . . . : 00-1B-DC-06-3E-BD
</ul>

<h3>Require Software</h3>

- OpenJDK11U-jdk_x64_windows_hotspot_11.0.27_6.zip
- jna-5.14.0.zip
- swt-4.24-win32-win32-x86_64.zip

