package TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;


import ftc.rogue.blacksmith.Scheduler;
import ftc.rogue.blacksmith.listeners.ReforgedGamepad;
import mechanisms.Camera;
import mechanisms.DriveTrain;

@TeleOp (name = "MainTeleOp")
public class MainTeleOp extends LinearOpMode {

    ReforgedGamepad driver;
    ReforgedGamepad codriver;
    Camera.BlueConeDetector blueConePipeline;
    private Servo rightServo;
    private Servo leftServo;



    DriveTrain dt;


    private void initialize() {
        driver = new ReforgedGamepad(gamepad1);
        codriver = new ReforgedGamepad(gamepad2);

        blueConePipeline = new Camera.BlueConeDetector(telemetry, hardwareMap);
        blueConePipeline.runPipeline();
        dt = new DriveTrain(hardwareMap);
        rightServo = hardwareMap.get(Servo.class, "rightServo");
        leftServo = hardwareMap.get(Servo.class, "leftServo");
    }




    @Override
    public void runOpMode() {
        initialize();
        while (opModeInInit()) {
            if (blueConePipeline.getDetectionState() == Camera.BlueConeDetector.DETECTION_STATE.LEFT) {
                rightServo.setPosition(0.34); //0.2
                leftServo.setPosition(0.66);
            }
            else {
                rightServo.setPosition(0.5); //0.2
                leftServo.setPosition(0.5);
            }
        }
        Scheduler.beforeEach(() -> {
            dt.drive(driver);
        });

        //implicit listener run

        Scheduler.launchOnStart(this, () -> {
            //aftereach, runs after every listener, normally pid
        });


    }


}
