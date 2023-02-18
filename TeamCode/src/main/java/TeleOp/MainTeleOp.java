package TeleOp;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import ftc.rogue.blacksmith.BlackOp;
import ftc.rogue.blacksmith.Scheduler;
import ftc.rogue.blacksmith.annotations.CreateOnGo;
import ftc.rogue.blacksmith.listeners.ReforgedGamepad;
import mechanisms.Camera;
import mechanisms.DriveTrain;

@TeleOp (name = "MainTeleOp")
public class MainTeleOp extends LinearOpMode {

    ReforgedGamepad driver;
    ReforgedGamepad codriver;
    Camera.BlueConeDetector blueConePipeline;


    @CreateOnGo
    DriveTrain dt;
    private OpenCvCamera camera;


    private void initialize() {
        driver = new ReforgedGamepad(gamepad1);
        codriver = new ReforgedGamepad(gamepad2);

        blueConePipeline = new Camera.BlueConeDetector(telemetry);
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "camera"), cameraMonitorViewId);
        //could potentially look at createInternalCamera
        camera.setPipeline(blueConePipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {
                throw new RuntimeException("Error opening camera! Error code " + errorCode);
            }
        });
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
