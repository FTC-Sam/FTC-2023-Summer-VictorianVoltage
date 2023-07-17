package mechanisms;


import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;


public class Camera {
    public static class BlueConeDetector extends OpenCvPipeline {
        private final HardwareMap hardwareMap;
        private final Telemetry telemetry;
        Mat mat = new Mat(); // matrix
        final Rect LEFT_ROI = new Rect(
                new Point(1, 1),
                new Point(319, 479)); //takes the top left point and bottom right point ** 0,0 origin starts at top right
        final Rect RIGHT_ROI = new Rect(
                new Point(320, 1),
                new Point(639, 479));
        final double PERCENT_COLOR_THRESHOLD = 0.3; // unlike the HSV which determines what is considered our desired color, this decides if we have enough in the frame to actually perform an action
        public enum DETECTION_STATE {
            BOTH,
            LEFT,
            RIGHT,
            NOTFOUND
        }
        private DETECTION_STATE detection_state;


        private OpenCvCamera camera;

        public BlueConeDetector(Telemetry t, HardwareMap hM) {
            telemetry = t;
            hardwareMap = hM;
        }
        public void runPipeline() {
            int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
            camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "camera"), cameraMonitorViewId);
            //could potentially look at createInternalCamera
            camera.setPipeline(this);
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
        public Mat processFrame(Mat input) {
            Imgproc.cvtColor(input, mat, Imgproc.COLOR_RGB2HSV); // convert input into HSV and store in mat
            Scalar lowHSV = new Scalar(110, 50, 70);
            Scalar highHSV = new Scalar(130, 255, 255); //currently set to detect blue

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



    public static class DistanceEstimator extends OpenCvPipeline{
        double dist = 0;
        double focal = focalLength(9.0, 0.6, 23);
        private OpenCvCamera camera;
        private HardwareMap hardwareMap;
        private Telemetry telemetry;
        public DistanceEstimator(HardwareMap hw, Telemetry tele){
            this.hardwareMap = hw;
            this.telemetry = tele;
        }
        public void runPipeline() {
            int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
            camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "camera"), cameraMonitorViewId);
            //could potentially look at createInternalCamera
            camera.setPipeline(this);
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
        public Mat processFrame(Mat input){
            try{       //requires try catch as if we don't find contours we get an error
            Mat end = input; //the frames that will be streamed
            Mat mat = input; //the frames that are altered to find our info

            Scalar lowHSV = new Scalar(110, 50, 70);
            Scalar highHSV = new Scalar(130, 255, 255);

            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HSV);
            Core.inRange(mat, lowHSV, highHSV, mat);  // thresholding

            Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_CLOSE, new Mat());
            Imgproc.blur(mat, mat, new Size(10, 10));// removing false negatives and using gaussian blur

                //finding all the contours in the image
            ArrayList<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

            int largestIndex = 0;
            int largest = contours.get(0).toArray().length;

            //finding the largest contour form the frame
            for (int i = 0; i < contours.size(); i++) {
                int currentSize = contours.get(i).toArray().length;
                if (currentSize > largest) {

                    largest = currentSize;
                    largestIndex = i;
                }

            }


            //Draw rectangle on largest contours

            MatOfPoint2f areaPoints = new MatOfPoint2f(contours.get(largestIndex).toArray());

            Rect rect = Imgproc.boundingRect(areaPoints);

            Imgproc.rectangle(end, rect, new Scalar(255, 0, 0));
            dist = estimateDist(focal, 0.6, rect.width);
            telemetry.addData("Distance: ", dist);
            telemetry.addData("pixle width", rect.width);
            telemetry.update();
            return end;
        } catch (IndexOutOfBoundsException e) {
            telemetry.addData("No objects found", 0);
        }
            return input;
        }
        //algos
        public static double focalLength(double measured_dist, double real_width, double width_in_rf_img ){
            return (width_in_rf_img * measured_dist)/ real_width;
        }
        public static double estimateDist(double focalL , double real_width, double width_in_rf_img ){
            return (focalL*real_width)/ width_in_rf_img;
        }
    }
}
