cd /d %~dp0
..\..\jdk-11.0.27+6_x64\bin\java -Djdk.attach.allowAttachSelf=true -DLOG=..\logs -cp bin;jna\* socket.client ble vs2022 %*
pause