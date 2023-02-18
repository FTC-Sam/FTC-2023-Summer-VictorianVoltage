package mechanisms;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;


public class Camera {
    public static class BlueConeDetector extends OpenCvPipeline {
        private final Telemetry telemetry;
        Mat mat = new Mat(); // matrix
        final Rect LEFT_ROI = new Rect(
                new Point(60, 35),
                new Point(120, 75)); //takes the top left point and bottom right point
        final Rect RIGHT_ROI = new Rect(
                new Point(140, 35),
                new Point(200, 75));
        final double PERCENT_COLOR_THRESHOLD = 0.4; // unlike the HSV which determines what is considered our desired color, this decides if we have enough in the frame to actually perform an action
        public enum DETECTION_STATE {
            BOTH,
            LEFT,
            RIGHT,
            NOTFOUND
        }
        private DETECTION_STATE detection_state;


        private OpenCvCamera camera;

        public BlueConeDetector(Telemetry t) {
            telemetry = t;
        }
        public void runPipeline() {
            int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
            camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "camera"), cameraMonitorViewId);
            //could potentially look at createInternalCamera
            camera.setPipeline(this);
            camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
                @Override
                public void onOpened() {
                    camera.startStreaming(480, 360, OpenCvCameraRotation.SIDEWAYS_LEFT);
                }

                @Override
                public void onError(int errorCode) {
                    throw new RuntimeException("Error opening camera! Error code " + errorCode);
                }
            });
        }


        @Override
        public Mat processFrame(Mat input) {
            Imgproc.cvtColor(input, mat, Imgproc.COLOR_RGB2HSV); // convert input into HSV and store in mat
            Scalar lowHSV = new Scalar(23, 50, 70);
            Scalar highHSV = new Scalar(32, 255, 255); //currently set to detect yellow

            Core.inRange(mat, lowHSV, highHSV, mat); // the stored mat is thresholded into black and white and stored, white is desired color
            Mat left = mat.submat(LEFT_ROI); //extracting ROI by taking the sub matrix (part we want)
            Mat right = mat.submat(RIGHT_ROI);

            double leftValue = Core.sumElems(left).val[0]/ LEFT_ROI.area()/255;
            double rightValue = Core.sumElems(right).val[0]/ RIGHT_ROI.area()/255; // first we sum up all the elements which are basically pixels with different values in the matrix
            // the values I'm pretty sure represent how much of a channel it is, currently it's just gray. Then we divide by area to get average color for entire thing, and then make it out of 255 (max value for grayscale) to get decimal percentage
            // the higher the average submatrix value the more white is in the image
            left.release();
            right.release(); //opencv's stuff doesn't get automatically recycled so we need to do this to keep memory okay
            // the guy from yt video put release and then called them again, no idea what release actually does in technicality

            telemetry.addData("Left raw value", (int) Core.sumElems(left).val[0]);
            telemetry.addData("Right raw value", (int) Core.sumElems(right).val[0]);
            telemetry.addData("Left percentage", Math.round(leftValue*100) + "%");
            telemetry.addData("Right percentage", Math.round(rightValue*100) + "%"); // debugging help

            boolean coneLeft = leftValue > PERCENT_COLOR_THRESHOLD;
            boolean coneRight = rightValue > PERCENT_COLOR_THRESHOLD;

            if (coneLeft && coneRight) {
                // two cones
                detection_state = DETECTION_STATE.BOTH;
                telemetry.addData("Cone Location", "BOTH");
            }
            else if (coneLeft) {
                // one cone left
                detection_state = DETECTION_STATE.LEFT;
                telemetry.addData("Cone Location", "LEFT");
            }

            else if (coneRight){
                // one cone right
                detection_state = DETECTION_STATE.RIGHT;
                telemetry.addData("Cone Location", "RIGHT");
            }

            else {
                detection_state = DETECTION_STATE.NOTFOUND;
                telemetry.addData("Cone Location", "NOT FOUND");
            }
            telemetry.update();

            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGB); // trying to draw some colored boxes around ROI
            Scalar colorNo = new Scalar(255, 0 ,0);
            Scalar colorYes = new Scalar(0, 255, 0);

            if (detection_state == DETECTION_STATE.BOTH) {
                Imgproc.rectangle(mat, LEFT_ROI, colorYes); // parameters go: what image, what rectangle, what color
                Imgproc.rectangle(mat, RIGHT_ROI, colorYes);
            }
            else if (detection_state == DETECTION_STATE.LEFT) {
                Imgproc.rectangle(mat, LEFT_ROI, colorYes);
                Imgproc.rectangle(mat, RIGHT_ROI, colorNo);
            }
            else if (detection_state == DETECTION_STATE.RIGHT) {
                Imgproc.rectangle(mat, RIGHT_ROI, colorYes);
                Imgproc.rectangle(mat, LEFT_ROI, colorNo);
            }
            else {
                Imgproc.rectangle(mat, RIGHT_ROI, colorNo);
                Imgproc.rectangle(mat, LEFT_ROI, colorNo);
            } // draws colored bounding box

            return mat;
        }

        public DETECTION_STATE getDetectionState() {
            return detection_state;
        }
    }






    // we can keep all detection in one class
    public static class RedConeDetector extends OpenCvPipeline {
        @Override
        public Mat processFrame(Mat input) {
            return null;
        }
    }
}
