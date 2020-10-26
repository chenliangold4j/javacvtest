package com.phantom5702.javacvtest.example2;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.videoinput.videoInput;

/**
 * 获取摄像头以及摄像头信息
 */
public class VideoNumber {
    public static void main(String[] args) {
        int webcams = videoInput.listDevices();
        String webcam_name[] = new String[webcams];

        int max = 0;
        for (int i = 0; i < webcams; i++) {
            BytePointer dev = videoInput.getDeviceName(i);
            webcam_name[i] = dev.getString();
        }
    }
}
