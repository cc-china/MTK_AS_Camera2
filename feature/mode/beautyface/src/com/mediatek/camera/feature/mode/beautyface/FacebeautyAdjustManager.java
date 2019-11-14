/*
 * File name: FacebeautyAdjustManager.java
 * 
 * Description: Parameter of face beauty filter mode adjust control
 *              panel, can select level about skin-smooth, face-slim
 *              and eye-enlarge.
 *
 * Author: Theobald_wu, contact with wuqizhi@tydtech.com
 * 
 * Date: 2014-3-25   
 * 
 * Copyright (C) 2014 TYD Technology Co.,Ltd.
 * 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.camera.feature.mode.beautyface;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.R;

import org.lasque.tusdk.core.seles.SelesParameters;
import org.lasque.tusdk.core.seles.sources.SelesOutInput;

import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil.Tag;

import android.widget.Toast;

//import com.mediatek.camera.CameraModule;
//import com.mediatek.camera.PhotoModule;
//import com.mediatek.camera.data.mediatekSceneModeData;

//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
/* tydtech:azmohan on: Tue, 19 May 2015 20:42:29 +0800
 * adjust camera simple ui
 */

// End of tydtech: azmohan

public class FacebeautyAdjustManager implements SettingAdjustView.OnPickerListener, OnClickListener {
    //    private static final String TAG = "[TYD_DEBUG]FacebeautyAdjustManager";
    private static final int MAX_ITEMS = 4;
    private static final int BEAUTY_FACE_VIEW_ADD = 0;
    private static final int BEAUTY_FACE_VIEW_REMOVE = 1;
    CameraActivity mContext;
    ViewGroup mListLayout;
    ViewGroup mDegreelayout;
    LinearLayout modeLayout;
    LinearLayout mDegreeView;
    ViewGroup mLayout;
    private SettingAdjustView mAdjustCtrl;
    private boolean mShowingCtrl = false;
    private ViewGroup mPanel;
    private int mCurrentItem = -2;
    private boolean mIsExited = false;
    //SharedPreferences sharedPreferences;
    private ViewGroup mSettingList;
    private TextView mDegree;
    private ImageView mDegreeIv;
    private TextView mModeBtnDown;
    private TextView modeBtn;
    private Drawable mPopDownDrawable;
    private Drawable mPopUpDrawable;
    //private PhotoModule mPhotoModule;
//    private BeautyFaceViewCtrl mBeautyFaceViewCtrl = new BeautyFaceViewCtrl();
    private boolean isShownDegree = false;
    private MainHandler mMainHandler;
    RelativeLayout.LayoutParams lp;
    ViewGroup mRootView;
    private static final Tag TAG = new Tag(FacebeautyAdjustManager.class.getSimpleName());

    public FacebeautyAdjustManager(ViewGroup Layout, CameraActivity context) {
        super();
        mLayout = Layout;
        mContext = context;
        /*CameraModule module = context.getCurrentModule();
        if (module instanceof PhotoModule) {
            mPhotoModule = (PhotoModule) module;
        }*/
//         sharedPreferences = context.getSharedPreferences("face",
//         Activity.MODE_PRIVATE);
        mMainHandler = new MainHandler(mContext.getMainLooper());
        initView();
    }

