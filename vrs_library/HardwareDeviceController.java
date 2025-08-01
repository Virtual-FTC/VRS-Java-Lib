package com.qualcomm.robotcore.hardware;
import java.util.Timer;     // using Thread.sleep() caused strange timing issues, so used Timer instead.
import java.util.TimerTask;
import java.util.ArrayDeque;

public class HardwareDeviceController {
    // TODO We should adjust this to include servo commands as well, maybe split
    // into a motorDeviceController and a servoDeviceController


    public class MotorCommand {
        public int index;
        public double power;

        public MotorCommand(int index, double power) {
            this.power = power;
            this.index = index;
        }
    }

    final long FIRST_COMMAND_TIMEOUT    = 15000000; // in ns, 15ms,
                                                    // if the time since the first command in a batch 
                                                    // exceeds this, then batch is sent
                                                    // (15ms is reasonable enough according to 
                                                    // https://www.reddit.com/r/FTC/comments/elc4fl/autonomous_loop_speeds/
                                                    // maybe not ideal though)

    final long LAST_COMMAND_TIMEOUT     = 700000;   // in ns, 0.7ms
                                                    // if the time since the most recent command in a batch 
                                                    // exceeds this, then batch is sent
    
    long firstCommandTime;  // time of the first command in a batch
    long lastCommandTime;   // time of the most recent command in a batch
    ArrayDeque<MotorCommand> motorCommands;
    Timer timer;

    public HardwareDeviceController() {
        firstCommandTime = Long.MAX_VALUE;
        lastCommandTime = Long.MAX_VALUE;
        motorCommands = new ArrayDeque<>();
        timer = new Timer();
    }

    public void receiveMotorCommand(int index, double power) {
        // Thread.yield();
        motorCommands.add(new MotorCommand(index, power));
        
        long now = System.nanoTime();
        System.out.println("received motor command");
        System.out.println(now - lastCommandTime);
        if (lastCommandTime == Long.MAX_VALUE) {
            firstCommandTime = now;
        }
        lastCommandTime = now;
        // setTimeout(this::checkBatch, 1);
        timer.schedule(new TimerTask() {
                            @Override
                                public void run() {
                                    checkBatch();
                                }
                        }, 1);
    }

    private void checkBatch() {
        System.out.println("check batch");
        long now = System.nanoTime();
        long lastCommandElapsed = now - lastCommandTime;
        long firstCommandElapsed = now - firstCommandTime;

        if (lastCommandElapsed > LAST_COMMAND_TIMEOUT) {
            System.out.println("IN IF STATEMENT");
            firstCommandTime = Long.MAX_VALUE;
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
}
