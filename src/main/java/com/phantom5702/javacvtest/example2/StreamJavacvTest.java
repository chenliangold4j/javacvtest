package com.phantom5702.javacvtest.example2;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.swing.*;

/**
 * 1.FrameRecorder的结构和分析
 * <p>
 * FrameRecorder与FrameGrabber类似，也是个抽象类，抽象了所有录制器的通用方法和一些共用属性。
 * <p>
 * FrameRecorder只有两个子类实现：FFmpegFrameRecorder和OpenCVFrameRecorder
 * <p>
 * 两个FrameRecorder实现类的介绍
 * FFmpegFrameRecorder：使用ffmpeg作为音视频编码/图像、封装、推流、录制库。除了支持音视频录制推流之外，还可以支持gif/apng动态图录制，
 * 对于音视频这块的功能ffmpeg依然还是十分强大的保障。
 * <p>
 * OpenCVFrameRecorder：使用opencv的videowriter录制视频，不支持像素格式转换，根据opencv的官方介绍，
 * 如果在ffmpeg可用的情况下，opencv的视频功能可能也是基于ffmpeg的，macos下基于avfunctation。
 * <p>
 * 总得来说，音视频这块还是首选ffmpeg的录制器。但是凡事都有例外，由于javacv的包实在太大，开发的时候下载依赖都要半天。
 * 为了减少程序的体积大小，如果只需要进行图像处理/图像那个识别的话只使用opencv的情况就能解决问题，
 * 那就opencvFrameGrabber和OpenCVFrameRecorder配套使用吧，毕竟可以省很多空间。
 * <p>
 * 同样的，如果只是做音视频流媒体，那么能使用ffmpeg解决就不要用其他库了，能节省一点空间是一点，确实javacv整个完整包太大了，快要1G了。。。这些都是题外话了，还是回归正题吧。
 * <p>
 * 2.FrameRecorder的结构和流程
 * FrameRecorder的整个代码结构和运作流程很简单
 * <p>
 * FFmpegFrameRecorder结构和分析
 * FFmpegFrameRecorder是比较复杂的，我们主要它来实现像素格式转换、视频编码和录制推流。
 * <p>
 * 我们把流程分为转封装流程和转码流程
 * <p>
 * 转封装流程：
 * <p>
 * FFmpegFrameRecorder初始化-->start()-->循环recordPacket(AVPacket)-->close()
 * <p>
 * 这里的AVPacket是未解码的解复用视频帧。
 * <p>
 * 转码编码流程：
 * <p>
 * FFmpegFrameRecorder初始化-->start()-->循环record(Frame)/recordImage()/recordSamples()-->close()
 * <p>
 * FFmpegFrameRecorder初始化及参数说明
 * FFmpegFrameRecorder初始化支持文件地址、流媒体推流地址、OutputStream流的形式和imageWidth(视频宽度)、imageHeight（图像高度）、audioChannels（音频通道，1-单声道，2-立体声）
 * <p>
 * FFmpegFrameRecorder参数较多，不一一列举，直接看代码中的参数说明：
 * FFmpegFrameRecorder recorder=new FFmpegFrameRecorder(output, width,height,0);
 * recorder.setVideoCodecName(videoCodecName);//优先级高于videoCodec
 * recorder.setVideoCodec(videoCodec);//只有在videoCodecName没有设置或者没有找到videoCodecName的情况下才会使用videoCodec
 * //        recorder.setFormat(format);//只支持flv，mp4，3gp和avi四种格式，flv:AV_CODEC_ID_FLV1;mp4:AV_CODEC_ID_MPEG4;3gp:AV_CODEC_ID_H263;avi:AV_CODEC_ID_HUFFYUV;
 * recorder.setPixelFormat(pixelFormat);// 只有pixelFormat，width，height三个参数任意一个不为空才会进行像素格式转换
 * recorder.setImageScalingFlags(imageScalingFlags);//缩放，像素格式转换时使用，但是并不是像素格式转换的判断参数之一
 * recorder.setGopSize(gopSize);//gop间隔
 * recorder.setFrameRate(frameRate);//帧率
 * recorder.setVideoBitrate(videoBitrate);
 * recorder.setVideoQuality(videoQuality);
 * //下面这三个参数任意一个会触发音频编码
 * recorder.setSampleFormat(sampleFormat);//音频采样格式,使用avutil中的像素格式常量，例如：avutil.AV_SAMPLE_FMT_NONE
 * recorder.setAudioChannels(audioChannels);
 * recorder.setSampleRate(sampleRate);
 * recorder.start();
 *
 * FFmpegFrameRecorder的start
 * start中其实做了很多事情：一堆初始化操作、打开网络流、查找编码器、把format字符转换成对应的videoCodec和videoFormat等等。
 *
 *  FFmpegFrameRecorder中的stop和close
 * 非常需要的注意的是，当我们在录制文件的时候，一定要保证stop()方法被调用，因为stop里面包含了写出文件头的操作，如果没有调用stop就会导致录制的文件损坏，无法被识别或无法播放。
 *
 * close()方法中包含stop()和release()方法
 *
 * OpenCVFrameRecorder结构分析
 * OpenCVFrameRecorder的代码很简单，不到120行的代码，主要是基于opencv的videoWriter封装，流程与FrameRecorder的相同：
 *
 * 初始化和设置参数--->start()--->循环record(Frame frame)--->close()
 *
 * 在start中会初始化opencv的VideoWriter，VideoWriter是opencv中用来保存视频的模块，是比较简单的
 *
 * OpenCVFrameRecorder的初始化和参数设置
 * OpenCVFrameRecorder初始化参数只有三个：filename（文件名称或者文件保存地址），imageWidth（图像宽度）, imageHeight（图像高度）。但是OpenCVFrameRecorder的有作用的参数只有六个，其中pixelFormat和videoCodec这两个比较特殊。
 *
 * pixelFormat：该参数并不像ffmpeg那样表示像素格式，这里只表示原生和灰度图，其中pixelFormat=1表示原生，pixelFormat=0表示灰度图。
 *
 * videoCodec：这个参数，在opencv中对应的是fourCCCodec，所以编码这块的设置也要按照opencv的独特方式来设置，有关opencv的视频编码fourCC的编码集请参考：http://mp4ra.org/#/codecs和https://www.fourcc.org/codecs.php这两个列表，由于列表数据较多，这里就不展示了。
 *
 * filename（文件名称或者文件保存地址），imageWidth（图像宽度）, imageHeight（图像高度）和frameRate（帧率）这四个参数就不过多赘述了。
 *
 *
 *
 *
 */
public class StreamJavacvTest {
    public static void main(String[] args) throws Exception {
        recordCamera("rtmp://127.0.0.1:1935/live/123", 25);
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
