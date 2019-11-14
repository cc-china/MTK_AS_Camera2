
package com.mediatek.camera.feature.mode.beautyface;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.util.Log;

import com.mediatek.camera.R;

import java.util.ArrayList;
import java.util.List;

//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;

public class SettingAdjustView extends LinearLayout implements SeekBar.OnSeekBarChangeListener,
        VerticalSeekBar.VerticalSeekBarListener {
    private Context mContext;
    private LinearLayout mTextRoot;
    private SeekBar mSeekBar;
    private int mType;
    public final static int TYPE_SHOW_TITLES = 0;
    public final static int TYPE_SHOW_LABEL = 1;
    private static final int SLICE_MAX = 10;
    private List<String> mTitles = new ArrayList<String>();
    private int mCurrentSlicePos;
    private ListPreference mPref;
    private OnPickerListener mListener;
    private int mCurrProgress;
    private TextView mLabelView;
    private final static int FACTOR = 10;
    private final static int SHOW_TITLE_ITEM_MAX = 10;
    private View mCurrTitleView;
    private final static int MSG_DELAY_HIDE = 0;
    private final static int DELAY_HIDE_MS = 1000;
    private int mSliceWidth;
    private int mSliceLeftMargin;
    private int mItemContainerWidth;
    //SharedPreferences sharedPreferences;
    private int mTextSize;
    private int mindex = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DELAY_HIDE:
                    hide();
                    break;
                default:
                    break;
            }
        }
    };

    public void hide() {
        setVisibility(View.GONE);
    }

    public SettingAdjustView(Context context, int mCurrentItem, OnPickerListener listener) {
        super(context);
        mContext = context;
        mListener = listener;
        //sharedPreferences = context.getSharedPreferences("face", Activity.MODE_PRIVATE);
        mCurrentSlicePos = mCurrentItem;
        inflateView();
        initSlice(mCurrentItem);
    }

    private void inflateView() {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.setting_adjust_view_container, this);
        mTextRoot = (LinearLayout) view.findViewById(R.id.textRoot);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        mSeekBar.setThumb(getResources().getDrawable(R.drawable.setting_arc_slider_facebeauty));
        if (mSeekBar instanceof VerticalSeekBar) {
            VerticalSeekBar bar = (VerticalSeekBar) mSeekBar;
            bar.setListener(this);
        }
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    private void inflateTextRoot() {
        int orientation = getResources().getConfiguration().orientation;
        boolean isLandscape = false;
        if (Configuration.ORIENTATION_LANDSCAPE == orientation) {
            isLandscape = true;
        }
        switch (mType) {
            case TYPE_SHOW_TITLES:
                int index = -1;
                for (String title : mTitles) {
                    TextView v = new TextView(mContext);
                    LayoutParams params = null;
                    if (isLandscape) {
                        params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                                mSliceWidth);
                        /*if (index != -1) {
                            params.topMargin = mSliceLeftMargin;
                        } else {
                            params.topMargin = 0;
                        }*/
                        params.weight = 1;
                    } else {
                        params = new LayoutParams(mSliceWidth,
                                LayoutParams.WRAP_CONTENT);
                        /*if (index != -1) {
                            params.leftMargin = mSliceLeftMargin;
                        } else {
                            params.leftMargin = 0;
                        }*/
                        params.weight = 1;
                    }

                    v.setGravity(Gravity.CENTER);
                    v.setTextColor(Color.WHITE);
                    v.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
                    v.setText(title);
                    if (mCurrentSlicePos == index) {
                        v.setSelected(true);
                        mCurrTitleView = v;
                    }
                    mTextRoot.addView(v, params);
                    index++;
                    v.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            mCurrentSlicePos = mTextRoot.indexOfChild(arg0);
                        /*/ Modified by Droi Kimi Wu on 20151225 for optimize FN-FB parameters process
						Editor editor = sharedPreferences.edit();
						if (mindex == -1) {
							editor.putInt(String.valueOf(mindex), mCurrentSlicePos);
							for (int i = 0; i < 5; i++) {
								if (mCurrentSlicePos != 3) {
									editor.putInt(String.valueOf(i),
											getMode(i, mCurrentSlicePos));
									editor.commit();
								}
							}
						} else {
							editor.putInt(String.valueOf(mindex), mCurrentSlicePos);
							editor.commit();
						}
						//*/
                            FaceBeautyUtil.saveLevelIndex(mindex, mCurrentSlicePos);
                            //*/
                            setSeekBar(mCurrentSlicePos);
                            Log.i("front------------onTap", "front------------onTap");
                            onTap();
                            Log.i("before------------onTap", "before------------onTap");
                        }
                    });
                }

                break;
            case TYPE_SHOW_LABEL:
                mLabelView = new TextView(mContext);
                LayoutParams params = new LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                mLabelView.setGravity(Gravity.CENTER);
                String text = mTitles.get(mCurrentSlicePos);
                mLabelView.setText(text);
                mLabelView.setFocusable(false);
                mTextRoot.addView(mLabelView, params);
                break;
            default:
                break;
        }
    }

    public void initSlice(int index) {
        mTitles.clear();
        mindex = index;
        mTextRoot.removeAllViews();
        int mImg = 0;
        mSeekBar.setThumb(getResources().getDrawable(R.drawable.setting_arc_slider_facebeauty));
        CharSequence[] titles = {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
        };
        CharSequence[] titles1 = {
//                mContext.getResources().getString(R.string.pref_facebeauty_level_natural),
//                mContext.getResources().getString(R.string.pref_facebeauty_level_classic),
//                mContext.getResources().getString(R.string.pref_facebeauty_level_extreme),
//                mContext.getResources().getString(R.string.pref_facebeauty_level_custom),
                "a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h",
                "i",
                "j"
        };
        if (titles.length > SLICE_MAX) {
            throw new IllegalStateException("the item count can not over max (" + SLICE_MAX + ")!");
        }
        android.util.Log.i("sun", "mCurrentSlicePos==" + index);
        if (index == -1) {
            if (mSeekBar instanceof VerticalSeekBar) {
                for (int i = titles1.length - 1; i >= 0; i--) {
                    String str = titles1[i].toString();
                    mTitles.add(str);
                }
            } else {
                for (int i = 0; i < titles1.length; i++) {
                    String str = titles1[i].toString();
                    mTitles.add(str);
                }
            }
        } else {
            if (mSeekBar instanceof VerticalSeekBar) {
                for (int i = titles.length - 1; i >= 0; i--) {
                    String str = titles[i].toString();
                    mTitles.add(str);
                }
            } else {
                for (int i = 0; i < titles.length; i++) {
                    String str = titles[i].toString();
                    mTitles.add(str);
                }
            }
        }
        if (mTitles.size() > SHOW_TITLE_ITEM_MAX) {
            mType = TYPE_SHOW_LABEL;
        } else {
            mType = TYPE_SHOW_TITLES;
        }
        mSliceWidth = mContext.getResources().getDimensionPixelSize(
                R.dimen.setting_adjust_view_slice_width);
        mTextSize = mContext.getResources().getDimensionPixelSize(R.dimen.setting_adust_text_size);
        mItemContainerWidth = mContext.getResources().getDimensionPixelSize(
                R.dimen.setting_adjust_view_item_container_width);
        mSliceLeftMargin = (mItemContainerWidth - mSliceWidth * mTitles.size())
                / (mTitles.size() - 1);
        inflateTextRoot();
        if (mTitles.size() < 2) {
            throw new IllegalArgumentException("error size < 2");
        }
        int max = FACTOR * (mTitles.size() - 1);
        mSeekBar.setMax(max);
		/*/ Modified by Droi Kimi Wu on 20151225 for optimize FN-FB parameters process
		setSeekBar(sharedPreferences.getInt(String.valueOf(mindex), 0));
		//*/
        setSeekBar(FaceBeautyUtil.getLevelIndex(mindex));
        //*/
    }

    private boolean onTap() {
        boolean result = false;
        int newIdx = mCurrentSlicePos;
        int originIdx = 2;

        if (mListener != null) {
            mListener.func(mCurrentSlicePos);
        }
        result = true;
        return result;
    }

    private boolean isForceTap() {
        boolean force = false;
        int newIdx = mCurrentSlicePos;
        return force;
    }

    private int findSlicePos() {
        int value = 0;
        int index = mCurrProgress / FACTOR;
        int mod = mCurrProgress % FACTOR;
        if (mod <= (FACTOR / 2)) {
            value = index;
        } else {
            value = index + 1;
        }
        return value;
    }

    private void setSeekBar(int index) {
        int progress = index * FACTOR;
        mSeekBar.setProgress(progress);
    }

    private void handleCurrTitle(int index) {
        switch (mType) {
            case TYPE_SHOW_TITLES:
                if (index == mCurrentSlicePos || index >= mTextRoot.getChildCount()) {
                    return;
                }
                View v = mTextRoot.getChildAt(index);
                v.setSelected(true);
                mCurrTitleView.setSelected(false);
                mCurrTitleView = v;
                break;
            case TYPE_SHOW_LABEL:
                if (index == mCurrentSlicePos || index >= mTitles.size() || index < 0) {
                    return;
                }
                String text = mTitles.get(index);
                mLabelView.setText(text);
                break;
            default:
                break;
        }
    }

    public void setOrientation(int orientation, boolean isAnimate) {

    }

    public void show(boolean isAnimate) {
        setVisibility(View.VISIBLE);
    }

    public void hide(boolean isAnimate) {

        mHandler.removeMessages(MSG_DELAY_HIDE);
        mHandler.sendEmptyMessageDelayed(MSG_DELAY_HIDE, DELAY_HIDE_MS);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // mHandler.removeMessages(MSG_DELAY_HIDE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mCurrProgress = progress;
        handleCurrTitle(findSlicePos());
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mCurrentSlicePos = findSlicePos();

        android.util.Log.i("sun", "mCurrentSlicePos==" + mCurrentSlicePos);
		/*/ Modified by Droi Kimi Wu on 20151225 for optimize FN-FB parameters process
		Editor editor = sharedPreferences.edit();
		if (mindex == -1) {
			editor.putInt(String.valueOf(mindex), mCurrentSlicePos);
			for (int i = 0; i < 5; i++) {
				if (mCurrentSlicePos != 3) {
					editor.putInt(String.valueOf(i),
							getMode(i, mCurrentSlicePos));
					editor.commit();
				}
			}
		} else {
			editor.putInt(String.valueOf(mindex), mCurrentSlicePos);
			editor.commit();
		}
		//*/
        FaceBeautyUtil.saveLevelIndex(mindex, mCurrentSlicePos);
        //*/
        setSeekBar(mCurrentSlicePos);
        onTap();
    }

    @Override
    public void onVerticalSeekBarStopTrackingTouch(VerticalSeekBar seekBar) {
        mCurrentSlicePos = findSlicePos();
        int progress = mCurrentSlicePos * FACTOR;
        seekBar.setProgress(progress);
        seekBar.onSizeChanged(seekBar.getWidth(), seekBar.getHeight(), 0, 0);
        onTap();
    }

    public interface OnPickerListener {
        public void func(int mode);
    }

    public int getMode(int mode, int level) {
        String modeStr = mContext.getResources().getString(R.string.pref_facebeauty_level_natural);
        int reLevel = 0;
        switch (level) {
            case 0:
                modeStr = mContext.getResources().getString(R.string.pref_facebeauty_level_natural);
                switch (mode) {
                    case 0:
                        reLevel = 1;
                        break;
                    case 1:
                        reLevel = 1;
                        break;
                    case 2:
                        reLevel = 0;
                        break;
                    case 3:
                        reLevel = 0;
                        break;
                    case 4:
                        reLevel = 1;
                        break;
                }
                break;
            case 1:
                modeStr = mContext.getResources().getString(R.string.pref_facebeauty_level_classic);
                switch (mode) {
                    case 0:
                        reLevel = 2;
                        break;
                    case 1:
                        reLevel = 1;
                        break;
                    case 2:
                        reLevel = 2;
                        break;
                    case 3:
                        reLevel = 2;
                        break;
                    case 4:
                        reLevel = 2;
                        break;
                }
                break;
            case 2:
                modeStr = mContext.getResources().getString(R.string.pref_facebeauty_level_extreme);
                switch (mode) {
                    case 0:
                        reLevel = 3;
                        break;
                    case 1:
                        reLevel = 3;
                        break;
                    case 2:
                        reLevel = 4;
                        break;
                    case 3:
                        reLevel = 4;
                        break;
                    case 4:
                        reLevel = 3;
                        break;
                }
                break;
            case 3:
                modeStr = mContext.getResources().getString(R.string.pref_facebeauty_level_custom);
                // reLevel=0;
                break;
        }
        return reLevel;
    }
}
