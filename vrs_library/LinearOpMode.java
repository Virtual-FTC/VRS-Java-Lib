package com.qualcomm.robotcore.eventloop.opmode;

import com.qualcomm.robotcore.hardware.HardwareMap;

public class LinearOpMode extends OpMode {

    public HardwareMap hardwareMap = OpModeManager.getInstance().getHardwareMap();

    public LinearOpMode() {}

    public void runOpMode() throws InterruptedException {}

    public native void waitForStart();
    
}
