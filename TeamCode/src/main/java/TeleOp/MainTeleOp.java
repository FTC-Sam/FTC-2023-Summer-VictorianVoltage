package TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import ftc.rogue.blacksmith.BlackOp;
import ftc.rogue.blacksmith.Scheduler;
import ftc.rogue.blacksmith.annotations.CreateOnGo;
import ftc.rogue.blacksmith.listeners.ReforgedGamepad;
import mechanisms.Camera;
import mechanisms.DriveTrain;

@TeleOp (name = "MainTeleOp")
public class MainTeleOp extends BlackOp {

    ReforgedGamepad driver;
    ReforgedGamepad codriver;
    Camera.BlueConeDetector blueConePipeline;


    @CreateOnGo
    DriveTrain dt;

    private void initialize() {
        driver = new ReforgedGamepad(gamepad1);
        codriver = new ReforgedGamepad(gamepad2);

        blueConePipeline = new Camera.BlueConeDetector();
        blueConePipeline.runPipeline();
    }




    @Override
    public void go() {
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
