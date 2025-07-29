package com.qualcomm.robotcore.hardware;

import java.util.LinkedList;

public class HardwareDeviceController {
    public class MotorCommand {
        public int index;
        public double power;

        public MotorCommand(int index, double power) {
            this.power = power;
            this.index = index;
        }
    }

    long lastCommandTime;
    LinkedList<MotorCommand> motorCommands;

    public HardwareDeviceController() {
        lastCommandTime = Long.MAX_VALUE;
        motorCommands = new LinkedList<>();
    }

    public void receiveMotorCommand(int index, double power) {
        Thread.yield();
        motorCommands.add(new MotorCommand(index, power));
        System.out.println("received motor command");
        System.out.println(System.nanoTime()- lastCommandTime);
        lastCommandTime = System.nanoTime();
        setTimeout(this::checkBatch, 1);
    }

    private void checkBatch() {
        System.out.println("check batch");
        long elapsed = System.nanoTime() - lastCommandTime;
        if (elapsed > 1000000) {
            System.out.println("IN THE TIMEOUT");
            System.out.println(elapsed);
            lastCommandTime = Long.MAX_VALUE;
            int len = motorCommands.size();
            int[] indexArray = new int[len];
            double[] powerArray = new double[len];
            
            for (int i = 0; i < len; i++) {
                MotorCommand motorCommmand = motorCommands.pop();
                indexArray[i] = motorCommmand.index;
                powerArray[i] = motorCommmand.power;
                // System.out.println("Sent to JS the following arrs");
                // System.out.println(indexArray[i]);
                // System.out.println(powerArray[i]);
            }
            
            sendBatchCommand(indexArray,powerArray);
        }
    }

    private native void sendBatchCommand(int[] indexArray, double[] powerArray);

    // exactly from https://stackoverflow.com/questions/26311470/what-is-the-equivalent-of-javascript-settimeout-in-java
    private static void setTimeout(Runnable runnable, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e) {
                System.err.println(e);
            }
        }).start();
    }
}
