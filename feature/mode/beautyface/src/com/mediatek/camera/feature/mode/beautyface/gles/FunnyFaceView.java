package com.mediatek.camera.feature.mode.beautyface.gles;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.feature.mode.beautyface.tutu.BeautyFaceView;
import com.mediatek.camera.feature.mode.beautyface.gdx.helper.ApplictionImpl;
import com.mediatek.camera.feature.mode.beautyface.gdx.helper.GraphicsImpl;

import com.mediatek.camera.feature.mode.beautyface.jni.JniStub.LandmarkInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lasque.tusdk.core.face.FaceAligment;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class FunnyFaceView implements GLAppListener {
    static {
        System.loadLibrary("gdx");
    }

    private final static String TAG = "FunnyFaceView";
    private ApplictionImpl mApp;
    private SpriteBatch mSpriteBatch;
    private int mWidth;
    private int mHeight;
    private FunnyPictureCallback mCallback;
    private final static float FACE_PRIM_SIZE = 226f;
    private final static int FACE_MIN_SIZE = 80;
    private final static int FACE_MAX_SIZE = 350;
    protected long lastFrameTime = System.nanoTime();
    protected float deltaTime = 0;
    private float mStateTime;
    private CameraActivity mCamera;
    private ArrayList<ItemInfo> mCurrItemList;
    private float mFacePrimSize;
    private volatile boolean mIsSwitching = false;
    private volatile boolean mIsDispose = false;
    private volatile boolean mIsShowing = false;
    private BeautyFaceView mBeautyView;
    private ShapeRenderer mShapeRender;
    private BitmapFont mBitmapFont;
    private Object mLockObj = new Object();
    private boolean mIsNeedCapture;
//    private int mFaceMinSizePx;
//    private int mFaceMaxSizePx;

    private WorkerHandler mWorkerHandler;
    private static final int MSG_SAVE_THUMBNAIL = 0x01;
    private static final int MSG_LOAD_DECODE_THUMBNAIL = 0x02;
    private static final int MSG_NOT_READY_IMAGE_THUMBNAIL = 0x03;

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void show() {
        mIsShowing = true;
    }

    public void hide() {
        mIsShowing = false;
    }

    public void takePicture() {
        mIsNeedCapture = true;
    }

    public boolean isSwitching() {
        Log.i("ffboptimize", "isSwitching:" + mIsSwitching);
        return mIsSwitching;
    }

    public static class AnchorInfo {
        /*
         * azmohan center offset relative textureRegion orign center ,use libgdx
         * coordinate system center offset will join draw arthemetic.so dont mul
         * scale. x,y offset only decide to next textureRegion orign center.use
         * libgdx coordinate system, it will not join draw arthemetic,so you
         * need mull scale.
         */
        public float x;
        public float y;
        public float x_offset;
        public float y_offset;
        public float center_x_offset;
        public float center_y_offset;
        public boolean normal_draw;

        public AnchorInfo(boolean standard, float x, float y, float x_offset, float y_offset,
                          float center_x_offset, float center_y_offset) {
            this.normal_draw = standard;
            this.x = x;
            this.y = y;
            this.x_offset = x_offset;
            this.y_offset = y_offset;
            this.center_x_offset = center_x_offset;
            this.center_y_offset = center_y_offset;
        }
    }

    public static class ItemInfo {
        public List<String> resNameList;
        public List<Texture> textureList;
        public int resClipWidth;
        public int resClipHeight;
        public String referPoint;
        public int anchorCXOffset;
        public int anchorCYOffset;
        public int anchorXOffset;
        public int anchorYOffset;
        public int frameSumDuration;
        public Animation anim;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            return builder.append("ItemInfo:").append("resName = ").append(resNameList)
                    .append(",resClipWidth = ").append(resClipWidth).append("resClipHeight = ")
                    .append(resClipHeight).append(",referPoint = ").append(referPoint)
                    .append(",anchorCXOffset = ").append(anchorCXOffset)
                    .append(",anchorCYOffset = ").append(anchorCYOffset)
                    .append(",anchorXOffset = ").append(anchorXOffset).append(",anchorYOffset = ")
                    .append(anchorYOffset).append(",frameSumDuration = ").append(frameSumDuration)
                    .toString();
        }
    }

    public class RunnableCommand implements Runnable {
        public final static int CREATE_TEXTURE_COMMAND = 0;
        public final static int DESTRORY_TEXTURE_COMMAND = 1;
        public final static int TAKE_PICKTURE_COMMAND = 2;
        private int mCommand;
        private Texture mTexture;
        private Pixmap mPixmap;
        private boolean mIsFinish;

        public RunnableCommand(int command) {
            mCommand = command;
        }

        public RunnableCommand(int command, Pixmap pixmap) {
            mCommand = command;
            mPixmap = pixmap;
        }

        public RunnableCommand(int command, boolean isFinish) {
            mCommand = command;
            mIsFinish = isFinish;
        }

        public Texture getTexture() {
            return mTexture;
        }

        @Override
        public void run() {
            switch (mCommand) {
                case CREATE_TEXTURE_COMMAND:
                    synchronized (mLockObj) {
                        mTexture = new Texture(mPixmap);
                        mLockObj.notify();
                    }
                    break;
                case DESTRORY_TEXTURE_COMMAND:
                    synchronized (mLockObj) {
                        disposeResWithoutInfoLock();
                        if (mIsFinish) {
                            disposeCreateRes();
                        }
                        mLockObj.notify();
                    }
                    break;
                case TAKE_PICKTURE_COMMAND:
                    capture();
                    break;
                default:
                    break;
            }
        }
    }

    private String mEffect = "";

    public void switchEffectInWorkThread(final String effect) {

        if (mIsSwitching || mEffect.equals(effect)) {
            return;
        }
        mIsSwitching = true;
        mEffect = effect;
        Thread thread = new Thread("switch effect") {
            @Override
            public void run() {
                super.run();
                switchEffect(mEffect);
            }
        };
        thread.start();
    }

    public static interface FunnyPictureCallback {
        public void onFunnyPictureTaken(final byte[] originalJpegData, int width, int height);
    }

    public void setFunnyPictureCallback(FunnyPictureCallback callback) {
        mCallback = callback;
    }

    public FunnyFaceView(Context context) {
        mCamera = (CameraActivity) context;
        mApp = new ApplictionImpl(context, this);
        HandlerThread t = new HandlerThread("thumbnail-creation-thread");
        t.start();
        mWorkerHandler = new WorkerHandler(t.getLooper());

        Gdx.app = mApp;
        Gdx.files = mApp.getFiles();
        Gdx.graphics = mApp.getGraphics();
        Gdx.gl = mApp.getGraphics().getGL20();
        Gdx.gl20 = mApp.getGraphics().getGL20();
//        mFaceMaxSizePx = CameraUtil.dpToPixel(context, FACE_MAX_SIZE);
//        mFaceMinSizePx = CameraUtil.dpToPixel(context, FACE_MIN_SIZE);
    }

    private static TextureRegion[] arraysToArray(TextureRegion[][] regions) {
        int count = regions.length * regions[0].length;
        if (regions[0].length == 0) {
            count = regions.length;
        }
        TextureRegion[] regionArray = new TextureRegion[count];
        int index = 0;
        for (int i = 0; i < regions.length; i++) {
            for (int j = 0; j < regions[i].length; j++) {
                regionArray[index++] = regions[i][j];
            }
        }
        return regionArray;
    }

    private static String getStringFromInputStream(InputStream a_is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(a_is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }

    public static String readAssertResource(Context context, String strAssertFileName) {
        AssetManager assetManager = context.getAssets();
        String strResponse = "";
        try {
            InputStream ims = assetManager.open(strAssertFileName);
            strResponse = getStringFromInputStream(ims);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResponse;
    }

    private static float getFacePrimSize(String attrType, String attrContainer) {
        float size = 0;
        if (TextUtils.isEmpty(attrContainer)) {
            return size;
        }
        try {
            attrContainer = attrContainer.replaceAll(" ", "").replaceAll("\n", "")
                    .replaceAll("\t", "");
            JSONObject jo = new JSONObject(attrContainer);
            size = jo.getInt(attrType);
        } catch (JSONException e) {
            Log.v(TAG, "spiltItemInfoDatas( e:" + e.toString());
        }
        return size;
    }

    public ArrayList<ItemInfo> spiltItemInfoDatas(String itemName, String result) {
        if (TextUtils.isEmpty(result)) {
            return null;
        }
        try {
            result = result.replaceAll(" ", "").replaceAll("\n", "").replaceAll("\t", "");
            Log.i(TAG, "result:" + result);
            JSONObject jo = new JSONObject(result);
            JSONArray itemArray = jo.getJSONArray("itemList");
            ArrayList<ItemInfo> itemList = new ArrayList<ItemInfo>();
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject item = itemArray.getJSONObject(i);
                ItemInfo itemInfo = new ItemInfo();
                itemInfo.resClipWidth = item.getInt("resClipWidth");
                itemInfo.resClipHeight = item.getInt("resClipHeight");
                itemInfo.referPoint = item.getString("referPoint");
                itemInfo.anchorCXOffset = item.getInt("anchorCXOffset");
                itemInfo.anchorCYOffset = item.getInt("anchorCYOffset");
                itemInfo.anchorXOffset = item.getInt("anchorXOffset");
                itemInfo.anchorYOffset = item.getInt("anchorYOffset");
                itemInfo.frameSumDuration = item.getInt("frameSumDuration");
                itemInfo.resNameList = new ArrayList<String>();
                itemInfo.textureList = new ArrayList<Texture>();
                JSONArray resArray = item.getJSONArray("resName");
                long time = System.currentTimeMillis();
                long time1 = System.currentTimeMillis();
                List<TextureRegion> regionList = new ArrayList<TextureRegion>();
                for (int j = 0; j < resArray.length(); j++) {
                    JSONObject resItem = resArray.getJSONObject(j);
                    String key = "name";
                    String resName = resItem.getString(key);
                    time = System.currentTimeMillis();
                    Pixmap pixmap = new Pixmap(Gdx.files.internal(itemName + "/" + resName));
                    time1 = System.currentTimeMillis();
                    Log.i("ffboptimize", "new Pixmap spend:" + (time1 - time) + ",resName:"
                            + (itemName + "/" + resName));
                    time = System.currentTimeMillis();
                    RunnableCommand rc = new RunnableCommand(
                            RunnableCommand.CREATE_TEXTURE_COMMAND, pixmap);
                    synchronized (mLockObj) {
                        mBeautyView.queueEvent(rc);
                        Log.i("ffboptimize", "waiting");
                        try {
                            mLockObj.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            pixmap.dispose();
                            Log.i("ffboptimize", "wait exception:" + e.toString());
                            continue;
                        }
                    }
                    Texture texture = rc.getTexture();
                    pixmap.dispose();
                    time1 = System.currentTimeMillis();
                    Log.i("ffboptimize", "new Texture spend:" + (time1 - time));
                    time = System.currentTimeMillis();
                    TextureRegion[] region = arraysToArray(TextureRegion.split(texture,
                            itemInfo.resClipWidth, itemInfo.resClipHeight));
                    time1 = System.currentTimeMillis();
                    Log.i("ffboptimize", "split texture spend:" + (time1 - time));
                    regionList.addAll(Arrays.asList(region));
                    itemInfo.resNameList.add(resName);
                    itemInfo.textureList.add(texture);
                }

                boolean isRepeated = item.optBoolean("isRepeated", false);
                if (isRepeated) {
                    JSONArray repeatInfo = item.optJSONArray("repeatInfo");
                    if (repeatInfo != null) {
                        List<TextureRegion> tmpList = new ArrayList<TextureRegion>();
                        tmpList.addAll(regionList);
                        int recordCnt = 0;
                        Log.i("ffboptimize", "repeatInfo leghth:" + repeatInfo.length());
                        for (int k = 0; k < repeatInfo.length(); k++) {
                            JSONObject infoItem = repeatInfo.getJSONObject(k);
                            int cnt = infoItem.getInt("repeatCount");
                            int startIndex = infoItem.getInt("repeatStartIndex");
                            int endIndex = infoItem.getInt("repeatEndIndex");
                            int span = endIndex - startIndex;
                            List<TextureRegion> repeatList = tmpList.subList(startIndex, endIndex);
                            for (int m = 0; m < cnt; m++) {
                                regionList.addAll(recordCnt + startIndex + m * span, repeatList);
                            }
                            recordCnt += span * cnt;
                        }
                    }
                }

                TextureRegion[] textureRegion = (TextureRegion[]) regionList
                        .toArray(new TextureRegion[regionList.size()]);
                float duration = itemInfo.frameSumDuration / 1000.f / textureRegion.length;
                itemInfo.anim = new Animation(duration, textureRegion);
                itemList.add(itemInfo);
            }
            return itemList;
        } catch (JSONException e) {
            Log.v("ffboptimize", "spiltItemInfoDatas( e:" + e.toString());
        }
        return null;
    }

    public void disposeRes(boolean isFinish) {
        mIsDispose = true;
        synchronized (mLockObj) {
            RunnableCommand rc = new RunnableCommand(RunnableCommand.DESTRORY_TEXTURE_COMMAND,
                    isFinish);
            if (mBeautyView != null) {
                mBeautyView.queueEvent(rc);
                try {
                    mLockObj.wait();
                } catch (InterruptedException e) {
                    Log.i("ffboptimize", "wait disposeRes exception:" + e.toString());
                }
            }
            mEffect = "";
        }
        mIsDispose = false;
    }

    private void disposeResWithoutInfoLock() {
        if (mCurrItemList != null) {
            for (ItemInfo item : mCurrItemList) {
                for (Texture texture : item.textureList) {
                    texture.dispose();
                }
                item.textureList.clear();
                item.textureList = null;
                item.resNameList.clear();
                item.resNameList = null;
                item.anim = null;
            }
            mCurrItemList.clear();
            mCurrItemList = null;
        }
    }

    private void disposeCreateRes() {
        if (mSpriteBatch != null) {
            mSpriteBatch.dispose();
            mSpriteBatch = null;
        }
        if (mShapeRender != null) {
            mShapeRender.dispose();
            mShapeRender = null;
        }

        if (mBitmapFont != null) {
            mBitmapFont.dispose();
            mBitmapFont = null;
        }
    }

    private void switchEffect(String effectName) {
        if (mBeautyView == null) {
            return;
        }
        long time = System.currentTimeMillis();
        synchronized (mLockObj) {
            RunnableCommand rc = new RunnableCommand(RunnableCommand.DESTRORY_TEXTURE_COMMAND);
            mBeautyView.queueEvent(rc);
            try {
                mLockObj.wait();
            } catch (InterruptedException e) {
                Log.i("ffboptimize", "wait DESTRORY_TEXTURE_COMMAND exception:" + e.toString());
            }
        }
        long time1 = System.currentTimeMillis();
        Log.i("ffboptimize", "dispose spent time:" + (time1 - time));
        time = System.currentTimeMillis();
        String paramFileName = effectName + "/params.txt";
        String params = readAssertResource(mCamera, paramFileName);
        String attrFileName = effectName + "/attribute.txt";
        String attrs = readAssertResource(mCamera, attrFileName);
        time1 = System.currentTimeMillis();
        Log.i("ffboptimize", "readAssertResource spent time:" + (time1 - time));
        time = System.currentTimeMillis();
        mCurrItemList = spiltItemInfoDatas(effectName, params);
        mFacePrimSize = getFacePrimSize("facePrimSize", attrs);
        time1 = System.currentTimeMillis();
        Log.i("ffboptimize", "spiltItemInfoDatas spent time:" + (time1 - time));
        mIsSwitching = false;
    }

    @Override
    public void create(BeautyFaceView bView) {
        mBeautyView = bView;
        mShapeRender = new ShapeRenderer();
        mSpriteBatch = new SpriteBatch();
        mBitmapFont = new BitmapFont();
        mBitmapFont.setColor(Color.BLACK);
        mStateTime = 0;
    }

    @Override
    public void dispose() {
        Log.i("lucy", "FunnyFaceView dispose");
        disposeRes(true);
    }

    public void disposeInGlThread() {
        mIsDispose = true;
        disposeResWithoutInfoLock();
        disposeCreateRes();
        mIsDispose = false;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    private void drawElements(TextureRegion currRegion, AnchorInfo anchor, float scale,
                              int orientation, float angle) {
        float orignW = currRegion.getRegionWidth();
        float orignH = currRegion.getRegionHeight();
        float orignX = orignW / 2 + anchor.center_x_offset;
        float orignY = orignH / 2 + anchor.center_y_offset;
        float pivotX = anchor.x + anchor.x_offset * scale;
        float pivotY = anchor.y + anchor.y_offset * scale;
        if (anchor.x_offset != 0 || anchor.y_offset != 0) {
            Matrix matrix = new Matrix();
            matrix.preRotate(angle, anchor.x, anchor.y);
            float[] point = new float[2];
            point[0] = pivotX;
            point[1] = pivotY;
            matrix.mapPoints(point);
            pivotX = point[0];
            pivotY = point[1];
        }
        float x = pivotX - orignW / 2 - anchor.center_x_offset;
        float y = pivotY - orignH / 2 - anchor.center_y_offset;
        if (anchor.normal_draw) {
            x = 0;
            y = 0;
            scale = 1.0f;
        }
        float finalAngle = orientation + angle;
        Log.i(TAG, "drawElements x:" + x + ",y:" + y + ",orignX:" + orignX + ",orignY:" + orignY
                + "orignW:" + orignW + ",orignH:" + orignH + ",scale:" + scale + ",angle:"
                + orientation + ",currRegion:" + currRegion);
        mSpriteBatch.draw(currRegion, x, y, orignX, orignY, orignW, orignH, scale, scale,
                finalAngle);
    }

    private PointF getAnchorXY(String anchorStr, LandmarkInfo mark) {
        if (TextUtils.isEmpty(anchorStr) || mark == null) {
            return null;
        }
//        boolean isMirror = (mCamera.getAppUi().getCurrentCameraId().equals("1"));
        boolean isMirror = true;
        PointF referPoint = new PointF(0, 0);
        if ("leftEye".equals(anchorStr)) {
            if (isMirror) {
                referPoint.set(mark.mRightEye);
            } else {
                referPoint.set(mark.mLeftEye);
            }
        } else if ("rightEye".equals(anchorStr)) {
            if (isMirror) {
                referPoint.set(mark.mLeftEye);
            } else {
                referPoint.set(mark.mRightEye);
            }
        } else if ("nose".equals(anchorStr)) {
            referPoint.set(mark.mNose);
        } else if ("leftMouth".equals(anchorStr)) {
            referPoint.set(mark.mLeftMouth);
            if (isMirror) {
                referPoint.set(mark.mRightMouth);
            } else {
                referPoint.set(mark.mLeftMouth);
            }
        } else if ("rightMouth".equals(anchorStr)) {
            if (!isMirror) {
                referPoint.set(mark.mRightMouth);
            } else {
                referPoint.set(mark.mLeftMouth);
            }
        } else if ("eyesCenter".equals(anchorStr)) {
            float x = (mark.mLeftEye.x + mark.mRightEye.x) / 2.0f;
            float y = (mark.mLeftEye.y + mark.mRightEye.y) / 2.0f;
            referPoint.set(x, y);
        } else if ("mouthCenter".equals(anchorStr)) {
            float x = (mark.mLeftMouth.x + mark.mRightMouth.x) / 2.0f;
            float y = (mark.mLeftMouth.y + mark.mRightMouth.y) / 2.0f;
            referPoint.set(x, y);
        }
        return referPoint;
    }

    private AnchorInfo computeAnchorInfo(ItemInfo item, LandmarkInfo mark, float scale,
                                         int orietation) {
        PointF anchorXY = getAnchorXY(item.referPoint, mark);
        float x = anchorXY.x;
        float y = anchorXY.y;
        float scale_x_o = item.anchorXOffset;
        float scale_y_o = item.anchorYOffset;
        float x_o = 0f;
        float y_o = 0f;
        float c_x_o = item.anchorCXOffset;
        float c_y_o = item.anchorCYOffset;
        switch (orietation) {
            case 0:
                x_o = scale_x_o;
                y_o = scale_y_o;
                break;
            case 90:
                x_o = -scale_y_o;
                y_o = scale_x_o;
                break;
            case 180:
                x_o = -scale_x_o;
                y_o = -scale_y_o;
                break;
            case 270:
                x_o = scale_y_o;
                y_o = -scale_x_o;
                break;
        }

        if (item.referPoint.equals("standard")) {
            return new AnchorInfo(true, x, y, x_o, y_o, c_x_o, c_y_o);
        } else {
            return new AnchorInfo(false, x, y, x_o, y_o, c_x_o, c_y_o);
        }
    }

    private void drawItem(float scale, int orientation, float angle, LandmarkInfo markInfo) {
        if (mCurrItemList != null) {
            for (ItemInfo item : mCurrItemList) {
                TextureRegion currRegion = item.anim.getKeyFrame(mStateTime, true);
                AnchorInfo anchor = computeAnchorInfo(item, markInfo, scale, orientation);
                drawElements(currRegion, anchor, scale, orientation, angle);
            }
        }
    }

    private void handleRGB565Data() {
        long time = System.currentTimeMillis();
        final int data[] = this.getJpegDataFromGpu565(0, 0, mWidth, mHeight);
        long time1 = System.currentTimeMillis();
        Log.i("ffbcapture", "getPixel spent:" + (time1 - time));
//        Handler handler = mCamera.getWorkerHandler();
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
                int size = mWidth * mHeight;
                bitmap.setPixels(data, size - mWidth, -mWidth, 0, 0, mWidth, mHeight);
                short sdata[] = new short[size];
                ShortBuffer sbuf = ShortBuffer.wrap(sdata);
                bitmap.copyPixelsToBuffer(sbuf);
                for (int i = 0; i < size; ++i) {
                    // BGR-565 to RGB-565
                    short v = sdata[i];
                    sdata[i] = (short) (((v & 0x1f) << 11) | (v & 0x7e0) | ((v & 0xf800) >> 11));
                }
                sbuf.rewind();
                bitmap.copyPixelsFromBuffer(sbuf);
                float scaleWidth = ((float) mHeight) / mWidth;
                float scaleHeight = ((float) mWidth) / mHeight;
                // 取得想要缩放的matrix参数.
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                // 得到新的图片.
                Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, mWidth, mHeight, matrix, true);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                newbm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] jpegData = baos.toByteArray();
                if (mCallback != null) {
                    mCallback.onFunnyPictureTaken(jpegData, mWidth, mHeight);
                }
            }
        });
    }

    public void capture() {
        if (mIsNeedCapture) {
            mIsNeedCapture = false;
            handleRGB565Data();
        }
    }

    public void render(float deviceAngle, FaceAligment[] faceAligments) {
        if (!mIsShowing || mIsSwitching || mIsDispose) {
            return;
        }
        long time = System.nanoTime();
        deltaTime = (time - lastFrameTime) / 1000000000.0f;
        lastFrameTime = time;
        mStateTime += deltaTime;
        if (faceAligments != null && faceAligments.length > 0) {
            RectF face = faceAligments[0].rect;
            Matrix matrix = new Matrix();
            int width = getHasVirtualKeyWidth();
            int height = getHasVirtualKeyHeight();
            matrix.setScale(width, -height);//设置Matrix进行缩放，sx,sy控制X,Y方向上的缩放比例
            matrix.postTranslate(0, height);//控制Matrix进行平移
            matrix.mapRect(face);//对RectF的坐标点进行矩阵变换
            int faceW = (int) face.width();
            int faceH = (int) face.height();
            int abs = Math.abs(faceH - faceW);
            if (/*faceW < mFaceMinSizePx || faceW > mFaceMaxSizePx || abs > 80*/false) {
//                mCamera.showOrNotFFBNoFaceIndicator(true);
                return;
            }
            LandmarkInfo landmarkInfo = new LandmarkInfo();
            landmarkInfo.mFaceRect.set(face);
            float points[] = new float[2];
            PointF pointF = faceAligments[0].marks[40];
            points[0] = pointF.x;
            points[1] = pointF.y;
            matrix.mapPoints(points);
            landmarkInfo.mLeftEye.set(points[0], points[1]);

            pointF = faceAligments[0].marks[46];
            points[0] = pointF.x;
            points[1] = pointF.y;
            matrix.mapPoints(points);
            landmarkInfo.mRightEye.set(points[0], points[1]);

            pointF = faceAligments[0].marks[30];
            points[0] = pointF.x;
            points[1] = pointF.y;
            matrix.mapPoints(points);
            landmarkInfo.mNose.set(points[0], points[1]);

            pointF = faceAligments[0].marks[48];
            points[0] = pointF.x;
            points[1] = pointF.y;
            matrix.mapPoints(points);
            landmarkInfo.mLeftMouth.set(points[0], points[1]);

            pointF = faceAligments[0].marks[54];
            points[0] = pointF.x;
            points[1] = pointF.y;
            matrix.mapPoints(points);
            landmarkInfo.mRightMouth.set(points[0], points[1]);


            landmarkInfo.x_angle = faceAligments[0].roll;
            landmarkInfo.y_angle = faceAligments[0].yaw;
            landmarkInfo.z_angle = faceAligments[0].pitch;
            float angle = deviceAngle - landmarkInfo.x_angle;
            if (mFacePrimSize == 0) {
                mFacePrimSize = FACE_PRIM_SIZE;
            }
            float scale = faceW / mFacePrimSize;
            mSpriteBatch.begin();
            drawItem(scale, 0, angle, landmarkInfo);
            mSpriteBatch.end();
//            mCamera.showOrNotFFBNoFaceIndicator(false);
        } else {
//            mCamera.showOrNotFFBNoFaceIndicator(true);
        }
    }

    @Override
    public void create() {

    }

    @Override
    public void resize(int width, int height) {
        mWidth = width;
        mHeight = height;
        if (Gdx.graphics != null && Gdx.graphics instanceof GraphicsImpl) {
            GraphicsImpl graphics = (GraphicsImpl) Gdx.graphics;
            graphics.resize(mWidth, mHeight);
        }
    }

    @Override
    public void render() {

    }

    public int[] getJpegDataFromGpu565(int x, int y, int w, int h) {
        int size = w * h;
        ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
        buf.order(ByteOrder.nativeOrder());
        GLES20.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, buf);
        //从显存中读取帧数据了
        int data[] = new int[size];
        buf.asIntBuffer().get(data);
        buf = null;
        return data;
    }

    /**
     * 通过反射，获取包含虚拟键的整体屏幕高度
     *
     * @return
     */
    private int getHasVirtualKeyWidth() {
        int width = 0;
        Display display = mCamera.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            width = dm.widthPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return width;
    }

    private int getHasVirtualKeyHeight() {
        int height = 0;
        Display display = mCamera.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            height = dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return height;
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
//                            && dataType != LocalData.LOCAL_IN_PROGRESS_DATA
//                            && dataType != LocalData.LOCAL_VIDEO) {
//                        return;
//                    }
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
