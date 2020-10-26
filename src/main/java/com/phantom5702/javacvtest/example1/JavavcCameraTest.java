package com.phantom5702.javacvtest.example1;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.swing.*;

/**
 *
 * https://blog.csdn.net/eguid_1/article/details/106785754
 *
 *
 * 1.FrameGrabber(帧抓取器/采集器)介绍
 * 用于采集/抓取视频图像和音频采样。封装了检索流信息，
 * 自动猜测视频解码格式，音视频解码等具体API，并把解码完的像素数据（可配置像素格式）或音频数据保存到Frame中返回等功能。
 *
 * FrameGrabber的结构
 * FrameGrabber本身是个抽象类，抽象了所有抓取器的通用方法和一些共用属性。
 *
 *
 * 2.FrameGrabber的子类/实现类包含以下几个：
 *
 * FFmpegFrameGrabber、OpenCVFrameGrabber、IPCameraFrameGrabber、VideoInputFrameGrabber、
 * FlyCapture2FrameGrabber、DC1394FrameGrabber、RealSenseFrameGrabber、
 * OpenKinectFrameGrabber、OpenKinect2FrameGrabber、PS3EyeFrameGrabber
 *
 * 3.几种FrameGrabber子类实现介绍
 * FFmpegFrameGrabber：强大到离谱的音视频处理/计算机视觉/图像处理库，视觉和图像处理这块没有opencv强大；
 *
 * 可以支持网络摄像机：udp、rtp、rtsp和rtmp等，支持本机设备，比如屏幕画面、摄像头、音频话筒设备等等、还支持视频文件、网络流（flv、rtmp、http文件流、hls、等等等）
 *
 * OpenCVFrameGrabber：Intel开源的opencv计算机视觉/图像处理库，也支持读取摄像头、流媒体等，但是一般常用于读取图片，处理图像，流媒体音视频这块功能没有ffmpeg强大；
 *
 * 也支持rtsp视频流（不支持音频），本地摄像机设备画面采集、视频文件、网络文件、图片等等。
 *
 * IPCameraFrameGrabber：基于openCV调用“网络摄像机”。可以直接用openCV调用，所以一般也不需要用这个抓取器，
 * 例如这个网络摄像机地址：http://IP:port/videostream.cgi?user=admin&pwd=12345&.mjpg；
 *
 * VideoInputFrameGrabber：只支持windows，用来读取通过USB连接的摄像头/相机。videoinput库已经被内置到opencv中了，
 * 另外videoinput调用摄像机的原理是dshow方式，FFmpegFrameGrabber和OpenCVFrameGrabber都支持dshow方式读取摄像机/相机，所以这个库一般用不到；
 *
 * FlyCapture2FrameGrabber：通过USB3.1,USB2.0、GigE和FireWire（1394）四种方式连接的相机，比videoinput要慢一些；
 *
 * DC1394FrameGrabber：另一个支持MAC的FireWire（1394）外接摄像头/相机，这种接口现在已经不常见了；
 *
 * RealSenseFrameGrabber：支持Intel的3D实感摄像头，基于openCV；
 *
 * OpenKinectFrameGrabber：支持Xbox Kinect v1体感摄像头，通过这个采集器可支持mac\linux\windows上使用Kinect v1；
 *
 * OpenKinect2FrameGrabber：用来支持Xbox Kinect v2（次世代版）体感摄像头；
 *
 * PS3EyeFrameGrabber：没错就是那个sony的PS3，用来支持PS3的体感摄像头。
 *
 * 4.FrameGrabber及其实现类的一般使用流程
 * new初始化及初始化设置-->start（用于启动一些初始化和解析操作）-->循环调用grab()获取音视频-->stop（销毁所有对象，回收内存）
 *
 * FrameGrabber中有几种grab：
 *
 * （1）Frame grab();//通用获取解码后的视频图像像素和音频采样，一般的FrameGrabber子类实现只有这个，特殊的子类实现会多几种获取方法。
 *
 * （2）Frame grabFrame();//FFmpegFrameGrabber特有，等同于上面的grab()
 *
 * （3） Frame grabImage();//FFmpegFrameGrabber特有，只获取解码后的视频图像像素
 *
 * （4）Frame grabSamples();//FFmpegFrameGrabber特有，只获取音频采样
 *
 * （5）Frame grabKeyFrame();//FFmpegFrameGrabber特有，只获取关键帧
 *
 * （6）Avpacket grabPacket();//FFmpegFrameGrabber特有，获取解复用的音/视频帧，也就是grabPacket()可以获取没有经过解码的音/视频帧。
 *
 * （7）void grab()后还需要再调用getVideoImage()、getIRImage()和getDepthImage();//这是OpenKinectFrameGrabber特有的来获取体感摄像头图像的方法
 *
 * （8）grabDepth()、grabIR()和grabVideo()//这是OpenKinect2FrameGrabber和RealSenseFrameGrabber特有的调用体感摄像头的方法
 *
 * （9）grabBufferedImage()//是IPCameraFrameGrabber用于调用网络摄像机图像的方法
 *
 * （10）grab_RGB4()和grab_raw()//是PS3EyeFrameGrabber用于调用体感摄像头图像的方
 *
 *
 *
 */
public class JavavcCameraTest {
    public static void main(String[] args) throws Exception, InterruptedException {

        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);//新建opencv抓取器，一般的电脑和移动端设备中摄像头默认序号是0，不排除其他情况
        grabber.start();//开始获取摄像头数据

        CanvasFrame canvas = new CanvasFrame("摄像头预览");//新建一个预览窗口
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //窗口是否关闭
        while (canvas.isDisplayable()) {
            /*获取摄像头图像并在窗口中显示,这里Frame frame=grabber.grab()得到是解码后的视频图像*/
            canvas.showImage(grabber.grab());
        }
        grabber.close();//停止抓取
    }

}
