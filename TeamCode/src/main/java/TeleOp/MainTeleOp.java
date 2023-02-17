package TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import ftc.rogue.blacksmith.BlackOp;
import ftc.rogue.blacksmith.Scheduler;
import ftc.rogue.blacksmith.annotations.CreateOnGo;
import ftc.rogue.blacksmith.listeners.Listener;
import ftc.rogue.blacksmith.listeners.ReforgedGamepad;
import mechanisms.Camera;
import mechanisms.DriveTrain;

@TeleOp (name = "MainTeleOp")
public class MainTeleOp extends BlackOp {

    ReforgedGamepad driver = new ReforgedGamepad(gamepad1);
    ReforgedGamepad codriver = new ReforgedGamepad(gamepad2);
    Camera.BlueConeDetector blueConePipeline = new Camera.BlueConeDetector();
    Camera blueConeDetector = new Camera(blueConePipeline);

    @CreateOnGo
    DriveTrain dt;





    @Override
    public void go() {
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