    public static void setFilterParams(SelesOutInput filter) {
        if (filter != null) {
            float tune = normalize(FaceBeautyUtil.getVfbParamsLevel(FaceBeautyUtil.SKIN_TONING)); //SKinCare:    ~ 皮肤护理 smooth
            float slim = normalize(FaceBeautyUtil.getVfbParamsLevel(FaceBeautyUtil.FACE_SLIMING)); //Slim: 瘦脸  ~ chinSize 
            float eyeEnlarge = normalize(FaceBeautyUtil.getVfbParamsLevel(FaceBeautyUtil.EYE_ENLARGE)); //Enlarge: 大眼   ~ eyeSize
            LogHelper.i(TAG, "tune = " + tune + "\tslim = " + slim + "\teyeEnlarge = " + eyeEnlarge);
            List<SelesParameters.FilterArg> args = filter.getParameter().getArgs();
            for (SelesParameters.FilterArg arg : args) {
                /*
                 * smoothing 润滑
                 * mixed     效果
                 * eyeSize   大眼
                 * chinSize  瘦脸
                 *
                 */
                if (arg.equalsKey("smoothing")) {
                    // 取值范围： 0 ~ 1.0
                    arg.setPrecentValue(tune);
                } else if (arg.equalsKey("whitening")) {
                    arg.setPrecentValue(0.0f);
                } else if (arg.equalsKey("eyeSize")) {
                    arg.setPrecentValue(eyeEnlarge);
                } else if (arg.equalsKey("chinSize")) {
                    arg.setPrecentValue(slim);
                } else if (arg.equalsKey("mixied")) {
//                    Log.i("sunny", "set mixied:" + tune);
//                    arg.setPrecentValue(tune);
                }

            }
            filter.submitParameter();
            LogHelper.i(TAG, "after submitParameter");
        } else {
            LogHelper.i(TAG, "filter = null");
        }
    }

    private static float normalize(float value) {
        float ret = value / 255.0f;
        return ret;
    }

    /**
     * All UI init code here
     */
    private void initView() {
        mPopDownDrawable = mContext.getResources().getDrawable(R.drawable.mode_setting_pop_up);
        mPopDownDrawable.setBounds(0, 0, mPopDownDrawable.getIntrinsicWidth(),
                mPopDownDrawable.getIntrinsicHeight());
        mPopUpDrawable = mContext.getResources().getDrawable(R.drawable.mode_setting_pop_down);
        mPopUpDrawable.setBounds(0, 0, mPopUpDrawable.getIntrinsicWidth(),
                mPopUpDrawable.getIntrinsicHeight());

        LayoutInflater mInflater = LayoutInflater.from(mContext);
        mSettingList = (ViewGroup) mInflater.inflate(
                mContext.getResources().getLayout(R.layout.facebeauty_setting_panel), null);

        mListLayout = (ViewGroup) mSettingList.findViewById(R.id.item_list);
        mDegreelayout = (ViewGroup) mSettingList.findViewById(R.id.Degree_layout);
        modeLayout = (LinearLayout) mSettingList.findViewById(R.id.mode_layout);
        mDegreelayout.setOnClickListener(this);
        modeLayout.setOnClickListener(this);
        mDegreeView = (LinearLayout) mSettingList.findViewById(R.id.degree_view);
        mDegree = (TextView) mSettingList.findViewById(R.id.Degree);
        mDegreeIv = (ImageView) mSettingList.findViewById(R.id.degreeiv);
        mModeBtnDown = (TextView) mSettingList.findViewById(R.id.mode_button_down);
        modeBtn = (TextView) mSettingList.findViewById(R.id.mode_button);
        lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);

        Point size = new Point();
        mContext.getWindowManager().getDefaultDisplay().getSize(size);
//        mScreenRatio = CameraUtil.determineRatio(size.x, size.y);

        lp.bottomMargin = mContext.getAppUi().getShutterRootView().getHeight();

        mSettingList.setOnClickListener(this);
        Resources res = mContext.getResources();
        mDegreeView.setOnClickListener(this);

        String[] facetitles = {
//                mContext.getResources().getString(R.string.pref_facebeauty_skin_smooth_title),
                mContext.getResources().getString(R.string.pref_facebeauty_skin_toning_title),
                mContext.getResources().getString(R.string.pref_facebeauty_face_slim_title),
                mContext.getResources().getString(R.string.pref_facebeauty_ruddy_title),
//                mContext.getResources().getString(R.string.pref_facebeauty_eye_circles_title)
        };

        Drawable[] faceviews = {
//                mContext.getResources().getDrawable(R.drawable.freeme_beauty_smooth),
                mContext.getResources().getDrawable(R.drawable.freeme_beauty_toning),
                mContext.getResources().getDrawable(R.drawable.freeme_beauty_slim),
                mContext.getResources().getDrawable(R.drawable.freeme_beauty_enlarge),
//                mContext.getResources().getDrawable(R.drawable.freeme_beauty_circles),
        };

