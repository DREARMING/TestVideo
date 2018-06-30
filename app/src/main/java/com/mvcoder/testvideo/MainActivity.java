package com.mvcoder.testvideo;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

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

    private File root = Environment.getExternalStorageDirectory();

    private File desDir = new File(root.getAbsolutePath() + "/VideoImages");

    private static final int UPDATE_TEXT_IMAGE = 1;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int code = msg.what;
            switch (code){
                case UPDATE_TEXT_IMAGE:
                    texture.updateTexImage();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        if (!desDir.exists()) {
            desDir.mkdirs();
        }
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
            File videoDir = getDir("videos", MODE_WORLD_WRITEABLE);
            File videoFile = new File(videoDir, "/test.mp4");
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
        File videoFile = new File(videoDir, "/test.mp4");
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

    private boolean isEof = false;

    private int frameNum = 0;

    public void decodeVideoFile(String filePath) {
        try {
            final MediaExtractor extractor = new MediaExtractor();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                extractor.setDataSource(filePath);
            }
            MediaFormat format = null;
            int videoTrackIndex = -1;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                format = extractor.getTrackFormat(i);
                String mineType = format.getString(MediaFormat.KEY_MIME);
                if (mineType.startsWith("video/")) {
                    videoTrackIndex = i;
                }
                System.out.println(format.getString(MediaFormat.KEY_MIME));
            }
            if (videoTrackIndex == -1) return;
            extractor.selectTrack(videoTrackIndex);
            if (format == null) return;
            MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
            String decodeName = codecList.findDecoderForFormat(format);
            final MediaCodec codec = MediaCodec.createByCodecName(decodeName);
            codec.configure(format,surface,null,0);
            codec.setOutputSurface(surface);
            codec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int i) {
                    if(isEof) return;
                    ByteBuffer byteBuffer = mediaCodec.getInputBuffer(i);
                    int readNum = extractor.readSampleData(byteBuffer, 0);
                    if (readNum > 0) {
                        long presentionTime = extractor.getSampleTime();
                        int flags = extractor.getSampleFlags();
                        System.out.println("flag ：" + flags);
                        mediaCodec.queueInputBuffer(i, 0, readNum, presentionTime, flags);
                        extractor.advance();
                    } else if (readNum < 0) {
                        isEof = true;
                        extractor.release();
                        //流结束
                        mediaCodec.queueInputBuffer(i, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int i, @NonNull MediaCodec.BufferInfo bufferInfo) {
                    boolean render = false;
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        mediaCodec.releaseOutputBuffer(i, false);
                        System.out.println("--------frame num---------- : " + frameNum);
                        codec.stop();
                        codec.release();
                        return;
                    } else if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        mediaCodec.getOutputFormat(i);
                    }else if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0 || (bufferInfo.flags & MediaCodec.BUFFER_FLAG_PARTIAL_FRAME) != 0) {
                       // mHandler.sendEmptyMessage(UPDATE_TEXT_IMAGE);
                        render = true;
                    } else {
                        System.out.println("unknow buffer info");
                        render = true;
                    }
                    mediaCodec.releaseOutputBuffer(i,render);
                    if(render){
                        frameNum++;
                    }
                }

                @Override
                public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {
                    System.out.println("some error occur");
                    e.printStackTrace();
                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {

                }
            });
            codec.start();
        } catch (IOException e) {
            e.printStackTrace();
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

        //Bitmap bitmap = textureView.getBitmap();
       // Bitmap copyBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
       /* try {
            File file = new File(desDir,(fileNum++)+".png");
            bitmap.compress(Bitmap.CompressFormat.PNG,100,new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //service.execute(new PictureRunnable(copyBitmap));
        System.out.println("fileNum : " + (fileNum++) + " , timestamp:" + surfaceTexture.getTimestamp() / 1000 / 1000);
       // copyBitmap.recycle();
       // bitmap.recycle();

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (player != null) {
            player.start();
        }
    }

    class PictureRunnable implements Runnable {

        private Bitmap bitmap;

        public PictureRunnable(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            File file = new File(desDir, (fileNum++) + ".png");
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
