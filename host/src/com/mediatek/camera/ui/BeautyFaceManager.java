/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */
package com.mediatek.camera.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.mediatek.camera.R;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.widget.RotateImageView;


import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
/**
 * Manager effect view entry.
 */

public class BeautyFaceManager extends AbstractViewManager {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(BeautyFaceManager.class.getSimpleName());
    
    private RotateImageView mBeautyFace;
    private View.OnClickListener mBeautyFaceClickedListener;

    /**
     * Effect view manager constructor.
     *
     * @param app        IApp app, ViewGroup parentView
     * @param parentView The root view of ui.
     */
     static int i =0;
    public BeautyFaceManager(IApp app, ViewGroup parentView) {
        super(app, parentView);
        getView();
        setImageViewParams();
    }

    @Override
    protected View getView() {
        mBeautyFace = (RotateImageView) mParentView
                .findViewById(R.id.face_beauty);
                LogHelper.e(TAG, "111111!!----getView");
        mBeautyFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				LogHelper.e(TAG, "111111!!----setOnClickListener");
                if (mBeautyFaceClickedListener != null) {
					LogHelper.e(TAG, "111111!!----!= null");
                    mBeautyFaceClickedListener.onClick(view);
                }
            }
        });
        return mBeautyFace;

    }
    private void setImageViewParams(){
        float density = mApp.getActivity().getResources().getDisplayMetrics().density;
            int widthPixels = mApp.getActivity().getResources().getDisplayMetrics().widthPixels;
            int lMarginPix = (int) (11 * density);
            int itemInPix = (int) (30 * density);
            int marginInPix = (int) ((widthPixels - 2 * lMarginPix - 4 * itemInPix) / 3);
            int topMarginInPix = (int) (3.5 * density);
            int bottomMarginInPix = (int) (3.5 * density);

            mBeautyFace.setImageResource(R.drawable.ic_freeme_beauty_face);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            params.setMargins(marginInPix, topMarginInPix, 0, bottomMarginInPix);
            mBeautyFace.setLayoutParams(params);
            mBeautyFace.setVisibility(View.GONE);
            
            LogHelper.e(TAG, "111111!!----BeautyFaceManager=="+ ++i);
    }
    
    public void setBeautyFaceClickedListener(View.OnClickListener listener) {
        mBeautyFaceClickedListener = listener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mBeautyFace != null) {
            //mBeautyFace.setClickable(enabled);
        }
    }
    
    public void setVisibilitys (int flag){
        if (mBeautyFace != null) {
            mBeautyFace.setVisibility(flag);
        }
    }
}
