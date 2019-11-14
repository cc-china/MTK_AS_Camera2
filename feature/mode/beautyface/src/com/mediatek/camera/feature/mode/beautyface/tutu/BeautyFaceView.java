package com.mediatek.camera.feature.mode.beautyface.tutu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.feature.mode.beautyface.pluginmanager.MediatekSceneModeData;
import com.mediatek.camera.common.utils.Size;
import com.mediatek.camera.feature.mode.beautyface.FacebeautyAdjustManager;
import com.mediatek.camera.feature.mode.beautyface.gles.FunnyFaceView;

import org.lasque.tusdk.api.TuSDKFilterEngine;
import org.lasque.tusdk.api.TuSDKMediaRecoder;
import org.lasque.tusdk.core.encoder.video.TuSDKVideoEncoderSetting;
import org.lasque.tusdk.core.exif.ExifInterface;
import org.lasque.tusdk.core.face.FaceAligment;
import org.lasque.tusdk.core.seles.SelesEGLContextFactory;
import org.lasque.tusdk.core.seles.sources.SelesOutInput;
import org.lasque.tusdk.core.struct.TuSdkSize;
import org.lasque.tusdk.core.utils.TLog;
import org.lasque.tusdk.core.utils.TuSdkDate;
import org.lasque.tusdk.core.utils.hardware.CameraConfigs;
import org.lasque.tusdk.core.utils.hardware.InterfaceOrientation;
import org.lasque.tusdk.core.utils.image.BitmapHelper;
import org.lasque.tusdk.core.utils.sqllite.ImageSqlHelper;
import org.lasque.tusdk.core.utils.sqllite.ImageSqlInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.widget.Toast;
import android.util.Log;
/**
 * Created by azmohan on 17-9-11.
 */

public class BeautyFaceView extends GLSurfaceView implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener, IAppUiListener.OnPreviewAreaChangedListener {
    private int mOESTextureId;
    private SurfaceTexture mSurfaceTexture;
    private Texture2dProgram textureProgram;
    private HandlerThread mHandlerThread;
    private TuSDKFilterEngine mFilterEngine;
    private FilerChangeListener mFilerChangedListener;
    private CameraActivity mCameraActivity;
    private long mCaptureTime;
    public ExifInterface mMetadata;
    private TuSdkDate mCaptureStartTime;
    private Rect mDrawBounds;
    private final Object waitDoneObject = new Object();
    private final static int WAIT_DONE = 1;
    private final static int NOTIFY_SURFACE_UPDATE = 2;
    private byte[] mTutuRetPictureData;
    private int mResolutionWidth;
    private int mResolutionHeight;
    private SelesOutInput mFilter;
    private float SAMPLER_RATIO = 1.5f;
    private android.opengl.EGLContext mEGLContext;
    private FunnyFaceView mFunnyFaceView;
    private boolean mIsTextureForPreview;
    private DataBeautyHandleCallback mDataBeautyCallback;
    private InterfaceOrientation mCaptureOrientation;
    private volatile int mCaptureOrientationIndex;
    private int mRotation;

    private String mResolution = null;
    private static final double ASPECT_TOLERANCE = 0.03;
    private double mAspectRatio = 0.0;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private boolean mPauseed = false;
    private static final Tag TAG = new Tag(BeautyFaceView.class.getSimpleName());
    private WorkerHandler mWorkerHandler;
    private static final int MSG_SAVE_THUMBNAIL = 0x01;
    private static final int MSG_LOAD_DECODE_THUMBNAIL = 0x02;
    private static final int MSG_NOT_READY_IMAGE_THUMBNAIL = 0x03;

    private static final InterfaceOrientation TUTU_CAPTURE_ORIENTATION[] = {
            InterfaceOrientation.Portrait,
            InterfaceOrientation.LandscapeLeft,
            InterfaceOrientation.PortraitUpsideDown,
            InterfaceOrientation.LandscapeRight,
    };

    private static final int TUTU_ADUST_ORIENTATION[] = {
            0,
            90,
            180,
            270,
    };

