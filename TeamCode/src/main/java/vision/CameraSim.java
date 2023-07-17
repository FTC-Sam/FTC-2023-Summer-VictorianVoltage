package vision;


import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;


public class CameraSim {

    public static class DistanceEstimator extends OpenCvPipeline {

        double dist = 0;
        double focal = focalLength(9.0, 0.6, 23);


        private Telemetry telemetry;

        public DistanceEstimator(Telemetry tele) {
            this.telemetry = tele;
        }


        @Override
        public Mat processFrame(Mat input) {
            try {
                double width_in_rf_img = 0;
                Mat end = input;
                Mat mat = input;

                Scalar lowHSV = new Scalar(110, 50, 70);
                Scalar highHSV = new Scalar(130, 255, 255);

                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HSV);
                Core.inRange(mat, lowHSV, highHSV, mat);

                Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_CLOSE, new Mat());
                Imgproc.blur(mat, mat, new Size(10, 10));

                ArrayList<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

                int largestIndex = 0;
                int largest = contours.get(0).toArray().length;


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

        public double focalLength(double measured_dist, double real_width, double width_in_rf_img) {
            return (width_in_rf_img * measured_dist) / real_width;
        }

        public double estimateDist(double focalL, double real_width, double width_in_rf_img) {
            return (focalL * real_width) / width_in_rf_img;
        }
    }
}
