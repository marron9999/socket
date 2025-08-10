<h2>Socket</h2>

<h3>Execute</h3>


</h4>Server mode</h4>

java -Djdk.attach.allowAttachSelf=true -cp bin;jna\\* socket.server [port-number]

- jna folder: Copy jna jar files, swt jar file, and bthlib.dll (build bthlib with VS2022)
- [port-number]: (defualt) 9999

</h4>Client mode (socket)</h4>

java -Djdk.attach.allowAttachSelf=true -cp bin;jna\\* socket.client {server-name} [port-number]

- jna folder: Copy jna jar files, swt jar file, and bthlib.dll (build bthlib with VS2022)
- {server-name}: Host-Name or IP-Address (ex. SRV001)
- [port-number]: 9999 (defualt) 

</h4>Client mode (BLE socket)</h4>

java -Djdk.attach.allowAttachSelf=true -cp bin;jna\\* socket.client BLE {ble-name}

- jna folder: Copy jna jar files, swt jar file, and bthlib.dll (build bthlib with VS2022)
- {ble-name}: Bluetoot-Name or IP-Address (ex. SRV001)

<h3>Require Software</h3>

- OpenJDK11U-jdk_x64_windows_hotspot_11.0.27_6.zip
- jna-5.14.0.zip
- swt-4.24-win32-win32-x86_64.zip
- apache-tomcat-10.1.43-embed.zip
