package com.phantom5702.javacvtest;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.GaussianBlur;
import static org.bytedeco.opencv.global.opencv_imgproc.cvPutText;

public class Smooth {

    public static void main(String[] args) {
//        System.out.println("test");
//        smooth("D:/test.jpg","D:/test2.jpg");

    }

    public static void smooth(String filename, String targetImg) {
        Mat image = imread(filename);
        if (image != null) {
            GaussianBlur(image, image, new Size(3, 3), 0);
            imwrite(targetImg, image);
        }
    }

}