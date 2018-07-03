package com.mvcoder.testvideo;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, MediaPlayer.OnPreparedListener {

    private FrameLayout container;

    private TextureView textureView;

    private SurfaceTexture texture;

    private Surface surface;

    private MediaPlayer player;

    private Button playBt;

    private ExecutorService service = Executors.newSingleThreadExecutor();

    private int fileNum = 0;

    private File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

    private File desDir = new File(root.getAbsolutePath() + "/VideoImages");

    private static final int CACHE_ING = 1;
    private static final int CACHE_CANCEL = 2;

    private final String networkFile = "http://192.168.3.83:8080/test.mp4";

    private final String networkFile2 = "http://192.168.3.83:8080/test2.mp4";

    private final String networkFile3 = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";

    private final String networkFile4 = "http://221.228.226.23/11/t/j/v/b/tjvbwspwhqdmgouolposcsfafpedmb/sh.yinyuetai.com/691201536EE4912BF7E4F1E2C67B8119.mp4";

    private final String networkFile5 = "http://yun.it7090.com/video/XHLaunchAd/video01.mp4";

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int code = msg.what;
            switch (code) {
                case CACHE_ING:
                    StringBuilder builder = new StringBuilder(cacheTv.getText().toString());
                    builder.append("\n");
                    builder.append("caching");
                    cacheTv.setText(builder.toString());
                    break;
                case CACHE_CANCEL:
                    StringBuilder builder1 = new StringBuilder(cacheTv.getText().toString());
                    builder1.append("\n");
                    builder1.append("cache cancel");
                    cacheTv.setText(builder1.toString());
                    break;
            }
        }
    };

    interface T{
        void sout();
    }

    public T[] getTArray(){
        return new T[0];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }



    private TextView cacheTv;

    private void initView() {
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
        cacheTv = findViewById(R.id.tv_cache);
        System.out.println(desDir.getAbsolutePath());
        container = findViewById(R.id.container);
        textureView = new TextureView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        container.addView(textureView);
        playBt = findViewById(R.id.playBt);
        playBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textureView.isAvailable()) {
                    // playVideo();
                    startDecoderVideo();
                }
            }
        });
        // textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (service != null && !service.isShutdown()) {
            service.shutdown();
        }
    }

    private void playVideo() {
        player = new MediaPlayer();
        try {
            File videoDir = getDir("videos", MODE_PRIVATE);
            File videoFile = new File(videoDir, "test.mp4");
            if (!videoFile.exists()) return;
            player.setDataSource(videoFile.getAbsolutePath());
            player.setSurface(surface);
            player.setOnPreparedListener(this);
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void startDecoderVideo() {
        File videoDir = getDir("videos", MODE_PRIVATE);
        File videoFile = new File(videoDir, "test.mp4");
        final String path = videoFile.getAbsolutePath();
        if (!videoFile.exists()) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                decodeVideoFile(path);
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.stop();
            player.release();
        }
    }

    private final String TAG = MainActivity.class.getSimpleName();

    private int frameNum = 0;

    public void decodeVideoFile(String filePath) {
        try {
            final MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(networkFile);
            MediaFormat format = null;
            int videoTrackIndex = -1;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                format = extractor.getTrackFormat(i);
                String mineType = format.getString(MediaFormat.KEY_MIME);
                if (mineType.startsWith("video/")) {
                    System.out.println(format.getString(MediaFormat.KEY_MIME));
                    videoTrackIndex = i;
                    break;
                }
            }
            if (videoTrackIndex == -1) return;
            extractor.selectTrack(videoTrackIndex);
            if (format == null) return;
            /*MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
            String decodeName = codecList.findDecoderForFormat(format);
            final MediaCodec codec = MediaCodec.createByCodecName(decodeName);*/
            final MediaCodec codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
            if (codec == null) return;
            codec.configure(format, surface, null, 0);

            codec.start();
            long seekTime = 0;
            boolean hasSeek = seekTime > 0;
            if(hasSeek) extractor.seekTo(seekTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

            ByteBuffer[] inputBuffers = codec.getInputBuffers();
            ByteBuffer[] outputBuffers = codec.getOutputBuffers();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean isEOS = false;
            long startMs = System.currentTimeMillis();

            int frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
            System.out.println("frameRate : -------- " + frameRate);

            boolean hasStartPoint = false;

            //上帧的数据
            long lastTimeStamp = -1;
            MediaCodec.BufferInfo lastInfo = new MediaCodec.BufferInfo();
            lastInfo.presentationTimeUs = seekTime;

            int cacheBufferNum = 0;
            boolean canDequeOutputBuffer = true;
            int delayNum = 0;

            while (!Thread.interrupted()) {
                if (!isEOS) {
                    int inIndex = codec.dequeueInputBuffer(10000);
                    if (inIndex >= 0) {
                        ByteBuffer buffer = inputBuffers[inIndex];
                        int sampleSize = extractor.readSampleData(buffer, 0);
                        cacheBufferNum++;
                        if (sampleSize < 0) {
                            // We shouldn't stop the playback at this point, just pass the EOS
                            // flag to mediaCodec, we will get it again from the dequeueOutputBuffer
                            Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                            codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEOS = true;
                        } else {
                            codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), extractor.getSampleFlags());
                            extractor.advance();
                        }
                    }
                }
                //不能deque，需要缓冲
                if(!canDequeOutputBuffer) {
                    if(!isEOS && cacheBufferNum < frameRate){
                        continue;
                    }
                    canDequeOutputBuffer = true;
                    delayNum = 0;
                    mHandler.sendEmptyMessage(CACHE_CANCEL);
                }

                int outIndex = codec.dequeueOutputBuffer(info, 10000);
                cacheBufferNum--;
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                        outputBuffers = codec.getOutputBuffers();
                        break;
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.d(TAG, "New format " + codec.getOutputFormat());
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.d(TAG, "dequeueOutputBuffer timed out!");
                        break;
                    default:
                        long rate = info.presentationTimeUs - lastInfo.presentationTimeUs;
                        long currentTime = System.currentTimeMillis();
                        if(hasSeek){
                            if(rate < 0) {
                                codec.releaseOutputBuffer(outIndex, false);
                                continue;
                            }
                        }
                        long m = currentTime - lastTimeStamp;
                        long n = rate / 1000;
                        if(m < n){
                            delayNum = 0;
                            try {
                                Thread.sleep(n - m);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else if(m > rate / 1000){
                            delayNum++;
                            Log.d(TAG, "frame delay...." + delayNum);
                            if(delayNum >= 3){
                                canDequeOutputBuffer = false;
                                mHandler.sendEmptyMessage(CACHE_ING);
                            }
                        }
                        frameNum++;
                        codec.releaseOutputBuffer(outIndex, true);

                        //更新lastTimeStamp
                        lastTimeStamp = System.currentTimeMillis();
                        //设置新的lastInfo
                        lastInfo.set(info.offset,info.size,info.presentationTimeUs,info.flags);


/*
                        long presentationTimeUs = info.presentationTimeUs;
                        if (hasSeek) {
                            presentationTimeUs -= seekTime;
                            if (presentationTimeUs < 0) {
                                codec.releaseOutputBuffer(outIndex, false);
                                break;
                            }
                        }
                        if (!hasStartPoint) {
                            hasStartPoint = true;
                            startMs = System.currentTimeMillis();
                        }
                        //ByteBuffer buffer = outputBuffers[outIndex];
                        //Log.v(TAG,"We can't use this buffer but render it due to the API limit, " + buffer);

                        // We use a very simple clock to keep the video FPS, or the video
                        // playback will be too fast
                        while (presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                        frameNum++;
                        codec.releaseOutputBuffer(outIndex, true);*/
                }

                // All decoded frames have been rendered, we can stop playing now
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    Log.d(TAG, "---- frame ---- :" + frameNum);
                    break;
                }
            }
            codec.stop();
            codec.release();
            extractor.release();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("The video source unAvailable!");
        }
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        texture = surfaceTexture;
        surface = new Surface(surfaceTexture);
        System.out.println("onSurfaceTextureAvailable");
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        System.out.println("onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    Set<Integer> set = new HashSet<>();

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

       // Bitmap bitmap = textureView.getBitmap();
       // Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        System.out.println("fileNum : " + (fileNum++) + " , timestamp:" + surfaceTexture.getTimestamp() / 1000 / 1000);
       // service.execute(new PictureRunnable(copyBitmap, fileNum));
        // copyBitmap.recycle();
      //  bitmap.recycle();

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (player != null) {
            player.start();
        }
    }

    class PictureRunnable implements Runnable {

        private Bitmap bitmap;
        private int fileNum;

        public PictureRunnable(Bitmap bitmap, int fileNum) {
            this.bitmap = bitmap;
            this.fileNum = fileNum;
        }

        @Override
        public void run() {
            File file = new File(desDir, fileNum + ".png");
            try {
                boolean flag = bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                if (!flag) {
                    System.out.println("保存图片失败");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        }
    }

}
