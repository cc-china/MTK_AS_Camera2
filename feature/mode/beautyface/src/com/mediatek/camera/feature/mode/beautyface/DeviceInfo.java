/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("mediatek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to mediatek Inc. and/or its licensors. Without
 *     the prior written permission of mediatek inc. and/or its licensors, any
 *     reproduction, modification, use or disclosure of mediatek Software, and
 *     information contained herein, in whole or in part, shall be strictly
 *     prohibited.
 *
 *     mediatek Inc. (C) 2017. All rights reserved.
 *
 *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("mediatek SOFTWARE")
 *     RECEIVED FROM mediatek AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *     ON AN "AS-IS" BASIS ONLY. mediatek EXPRESSLY DISCLAIMS ANY AND ALL
 *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *     NONINFRINGEMENT. NEITHER DOES mediatek PROVIDE ANY WARRANTY WHATSOEVER WITH
 *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE mediatek SOFTWARE, AND RECEIVER AGREES
 *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN mediatek
 *     SOFTWARE. mediatek SHALL ALSO NOT BE RESPONSIBLE FOR ANY mediatek SOFTWARE
 *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND mediatek'S
 *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE mediatek SOFTWARE
 *     RELEASED HEREUNDER WILL BE, AT mediatek'S OPTION, TO REVISE OR REPLACE THE
 *     mediatek SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *     CHARGE PAID BY RECEIVER TO mediatek FOR SUCH mediatek SOFTWARE AT ISSUE.
 *
 *     The following software/firmware and/or related documentation ("mediatek
 *     Software") have been modified by mediatek Inc. All revisions are subject to
 *     any receiver's applicable license agreements with mediatek Inc.
 */

package com.mediatek.camera.feature.mode.beautyface;

import com.mediatek.camera.common.setting.ISettingManager;

/**
 * When camera will be open,can be wrapped the parameters to the info.
 */
public class DeviceInfo {
    private ISettingManager mSettingManager;
    private String mCameraId;

    /**
     * Set the camera id.
     *
     * @param cameraId which camera need opened.
     */
    public void setCameraId(String cameraId) {
        mCameraId = cameraId;
    }

    /**
     * Add a setting manager to the info.
     *
     * @param settingManager current setting manager.
     */
    public void setSettingManager(ISettingManager settingManager) {
        mSettingManager = settingManager;
    }

    /**
     * Get the setting manager.
     *
     * @return current setting manager.
     */
    public ISettingManager getSettingManager() {
        return mSettingManager;
    }

    /**
     * Get the camera id from info.
     *
     * @return current camera id.
     */
    public String getCameraId() {
        return mCameraId;
    }

}
