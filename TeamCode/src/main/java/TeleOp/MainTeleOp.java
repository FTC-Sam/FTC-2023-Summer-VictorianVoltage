package TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;


import ftc.rogue.blacksmith.Scheduler;
import ftc.rogue.blacksmith.listeners.ReforgedGamepad;
import mechanisms.Camera;
import mechanisms.DriveTrain;

@TeleOp (name = "MainTeleOp")
public class MainTeleOp extends LinearOpMode {

    ReforgedGamepad driver;
    ReforgedGamepad codriver;
    Camera.BlueConeDetector blueConePipeline;



    DriveTrain dt;


    private void initialize() {
        driver = new ReforgedGamepad(gamepad1);
        codriver = new ReforgedGamepad(gamepad2);

        blueConePipeline = new Camera.BlueConeDetector(telemetry, hardwareMap);
        blueConePipeline.runPipeline();
        dt = new DriveTrain(hardwareMap);
    }




    @Override
    public void runOpMode() {
        initialize();
        while (opModeInInit()) {
            if (blueConePipeline.getDetectionState() == Camera.BlueConeDetector.DETECTION_STATE.LEFT) {
                telemetry.addData("pipeline if statement", "working");
                telemetry.update();
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
