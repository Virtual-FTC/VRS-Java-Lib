package com.qualcomm.robotcore.hardware;

// TODO Jenny thinks this should be made into an interface w/ default methods to more closely match FTC SDK.
// then have something like a GenericDcMotor that implements this (shouldn't need to override anything, just use defaults)

// TODO it may be worth it to create a SimpleDcMotor class since DcMotor extends SimpleDcMotor in FTC SDK.
public class DcMotor {

    public int index;
    private HardwareDeviceController hardwareDeviceController;

public  DcMotor(String name, String type, int maxrpm, int encoder, int i, HardwareDeviceController hardwareDeviceController) {
    index = i;
    this.hardwareDeviceController = hardwareDeviceController;
}

// TODO Jenny thinks this 'maybe' should be put into the HardwareDeviceController class,
// since its a motor command it should maybe be batched as well, same with setMode (which is not implemented yet).
public native void setDir(String dir);


public void setDirection(Direction dir) {
    setDir(dir.toString());
}

// TODO Jenny thinks that either javascript should send information to java when sensor values change,
// or that javascript writes to a file that java reads from (like write the robotconfig object json), 
// so we avoid the native method calls, whichever is faster.
public int getCurrentPosition() {
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