        for (int i = 0; i < facetitles.length; i++) {
            View item = mInflater.inflate(R.layout.facebeauty_setting_item, mListLayout, false);
            TextView facetitles1 = (TextView) item.findViewById(R.id.title);
            ImageView faceviews1 = (ImageView) item.findViewById(R.id.itemview);
            facetitles1.setText(facetitles[i]);
            faceviews1.setImageDrawable(faceviews[i]);
            mListLayout.addView(item);
            item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = mListLayout.indexOfChild(v);
                    if (index < 0) {
                        return;
                    }
                    TextView faceti = (TextView) v.findViewById(R.id.title);
                    ImageView faceiv = (ImageView) v.findViewById(R.id.itemview);
                    faceti.setSelected(true);
                    faceiv.setSelected(true);
                    LogHelper.i(TAG, "index:" + index);
                    switcher(index);
                }
            });
        }

        mModeBtnDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modeLayout.isShown()) {
                    modeLayout.setVisibility(View.GONE);
                    if (mDegreelayout.isShown()) {
                        mDegreelayout.setVisibility(View.GONE);
                        isShownDegree = true;
                    }
                    mModeBtnDown.setCompoundDrawables(null, null, mPopDownDrawable, null);
                } else {
                    if (isShownDegree) {
                        isShownDegree = false;
                        mDegreelayout.setVisibility(View.VISIBLE);
                    }
                    modeLayout.setVisibility(View.VISIBLE);
                    mModeBtnDown.setCompoundDrawables(null, null, mPopUpDrawable, null);
                }
            }
        });

