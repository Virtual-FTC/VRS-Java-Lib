package com.qualcomm.robotcore.eventloop.opmode;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Autonomous {
    public String group() default "";
    public String name()  default "";
    public String preselectTeleOp() default "";
}