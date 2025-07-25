package com.qualcomm.robotcore.eventloop.opmode;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class mainRunner { 
    public static native void setOpModeManager(OpModeManager opModeManager);

    public static void main(String[] args) {
        OpModeManager opModeManager = OpModeManager.getInstance();
        System.out.println(opModeManager.getOpModeList());

        // Start a thread for initializing the Java application instance in JavaScript
       new Thread(() -> {
            System.out.println("Starting Thread");
            System.out.println("DIFFERENT JAR RUNNING!!!");
            setOpModeManager(opModeManager);
        }).start();
    }
}   