//        Layout.addView(mSettingList, lp);
//        mBeautyFaceViewCtrl.init(mLayout, mContext);
//        mBeautyFaceViewCtrl.addView();

        // flate the beautyface layer and UI components
        addBeautyFaceView();
    }

    private void switcher(int index) {
        if (index != mCurrentItem && index != -1) {
            // collapseList(false);
            LogHelper.i(TAG, "getChildAt==" + mCurrentItem);
            if (mCurrentItem != -1 && mCurrentItem != -2) {
                TextView faceti = (TextView) mListLayout.getChildAt(mCurrentItem).findViewById(
                        R.id.title);
                ImageView faceiv = (ImageView) mListLayout.getChildAt(mCurrentItem).findViewById(
                        R.id.itemview);
                faceti.setSelected(false);
                faceiv.setSelected(false);
            } else {
                mDegree.setSelected(false);
                mDegreeIv.setSelected(false);
            }
            mCurrentItem = index;
            show(true);
        } else if (index != mCurrentItem && index == -1 && mDegree != null) {
            LogHelper.i(TAG, "mDegree==" + mCurrentItem);
            mDegree.setSelected(true);
            mDegreeIv.setSelected(true);
            if (mCurrentItem != -1 && mCurrentItem != -2) {
                TextView faceti = (TextView) mListLayout.getChildAt(mCurrentItem).findViewById(
                        R.id.title);
                ImageView faceiv = (ImageView) mListLayout.getChildAt(mCurrentItem).findViewById(
                        R.id.itemview);
                faceti.setSelected(false);
                faceiv.setSelected(false);
            }
            mCurrentItem = index;
            show(true);
        } else {
            LogHelper.i(TAG, "mDegreelayout.isShown()==" + mDegreelayout.isShown());
            if (mDegreelayout.isShown()) {
                if (index != -1) {

                    TextView faceti = (TextView) mListLayout.getChildAt(index).findViewById(
                            R.id.title);
                    ImageView faceiv = (ImageView) mListLayout.getChildAt(index).findViewById(
                            R.id.itemview);
                    faceti.setSelected(false);
                    faceiv.setSelected(false);
                }
                mDegree.setSelected(false);
                mDegreeIv.setSelected(false);
                hide(true);
            } else {
                if (index == -1) {
                    mDegree.setSelected(true);
                    mDegreeIv.setSelected(true);
                }
                show(true);
            }
        }
    }

    private void show(boolean isAnimate) {
        LogHelper.i(TAG, "show mCurrentItem==" + mCurrentItem);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        float sPixelDensity = metrics.density;
        if (mAdjustCtrl == null) {
            mAdjustCtrl = new SettingAdjustView(mContext, mCurrentItem, this);
            RelativeLayout.LayoutParams params = null;
            params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT);
            params.alignWithParent = true;
            params.addRule(RelativeLayout.ALIGN_BOTTOM);
            params.bottomMargin = mContext.getResources().getDimensionPixelSize(
                    R.dimen.setting_adjust_view_fb_bottom_margin);
            mDegreelayout.addView(mAdjustCtrl);
        }
        mAdjustCtrl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        mAdjustCtrl.initSlice(mCurrentItem);
        mShowingCtrl = true;
        mDegreelayout.setVisibility(View.VISIBLE);
        mAdjustCtrl.show(isAnimate);
    }

    private void hide(boolean isAnimate) {
        if (mListLayout == null || !mShowingCtrl || mAdjustCtrl == null) {
            return;
        }
        mDegreelayout.setVisibility(View.GONE);
        mAdjustCtrl.setVisibility(View.GONE);
        mShowingCtrl = false;
    }

    @Override
    public void func(int mode) {
        View item = mListLayout.getChildAt(mCurrentItem);
        if (mCurrentItem != -1) {
            /*
             * Modified by Droi Kimi Wu on 20151225 for optimize FN-FB
             * parameters process Editor editor = sharedPreferences.edit();
             * editor.putInt(String.valueOf(-1), 3); editor.commit();
             */
            FaceBeautyUtil.saveLevelIndex(FaceBeautyUtil.FB_GRADE, FaceBeautyUtil.CUSTOM);
            LogHelper.i(TAG, "get in FaceBeautyUtil.saveLevelIndex()");
        }
        SelesOutInput filter = ((CameraActivity) mContext).getmBeautyFaceView().getFilter();
        setFilterParams(filter);
        //xapi add start
        sendBroadcastReceiver(mode);
        //xapi add end
        LogHelper.i(TAG, "end of func()");
    }
    //xapi add start
    private void sendBroadcastReceiver(int mode) {
        Intent intent = new Intent();
        int[] modeAndLevel = new int[2];
        switch (mCurrentItem) {
            case -1:
                //Beauty mode
                modeAndLevel[0] = 1;
                break;
            case 0:
                //SkinCare mode
                modeAndLevel[0] = 2;
                break;
            case 1:
                //Slim mode
                modeAndLevel[0] = 3;
                break;
            case 2:
                //Ruddy mode
                modeAndLevel[0] = 4;
                break;
        }
        modeAndLevel[1] = mode;
        intent.putExtra("modeAndLevel", modeAndLevel);
        intent.setAction(BeautyFaceDevice2Controller.class.getName());
        mContext.sendBroadcast(intent);

    }
    //xapi add end
    public void removeView(ViewGroup rootView) {
        LogHelper.i(TAG, "rootView==" + rootView + "mSettingList==" + mSettingList);
        mRootView = rootView;
        if (null != mMainHandler) {
            mMainHandler.sendEmptyMessage(BEAUTY_FACE_VIEW_REMOVE);
        }
    }

    public void addView(ViewGroup rootView) {
        rootView.addView(mSettingList);
        // rootView.removeView(mAdjustCtrl);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        if (id == R.id.degree_view) {
            switcher(-1);
        }
    }

    /**
     * show the view. ~ add view to layout
     */
    public void addBeautyFaceView() {
        if (null != mMainHandler) {
            mMainHandler.sendEmptyMessage(BEAUTY_FACE_VIEW_ADD);
        }
    }

    /**
     * Handler let some tasks execute in main thread.
     */
    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BEAUTY_FACE_VIEW_ADD:
                    mLayout.addView(mSettingList, lp);
                    break;
                case BEAUTY_FACE_VIEW_REMOVE:
                    mRootView.removeView(mSettingList);
                    mRootView.removeView(mAdjustCtrl);
                    break;
                default:
                    break;
            }
        }
    }
}
