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

import android.app.Activity;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.device.CameraDeviceManagerFactory.CameraApi;

import javax.annotation.Nonnull;

/**
 * A factory used to create the instance of {@link IBeautyFaceDeviceController}.
 */
public  class DeviceControllerFactory {
    /**
     * Create the instance of {@link IBeautyFaceDeviceController} by API type.
     *
     * @param activity  the camera activity.
     * @param cameraApi the type of IDeviceController.
     * @param context   the camera context.
     * @return an instance of IDeviceController.
     */
    @Nonnull
    public IBeautyFaceDeviceController createDeviceController(
            @Nonnull Activity activity,
            @Nonnull CameraApi cameraApi,
            @Nonnull ICameraContext context) {
        if (CameraApi.API1 == cameraApi) {
            return new BeautyFaceDeviceController(activity, context);
        }
        return new BeautyFaceDevice2Controller(activity, context);
    }
}
