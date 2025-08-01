# Compiling
First, compile the vrs_library folder:
```javac -d vrs_lib_out .\vrs_library\*.java```

Then, turn the compiled classes into vrs_lib.jar:
```jar cf vrs_lib.jar -C vrs_lib_out . -C . org/json```

To compile the TeamCode folder, type 
```javac -d out -cp vrs_lib.jar .\TeamCode\*.java```

Then, turn the compiled classes into teamcode.jar:
```jar cf teamcode.jar -C out .```


# Jars in VRS-Cheerpj-UI
To use a different vrs_lib.jar, go to public\modules\RSM-CodeExecutionModule\android_studio\vrs_lib\vrs_lib.jar in VRS-Cheerpj-UI and replace the .jar file. The name must match.

To use a different teamcode.jar, start the local host on VRS-Cheerpj-UI, go to Android Studio Upload, then upload the teamcode.jar file.