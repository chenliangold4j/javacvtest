package com.phantom5702.javacvtest.example2;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.swing.*;

/**
 * 1.FrameRecorder的结构和分析
 *
 * FrameRecorder与FrameGrabber类似，也是个抽象类，抽象了所有录制器的通用方法和一些共用属性。
 *
 * FrameRecorder只有两个子类实现：FFmpegFrameRecorder和OpenCVFrameRecorder
 *
 * 两个FrameRecorder实现类的介绍
 * FFmpegFrameRecorder：使用ffmpeg作为音视频编码/图像、封装、推流、录制库。除了支持音视频录制推流之外，还可以支持gif/apng动态图录制，
 * 对于音视频这块的功能ffmpeg依然还是十分强大的保障。
 *
 * OpenCVFrameRecorder：使用opencv的videowriter录制视频，不支持像素格式转换，根据opencv的官方介绍，
 * 如果在ffmpeg可用的情况下，opencv的视频功能可能也是基于ffmpeg的，macos下基于avfunctation。
 *
 * 总得来说，音视频这块还是首选ffmpeg的录制器。但是凡事都有例外，由于javacv的包实在太大，开发的时候下载依赖都要半天。
 * 为了减少程序的体积大小，如果只需要进行图像处理/图像那个识别的话只使用opencv的情况就能解决问题，
 * 那就opencvFrameGrabber和OpenCVFrameRecorder配套使用吧，毕竟可以省很多空间。
 *
 * 同样的，如果只是做音视频流媒体，那么能使用ffmpeg解决就不要用其他库了，能节省一点空间是一点，确实javacv整个完整包太大了，快要1G了。。。这些都是题外话了，还是回归正题吧。
 *
 * 2.FrameRecorder的结构和流程
 * FrameRecorder的整个代码结构和运作流程很简单
 *
 * 初始化和设置参数--->start()--->循环record(Frame frame)--->close()
 *
 */
public class StreamJavacvTest {
    public static void main(String[] args) throws Exception {
        recordCamera("rtmp://127.0.0.1:1935/live/123", 30);
    }


    /**
     * 按帧录制本机摄像头视频（边预览边录制，停止预览即停止录制）
     *
     * @param outputFile -录制的文件路径，也可以是rtsp或者rtmp等流媒体服务器发布地址
     * @param frameRate  - 视频帧率
     * @throws Exception
     * @throws InterruptedException
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     * @author eguid
     */
    public static void recordCamera(String outputFile, double frameRate) throws Exception {
        //另一种方式获取摄像头，opencv抓取器方式获取摄像头请参考第一章，FrameGrabber会自己去找可以打开的摄像头的抓取器。

        FrameGrabber grabber = FrameGrabber.createDefault(0);//本机摄像头默认0
        grabber.start();//开启抓取器

        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();//转换器
        IplImage grabbedImage = converter.convert(grabber.grab());//抓取一帧视频并将其转换为图像，至于用这个图像用来做什么？加水印，人脸识别等等自行添加
        int width = grabbedImage.width();
        int height = grabbedImage.height();

        FrameRecorder recorder = FrameRecorder.createDefault(outputFile, width, height);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码
        recorder.setFormat("flv");//封装格式，如果是推送到rtmp就必须是flv封装格式
        recorder.setFrameRate(frameRate);

        recorder.start();//开启录制器
        long startTime = 0;
        long videoTS = 0;
        CanvasFrame frame = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        Frame rotatedFrame = converter.convert(grabbedImage);//不知道为什么这里不做转换就不能推到rtmp
        while (frame.isVisible() && (grabbedImage = converter.convert(grabber.grab())) != null) {

            rotatedFrame = converter.convert(grabbedImage);
            frame.showImage(rotatedFrame);
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            videoTS = 1000 * (System.currentTimeMillis() - startTime);
            recorder.setTimestamp(videoTS);
            recorder.record(rotatedFrame);
            Thread.sleep(40);
        }
        frame.dispose();//关闭窗口
        recorder.close();//关闭推流录制器，close包含release和stop操作
        grabber.close();//关闭抓取器
    }

}
