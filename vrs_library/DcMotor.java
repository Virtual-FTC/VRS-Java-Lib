package com.qualcomm.robotcore.hardware;




public class DcMotor {

    public int index;
    private HardwareDeviceController hardwareDeviceController;

public  DcMotor(String name, String type, int maxrpm, int encoder, int i, HardwareDeviceController hardwareDeviceController) {
    index = i;
    this.hardwareDeviceController = hardwareDeviceController;
}


public native void setDir(String dir);


public void setDirection(Direction dir) {
    setDir(dir.toString());

  //runJSCode()
}


public int getCurrentPosition() {
    Thread.yield();
    return getCurrentPositionInternal();
};

public native int getCurrentPositionInternal();

public void setPower(double power) {
    hardwareDeviceController.receiveMotorCommand(this.index, power);
}

public enum Direction {
    FORWARD,
    REVERSE
}



}
