cd %~dp0
Z:\dev\jdk-11.0.27+6_x64\bin\java -Djdk.attach.allowAttachSelf=true -cp bin;jna\* socket.client localhost %*
pause