    private int getIndextByOrientation(int orientation) {
        int index = 0;
        orientation = orientation % 360;
        for (int i = 0; i < TUTU_ADUST_ORIENTATION.length; i++) {
            if (orientation == TUTU_ADUST_ORIENTATION[i]) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void setCaptureRotation(int rotation) {
        mCaptureOrientation = mFilterEngine.getDeviceOrient();
        mRotation = rotation;
    }

    public InterfaceOrientation getCaptureOrientation() {
        InterfaceOrientation interfaceOrientation = TUTU_CAPTURE_ORIENTATION[0];
        if (mCaptureOrientationIndex > 0 && mCaptureOrientationIndex < TUTU_CAPTURE_ORIENTATION.length) {
            interfaceOrientation = TUTU_CAPTURE_ORIENTATION[mCaptureOrientationIndex];
        }
        return interfaceOrientation;
    }

    @Override
    public void onPreviewAreaChanged(RectF newPreviewArea, Size previewSize) {
        mDrawBounds = new Rect((int) newPreviewArea.left, (int) newPreviewArea.top,
                (int) newPreviewArea.right, (int) newPreviewArea.bottom);
    }

    public static interface DataBeautyHandleCallback {
        public void onDataBeautyCallback(byte[] jpeg);

        public void onThumbCallback(byte[] jpeg);

        public void onNeedBeautyCallback(byte[] jpeg);
    }

    private Handler mHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WAIT_DONE:
                    synchronized (waitDoneObject) {
                        waitDoneObject.notifyAll();
                    }
                    break;
                case NOTIFY_SURFACE_UPDATE:
                    if (mListener != null) {
                        mListener.onBfSurfaceTextureUpdate(mSurfaceTexture);
                    }
                    break;
            }
        }
    };


    public SelesOutInput getFilter() {
        return mFilter;
    }

    public interface FilerChangeListener {
        public void onFilerChanged(SelesOutInput filter);
    }

    public void setFilterChangeListener(FilerChangeListener listener) {
        mFilerChangedListener = listener;
    }

    private class TuSDKEGLContextFactory extends SelesEGLContextFactory {
        public TuSDKEGLContextFactory() {
            super(2);
        }

        @Override
        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            LogHelper.i(TAG, "egl factory destroy: %s" + Thread.currentThread().getName());

            if (mFilterEngine != null) {
                mFilterEngine.onSurfaceDestroy();
            }
            mFunnyFaceView.disposeInGlThread();
            super.destroyContext(egl, display, context);
        }
    }

    public BeautyFaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCameraActivity = (CameraActivity) context;
        // mWorkerHandler
        HandlerThread t = new HandlerThread("thumbnail-creation-thread");
        t.start();
        mWorkerHandler = new WorkerHandler(t.getLooper());

        setEGLContextClientVersion(2);
        setEGLContextFactory(new TuSDKEGLContextFactory());
        setRenderer(this);//设置渲染函数
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        constructFilterEngine();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if (width > height) {
            mResolution = width + "x" + height;
        } else {
            mResolution = height + "x" + width;
        }
        String[] array = mResolution.split("x");
        mResolutionWidth = Integer.parseInt(array[1]);
        mResolutionHeight = Integer.parseInt(array[0]);
    }

    public void setCameraId(int cameraId) {
        if (cameraId == 0) {
            mFilterEngine.setCameraFacing(CameraConfigs.CameraFacing.Back);
        } else {
            mFilterEngine.setCameraFacing(CameraConfigs.CameraFacing.Front);
        }
    }

    public void setCaptureMirrorEnabled(boolean isEnabled) {
        if (mFilterEngine != null) {
            mFilterEngine.setIsOutputCaptureMirrorEnabled(isEnabled);
        }
    }

    public void asyncHandlePictureData(byte[] datas, DataBeautyHandleCallback callback, boolean isRevertMirror, int orientation) {
        mDataBeautyCallback = callback;
        if (mDataBeautyCallback != null) {
            FaceAligment[] faceAligments = mFilterEngine.getFaceFeatures();//why is null
            LogHelper.i(TAG, "asyncHandlePictureData() - faceAligments result : " + (faceAligments != null && faceAligments.length > 0));
            if (!(faceAligments != null && faceAligments.length > 0)) {
                mDataBeautyCallback.onNeedBeautyCallback(datas);
                return;
            }
        }
        mCaptureStartTime = TuSdkDate.create();
        snapPreview(isRevertMirror, orientation);
        mFilterEngine.asyncProcessPictureData(datas, mCaptureOrientation, mRotation);
        LogHelper.i(TAG, "end of asyncHandlePictureData()");
    }

    public byte[] syncHandlePictureData(byte[] data) {
        synchronized (waitDoneObject) {
            FaceAligment[] faceAligments = mFilterEngine.getFaceFeatures();
            LogHelper.i(TAG, "syncHandlePictureData() - faceAligments result : " + (faceAligments != null && faceAligments.length > 0));
            if (!(faceAligments != null && faceAligments.length > 0)) {
                return data;
            }
            mCaptureStartTime = TuSdkDate.create();
            mFilterEngine.asyncProcessPictureData(data);
            try {
                waitDoneObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
            return mTutuRetPictureData;
        }
    }

    public void takeSnapShot() {
        if (mFilterEngine != null) {
            mFilterEngine.takeShot();
        }
    }

    private void constructFilterEngine() {
        if (mFilterEngine != null) return;
//        TuSDKFacePlasticFilter.MARK_FACE_ENABLE = true;
        // 初始化滤镜引擎
        mFilterEngine = new TuSDKFilterEngine(mCameraActivity, true, true);
        // 大眼瘦脸必须开启该配置
        mFilterEngine.setEnableLiveSticker(true);
//        mFilterEngine.setCameraFacing();
        mFilterEngine.setCameraFacing(CameraConfigs.CameraFacing.Front);
        mFilterEngine.setOutputOriginalImageOrientation(true);
        mFilterEngine.setIsOriginalCaptureOrientation(false);
        mFilterEngine.setIsInputCaptureMirrorEnabled(false);
        mFilterEngine.switchFilter("Nature_2_1"); // Nature_2_1
        mFilterEngine.setDelegate(new TuSDKFilterEngine.TuSDKFilterEngineDelegate() {
            @Override
            public void onPictureDataCompleted(final IntBuffer intBuffer, final TuSdkSize tuSdkSize) {
//                mCameraActivity.getWorkerHandler().post(new Runnable() {
                mWorkerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        LogHelper.i(TAG, "拍摄处理总耗时: %d ms" + mCaptureStartTime.diffOfMillis());
                        TuSdkDate date = TuSdkDate.create();
                        Bitmap bitmap = Bitmap.createBitmap(tuSdkSize.width, tuSdkSize.height, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(intBuffer);
                        long s1 = date.diffOfMillis();
                        LogHelper.i(TAG, "buffer -> bitmap taken: %d ms" + s1);
                        date = TuSdkDate.create();
                        if (mDataBeautyCallback != null) {
                            mDataBeautyCallback.onDataBeautyCallback(BitmapHelper.bitmap2byteArrayTurbo(bitmap, 100));
                        }
                        s1 = date.diffOfMillis();
                        LogHelper.i(TAG, "bitmap -> jpeg taken: %d ms" + s1);
                    }
                });
            }

            @Override
            public void onFilterChanged(SelesOutInput filter) {
                mFilter = filter;
                FacebeautyAdjustManager.setFilterParams(filter);
                LogHelper.i(TAG, "onFilterChanged -> mFilter = " + mFilter.toString());
            }

            @Override
            public void onPreviewScreenShot(Bitmap bitmap) {
                if (bitmap == null) {
                    return;
                }
                byte[] jpeg = BitmapHelper.bitmap2byteArrayTurbo(bitmap, 100);
                if (mSnapShotListener != null && jpeg != null) {
                    mSnapShotListener.onSnapShot(jpeg);
                    LogHelper.i(TAG, "mSnapShotListener.onSnapShot");
                }
            }
        });
    }

    private boolean isPortraitMode() {
        int rotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation();
        boolean ret = false;
        switch (rotation) {
            // 竖屏
            case Surface.ROTATION_0:
                ret = true;
                break;
            case Surface.ROTATION_90:
                ret = false;
                break;
            case Surface.ROTATION_180:
                ret = true;
                break;
            case Surface.ROTATION_270:
                ret = false;
                break;
        }
        return ret;
    }

    public void destroyFilterEngine() {
        LogHelper.i(TAG, "destroyFilterEngine");
        if (mFilterEngine == null) return;

        mFilterEngine.destroy();
        mFilterEngine = null;
    }


    public SurfaceTexture getSurfaceTexture() {
        while (mSurfaceTexture == null) {
            mIsTextureForPreview = true;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogHelper.i(TAG, "mSurfaceTexture = " + mSurfaceTexture.toString());
        return mSurfaceTexture;
    }

    public void initSurfaceTexture() {
        LogHelper.i(TAG, "initSurfaceTexture: thread name : %s" + Thread.currentThread().getName());

        mOESTextureId = GLUtils.createOESTexture();
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);//20181011

        if (textureProgram != null) {
            textureProgram.release();
        }
        // 初始化渲染脚本程序
        textureProgram = new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D);

        GLUtils.checkGlError("initSurfaceTexture");
        LogHelper.i(TAG, "end of initSurfaceTexture");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {  //渲染回调
        LogHelper.i(TAG, "onSurfaceCreated thread name:" + Thread.currentThread().getName());
        ApplicationListener listener = Gdx.app.getApplicationListener();
        if (listener instanceof FunnyFaceView) {
            mFunnyFaceView = (FunnyFaceView) listener;
            mFunnyFaceView.create(this);
        }
        mEGLContext = EGL14.eglGetCurrentContext();
        initSurfaceTexture();//首次需要初始化SurfaceTexture
        if (mFilterEngine != null) mFilterEngine.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {//渲染回调
        if (mFilterEngine != null) mFilterEngine.onSurfaceChanged(width, height);
        mFunnyFaceView.resize(mMeasuredHeight, mMeasuredWidth);//后续glReadPixels读取了与显示预览的surfaceView连接的缓冲区
    }

    private long mTime;
    private int mCount;

    /**
     * showFps
     */
    private void showFps() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - mTime) >= 1000) {
            LogHelper.i(TAG, "frame :" + mCount);
            mTime = currentTime;
            mCount = 0;
        } else {
            ++mCount;
        }
    }


    @Override
    public void onDrawFrame(GL10 gl10) {//渲染回调!!!
        // if not updateTexImage,surface texture don't notify next frame available.
        mSurfaceTexture.updateTexImage();
//        CameraModule cameraModule = mCameraActivity.getCurrentModule();
        if (mPauseed) {
            return;
        }
        if (mFilterEngine == null || mDrawBounds == null || mSurfaceTexture == null) {
            return;
        }

        if (mDrawBounds.width() <= 0 || mDrawBounds.height() <= 0) {
            LogHelper.w(TAG, "invalid draw");
            return;
        }

        if (mMediaRecorder != null && mResumeRecord) {
            LogHelper.i(TAG, "startVideoDataEncoder");
            updateOutputFilter();
            mMediaRecorder.startVideoDataEncoder(mEGLContext, mSurfaceTexture);
        }

        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // 滤镜引擎处理，返回的 textureID 为 TEXTURE_2D 类型
        int textureWidth = mMeasuredHeight;/*mDrawBounds.height();*/
        int textureHeight = mMeasuredWidth;/*mDrawBounds.width();*/
        if (mDrawBounds.width() >= 972) {
            textureHeight = (int) (textureHeight / SAMPLER_RATIO);
            textureWidth = (int) (textureWidth / SAMPLER_RATIO);
        }
        final int textureId = mFilterEngine.processFrame(mOESTextureId, /*textureWidth*/mMeasuredHeight, /*textureHeight*/mMeasuredWidth);
//        showFps();
        try {
            GLUtils.checkGlError("processFrame");
        } catch (RuntimeException e) {
            /*do nothing , just ignore
            GlError will drop this frame
             */
            return;
        }
        final int y;
        if(/*ResolutionUtil.hasNotch()*/false){
            y = mResolutionHeight - mDrawBounds.height() - mDrawBounds.top;// - ResolutionUtil.getNotchHeight();
        } else {
            y = mResolutionHeight - mDrawBounds.height() - mDrawBounds.top;
        }
        //GLES20.glViewport(mDrawBounds.left, y, /*mDrawBounds.height()*/mMeasuredHeight, /*mDrawBounds.width()*/mMeasuredWidth);
        textureProgram.draw(textureId);
//        if (mCameraActivity.getCurrentCameraMode() == MediatekSceneModeData.FREEME_SCENE_MODE_FC_ID) {
//            FaceAligment[] faceAligments = mFilterEngine.getFaceFeatures();
//            float deviceAngle = mFilterEngine.getDeviceAngle();
//            mFunnyFaceView.render(deviceAngle, faceAligments);
//            mFunnyFaceView.capture();
//        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
        if (mIsTextureForPreview) {
            mIsTextureForPreview = false;
            return;
        }
        if (mListener != null) {
            mListener.onBfSurfaceTextureUpdate(mSurfaceTexture);
        }
    }

    private static final boolean IS_SAVE_FOR_TEST = false;
    private Bitmap mPreviewBitmap;
    private Object object = new Object();

    public Bitmap createBitmapFromGLSurface(int x, int y, int w, int h) {
        synchronized (this) {
            int bitmapBuffer[] = new int[w * h];
            int bitmapSource[] = new int[w * h];
            IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
            intBuffer.position(0);
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            try {
                GLES20.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
                int offset1, offset2;
                for (int i = 0; i < h; i++) {
                    offset1 = i * w;
                    offset2 = (h - i - 1) * w;
                    for (int j = 0; j < w; j++) {
                        int texturePixel = bitmapBuffer[offset1 + j];
                        int blue = (texturePixel >> 16) & 0xff;
                        int red = (texturePixel << 16) & 0x00ff0000;
                        int pixel = (texturePixel & 0xff00ff00) | red | blue;
                        bitmapSource[offset2 + j] = pixel;
                    }
                }
            } catch (GLException e) {
                LogHelper.i(TAG, "createBitmapFromGLSurface: " + e.getMessage() + e);
                return null;
            }
            Bitmap bmp = Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
            int orientation = /*mCameraActivity.getCurrOrientation();*/mCameraActivity.getGSensorOrientation();
            if (orientation == 0) {
                return bmp;
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, false);
            if (IS_SAVE_FOR_TEST) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream("/sdcard/screen.png");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bmp;
        }
    }

    public void snapPreview(final boolean isRevertMirror, final int orientation) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                int y = mResolutionHeight - mDrawBounds.height() - mDrawBounds.top;
                Bitmap bitmap = createBitmapFromGLSurface(mDrawBounds.left, y, mDrawBounds.width(),
                        mDrawBounds.height());
                if (isRevertMirror) {
                    /*PhotoModule.mirrorBitmap(bitmap, orientation);*/
                }
                byte[] jpeg = BitmapHelper.bitmap2byteArrayTurbo(bitmap, 100);
                LogHelper.i(TAG, "onPreviewScreenShot bitmap:" + bitmap);
                if (mDataBeautyCallback != null && jpeg != null) {
                    mDataBeautyCallback.onThumbCallback(jpeg);
                }
            }
        });
    }

    public Bitmap getPreviewBitmap(int downsample) {
//        if (mDrawBounds == null || mDrawBounds.width() <= 0 || mDrawBounds.height() <= 0) {
//            return null;
//        }
//        synchronized (object) {
//            queueEvent(new Runnable() {
//                @Override
//                public void run() {
//                    synchronized (object) {
//                        Rect rcDrawBounds = mDrawBounds;
//                        int y = mResolutionHeight - mDrawBounds.height() - mDrawBounds.top;
//                        mPreviewBitmap = createBitmapFromGLSurface(rcDrawBounds.left, y, rcDrawBounds.width(),
//                                rcDrawBounds.height());
//                        object.notify();
//                    }
//                }
//            });
//            try {
//                object.wait(300);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        return null;
    }

    public interface SurfaceTextureListener {

        public void onBfSurfaceTextureUpdate(SurfaceTexture surface);
    }

    private SurfaceTextureListener mListener;

    public void setSurfaceTextureListener(SurfaceTextureListener listener) {
        mListener = listener;
    }

    /**
     * 音视频录制对象
     */
    private TuSDKMediaRecoder mMediaRecorder;

    /**
     * 切换摄像时需要恢复录制
     */
    private boolean mResumeRecord = false;
    /**
     * 当前是否正在录制视频
     */
    private boolean mRecording = false;
    private String mFileName;


    //========================  TuSDKMediaRecoder 视频录制对象 ======================== //
    public static interface MediaRecorderInfoListener {
        public void onMediaRecoderProgressChanged(float durationTime);

        public void onMediaRecoderStateChanged(TuSDKMediaRecoder.State state);
    }

    private MediaRecorderInfoListener mMediaRecorderInfoListener;

    public void setMediaRecorderInfoListener(MediaRecorderInfoListener listener) {
        mMediaRecorderInfoListener = listener;
    }

    public void setOutputFile(String fileName) {
        mFileName = fileName;
    }

    /**
     * 获取 TuSDKMediaRecoder 视频录制对象
     *
     * @return TuSDKMediaRecoder
     */
    private TuSDKMediaRecoder getMediaRecorder() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new TuSDKMediaRecoder();

            mMediaRecorder.setDelegate(new TuSDKMediaRecoder.TuSDKMediaRecoderDelegate() {

                @Override
                public void onMediaRecoderProgressChanged(float v) {
                    if (mMediaRecorderInfoListener != null) {
                        mMediaRecorderInfoListener.onMediaRecoderProgressChanged(v);
                    }
                }

                @Override
                public void onMediaRecoderStateChanged(TuSDKMediaRecoder.State state) {
                    ImageSqlInfo videoSqlInfo = ImageSqlHelper.saveMp4ToAlbum(mCameraActivity, mMediaRecorder.getOutputFilePath());
                    ImageSqlHelper.notifyRefreshAblum(mCameraActivity, videoSqlInfo);
                    if (mMediaRecorderInfoListener != null) {
                        mMediaRecorderInfoListener.onMediaRecoderStateChanged(state);
                    }

                }
            });
        }
        if (mMediaRecorder != null) {
            TuSDKVideoEncoderSetting defaultRecordSetting = TuSDKVideoEncoderSetting.getDefaultRecordSetting();
            mMediaRecorder.setVideoEncoderSetting(defaultRecordSetting)
                    .setOutputFilePath(new File(mFileName)); // 自定义输出路径
        }

        return mMediaRecorder;
    }

    /**
     * 启动录制视频
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void startRecording() {
        if (mEGLContext == null) return;

        mRecording = true;
        updateOutputFilter();
        getMediaRecorder().setOrientationHint(mCameraActivity.getGSensorOrientation()/*getCurrOrientation()*/);
        getMediaRecorder().startRecording(mEGLContext, mSurfaceTexture);
    }

    /**
     * 停止录制视频
     */
    public void stopRecording() {
        if (mMediaRecorder != null)
            mMediaRecorder.stopRecording();

        mRecording = false;

    }

    public void pausedRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.pauseRecording();
        }
    }

    /**
     * 将 MediaRecorder 添加至 Filter
     */
    private void updateOutputFilter() {
        getMediaRecorder().updateFilter(mFilterEngine.getFilterWrap().getFilter());
    }

    private SnapShotListener mSnapShotListener;

    public void setSnapShotListener(SnapShotListener listener) {
        mSnapShotListener = listener;
    }

    public static interface SnapShotListener {
        public void onSnapShot(byte[] jpeg);
    }

    public boolean isFullScreenPreview(double aspectRatio) {
        double fullScreenRatio = findFullscreenRatio(getContext());
        if (Math.abs((aspectRatio - fullScreenRatio)) <= ASPECT_TOLERANCE) {
            return true;
        } else {
            return false;
        }
    }

    private static double findFullscreenRatio(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);

        double fullscreen;
        if (point.x > point.y) {
            fullscreen = (double) point.x / point.y;
        } else {
            fullscreen = (double) point.y / point.x;
        }
        return fullscreen;
    }
    public void setAspectRatio(double aspectRatio) {
        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            requestLayout();
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int previewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int previewHeight = MeasureSpec.getSize(heightMeasureSpec);
        boolean widthLonger = previewWidth > previewHeight;
        int longSide = (widthLonger ? previewWidth : previewHeight);
        int shortSide = (widthLonger ? previewHeight : previewWidth);
        if (mAspectRatio > 0) {
            double fullScreenRatio = findFullscreenRatio(getContext());
            if (Math.abs((mAspectRatio - fullScreenRatio)) <= ASPECT_TOLERANCE) {
                // full screen preview case
                if (longSide < shortSide * mAspectRatio) {
                    longSide = Math.round((float) (shortSide * mAspectRatio) / 2) * 2;
                } else {
                    shortSide = Math.round((float) (longSide / mAspectRatio) / 2) * 2;
                }
            } else {
                // standard (4:3) preview case
                if (longSide > shortSide * mAspectRatio) {
                    longSide = Math.round((float) (shortSide * mAspectRatio) / 2) * 2;
                } else {
                    shortSide = Math.round((float) (longSide / mAspectRatio) / 2) * 2;
                }
            }
        }
        if (widthLonger) {
            previewWidth = longSide;
            previewHeight = shortSide;
        } else {
            previewWidth = shortSide;
            previewHeight = longSide;
        }
        mMeasuredWidth = previewWidth;
        mMeasuredHeight = previewHeight;
        setMeasuredDimension(previewWidth, previewHeight);
    }
    //*/

    public void setPauseed(boolean pause) {
        mPauseed = pause;
    }

    public class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            long now = System.currentTimeMillis();
            switch (msg.what) {
                case MSG_SAVE_THUMBNAIL:
                    //进入相机执行
//                    LocalData datas = (LocalData) msg.obj;
//                    startThumbnailTworker(datas, null);
                    break;
                case MSG_LOAD_DECODE_THUMBNAIL:
//                    LocalData localdata = (LocalData) msg.obj;
//                    MetadataLoader.loadMetadata(getApplicationContext(), localdata);
//                    int dataType = localdata.getLocalDataType();
//                    if (dataType != LocalData.LOCAL_IMAGE
 //                           && dataType != LocalData.LOCAL_IN_PROGRESS_DATA
 //                           && dataType != LocalData.LOCAL_VIDEO) {
 //                       return;
 //                  }
//                    byte[] jpegData = null;
//                   /* ModuleController cameraModule = CameraActivity.this.getCurrentModuleController();
//                    if (cameraModule instanceof PhotoModule) {
//                        PhotoModule photoModule = (PhotoModule) cameraModule;
//                        jpegData = photoModule.getFreemeJpegImageData();
//                        photoModule.setFreemeJpegImageData(null);
//                    }*/
//                    //拍完照片执行
//
//                    startThumbnailTworker(localdata, jpegData);
                    break;
                case MSG_NOT_READY_IMAGE_THUMBNAIL:
                    //handleNotReadyImageMsg();
                    break;
                default:
                    break;
            }
        }
    }

    public WorkerHandler getWorkerHandler() {
        return mWorkerHandler;
    }
}
