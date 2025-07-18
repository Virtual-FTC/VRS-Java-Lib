package com.qualcomm.robotcore.eventloop.opmode;

import com.qualcomm.robotcore.hardware.HardwareMap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class OpModeManager {
    private static OpModeManager manager;
    private HardwareMap hardwareMap = new HardwareMap();

    private Thread activeOpModeThread = null;

    final int MS_BEFORE_FORCE_STOP_AFTER_STOP_REQUESTED = 900;  // taken from OpModeInternal in FTC SDK
    private static String pathToTeamCodeJar = "/str/program.jar";

    enum OpModeEndCause {
        NATURAL, 
        FORCED
    };

    private OpModeManager() {
    }

    /**
     * Tells cheerpj when an op mode has finished running naturally (no interrupting by stop program button) or forced
     */
    private native void tellJSOpModeFinished(String endCause);

    private void tellJSOpModeFinished(OpModeEndCause endCause) {
        tellJSOpModeFinished(endCause.toString());
    }

    /**
     * Exits system (in case we need to terminate thread etc.)
     */
    public void systemExit() {
        System.exit(0);
    }

    public native boolean ReadyToStart();

    // TODO Jenny thinks this should
    // probably either be part of the LinearOpMode class
    // or have this not be a native method (like the js should signal when
    // the opmode should stop, constantly pinging the js seems far too costly)
    public native boolean opModeIsActive(); 

    public static OpModeManager getInstance() {
        if (manager == null) {
            manager = new OpModeManager();
        }

        return manager;
    }

    public void run(String opModeName) {
        // String currentOp = "Autonomous";
        String currentOp = getChosenOpMode();
        Class<?> clazz = null;

        if (currentOp.equals("Teleop")) {
            try {
                clazz = findTeleopClasses();
            }

            catch (TeleopClassNotFoundError e) {
                e.printStackTrace();
            }
        }

        else {
            try {
                clazz = findAutonomousClasses();
            }

            catch (AutonomousClassNotFoundError e) {
                e.printStackTrace();
            }
        }

        if (clazz.isAssignableFrom(OpMode.class)) {
            // iterative op mode
            try {
                OpMode runnable = (OpMode) clazz.getDeclaredConstructor().newInstance();
                runnable.init();

                while (true) {
                    runnable.init_loop();
                    if (ReadyToStart()) {
                        break;
                    }

                }

                runnable.start();
                while (opModeIsActive()) {
                    runnable.loop();
                    //
                    break;

                }
                runnable.stop();

            }

            catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                e.printStackTrace();
            }

        }

        else {
            // assuming its autonomous
            try {
                LinearOpMode runnable = (LinearOpMode) clazz.getDeclaredConstructor().newInstance();
                // currentOpMode = runnable;
                System.out.println("starting op mode thread");

                Thread opModeThread = new Thread(() ->
                    {
                        System.out.println("Running op mode thread now");
                        try {
                            runnable.runOpMode();
                        } catch (Exception e) {
                            System.out.println(e);
                        }

                        System.out.println("finished running current op naturally!!!");
                        tellJSOpModeFinished(OpModeEndCause.NATURAL);
                    }
                );

                opModeThread.start();
                activeOpModeThread = opModeThread;
            }

            catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Stops the active op mode in roughly the same way ftc sdk does 
     * (see OpModeManagerImpl class)
     */
    @SuppressWarnings({"removal"})
    public void stopActiveOpMode() {
        if (activeOpModeThread == null) return;
        // TODO put all motors in safe state
        // idk if this is necessary because once it is in the stop match
        // state the sim prevents motors from moving to begin with
        // ftc sdk does this though

        // interrupt first, then force kill.
        activeOpModeThread.interrupt();
        try {
            Thread.sleep(MS_BEFORE_FORCE_STOP_AFTER_STOP_REQUESTED);
        } catch (Exception e) {
            System.out.println(e);
        }
        activeOpModeThread.stop();
        tellJSOpModeFinished(OpModeEndCause.FORCED);
    }

    public String getOpModeList() {
        return """
                [
                {
                    "name": "Drive Forward",
                    "playMode": "Autonomous",
                    "group": "auto"
                },
                {
                    "name": "TeleOpName1",
                    "playMode": "TeleOp",
                    "group": "drive"
                },
                {
                    "name": "TeleOpName2",
                    "playMode": "TeleOp",
                    "group": "drive"
                }
                ]
                        """;
    }

    public native String getChosenOpMode();

    public HardwareMap getHardwareMap() {
        return hardwareMap;
    }

    public static Class<?> findAutonomousClasses() throws AutonomousClassNotFoundError {
        // String workingDir = System.getProperty("user.dir");
        // System.out.println("Working directory: " + workingDir);

        File jarFile = new File(pathToTeamCodeJar);
        
        try {
            URL jarLocation = jarFile.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[] { jarLocation });
            List<Class<?>> classes = new ArrayList<>();

            try {
                JarFile jar = new JarFile(jarFile);
                String directoryPrefix = "org/firstinspires/ftc/teamcode";
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (name.startsWith(directoryPrefix) && !entry.isDirectory()) {
                        // Print just the file name, or the full path
                        String className = name.replace("/", ".").replace(".class", "");
                        System.out.println(className);
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Autonomous.class)) {
                                System.out.println("found you! Autonomous is in" + className);
                                return clazz;

                            }

                        }

                        catch (ClassNotFoundException e) {
                            System.out.println("no such class found :(");
                        }
                    }
                }

                throw new AutonomousClassNotFoundError();
            }

            catch (IOException e) {
                e.printStackTrace();
                throw new AutonomousClassNotFoundError();

            }
        }

        catch (MalformedURLException e) {
            e.printStackTrace();
            throw new AutonomousClassNotFoundError();
        }

    }

    public static Class<?> findTeleopClasses() throws TeleopClassNotFoundError {
        URL jarLocation = mainRunner.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            File jarFile = new File(jarLocation.toURI());
            URLClassLoader classLoader = new URLClassLoader(new URL[] { jarLocation });
            List<Class<?>> classes = new ArrayList<>();

            try {
                JarFile jar = new JarFile(jarFile);
                String directoryPrefix = "TeamCode";
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (name.startsWith(directoryPrefix) && !entry.isDirectory()) {
                        // Print just the file name, or the full path
                        String className = name.replace("/", ".").replace(".class", "");
                        System.out.println(className);
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(TeleOp.class)) {
                                System.out.println("found you! Teleop is in" + className);
                                return clazz;

                            }

                        }

                        catch (ClassNotFoundException e) {
                            System.out.println("no such class found :(");
                        }
                    }
                }

                throw new TeleopClassNotFoundError();
            }

            catch (IOException e) {
                e.printStackTrace();
                throw new TeleopClassNotFoundError();

            }
        }

        catch (URISyntaxException e) {
            e.printStackTrace();
            throw new TeleopClassNotFoundError();
        }

    }

    public static class AutonomousClassNotFoundError extends Exception {
        public AutonomousClassNotFoundError() {
            super("Autonomous Class was not found. Check to make sure you used the @Autonomous annnotation");
        }
    }

    public static class TeleopClassNotFoundError extends Exception {
        public TeleopClassNotFoundError() {
            super("Teleop Class was not found. Check to make sure you used the @Autonomous annnotation");
        }
    }

}
