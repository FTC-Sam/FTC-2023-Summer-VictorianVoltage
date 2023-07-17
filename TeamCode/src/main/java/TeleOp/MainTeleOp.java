package TeleOp;

import com.acmerobotics.dashboard.FtcDashboard;
import com.outoftheboxrobotics.photoncore.PhotonCore;
import com.outoftheboxrobotics.photoncore.PhotonLynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;


import mechanisms.Camera;
import mechanisms.DriveTrain;

@TeleOp (name = "MainTeleOp")
public class MainTeleOp extends LinearOpMode {
    Camera.BlueConeDetector blueConePipeline;
    private Servo rightServo;
    private Servo leftServo;
    DriveTrain dt;
    private final FtcDashboard dashboard = FtcDashboard.getInstance();


    private void initialize() {
        dashboard.setTelemetryTransmissionInterval(25);

        blueConePipeline = new Camera.BlueConeDetector(telemetry, hardwareMap);
        blueConePipeline.runPipeline();

        dt = new DriveTrain(hardwareMap, gamepad1);

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
            } else {
                rightServo.setPosition(0.5); //0.2
                leftServo.setPosition(0.5);
            }
        }
        while (opModeIsActive()) {
            dt.drive();
        }
    }
}
