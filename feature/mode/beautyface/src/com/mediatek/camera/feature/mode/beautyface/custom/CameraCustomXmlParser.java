package com.mediatek.camera.feature.mode.beautyface.custom;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class CameraCustomXmlParser {
    public static final String TAG = "CameraCustomXmlParser";
    public static final String PHYSIOGNOMY_MODE_SUPPORT = "PHYSIOGNOMY_MODE_SUPPORT";
    public static String faceBeauty_grade_default;
    public static String camera_picturesize_back_default;
    public static String camera_picturesize_front_default;
    public static String camera_mode_back_default;
    public static String camera_mode_front_default;
    public static String camera_resource_center_default;
    //    public static String navigationbar_showed_default;
    public static String update_setting_default;
    public static String fake_focus_default;
    public static String camera_gestureshot_default;
    public static String camera_plugin_pose_show;
    public static String camera_plugin_child_show;
    public static String camera_plugin_watermark_show;
    public static String camera_plugin_filmmode_show;
    public static String camera_front_plugin_child_show;
    public static String camera_face_beauty_show;
    public static String camera_front_face_beauty_show;
    public static String sCameraLocationSwitch;
    public static String sCameraFlashStatus;
    public static String sCameraTorchStatus;
    public static String sCameraGuideSwitch;
    public static String sCameraBrightnessLevel;
    public static String sGalleryBrightnessLevel;
    public static String sCameraPhotoMirrordefault;
    public static String sGalleryZoomLeval;
    public static String sCameraZsdOnDefault;
    public static String sCameraPictureQuality;
    public static String sCameraContinueDefault;
    public static String sCameraContinueNumDefault;
    public static String sCameraBvModeSupport;
    //    public static String sCameraAntibandingDefault;
    public static String sCameraFocusSoundShow;
    public static String sCameraBackVideoSizeDefault;
    public static String sCameraFrontVideoSizeDefault;
    public static HashMap<String, String> sCameraCustomPicSizeMap = new HashMap<String, String>();
    public static HashMap<String, String> sCameraGenerParamMap = new HashMap<String, String>();
    public static final String FRONT_CAMERA_SUPPORT_BV = "FRONT_CAMERA_SUPPORT_BV";
    public static final String SDOF_LEVEL_DEFAULT = "SDOF_LEVEL_DEFAULT";
    public static final String CAMERA_FFB_SUPPORT = "CAMERA_FFB_SUPPORT";
    public static final String GOTO_FREEME_GALLERY = "GOTO_FREEME_GALLERY";
    public static final String SDOF_MODE_SUPPORT = "SDOF_MODE_SUPPORT";
    public static final String IKO_MODE_SUPPORT = "IKO_MODE_SUPPORT";
    public static final String IKO_USE_SPECIFIED_BROWSER_PKG = "IKO_USE_SPECIFIED_BROWSER_PKG";
    public static final String FRONT_CAMERA_PREVIEW_SIZE_CUSTOM = "FRONT_CAMERA_PREVIEW_SIZE_CUSTOM";
    public static final String BACK_CAMERA_PREVIEW_SIZE_CUSTOM = "BACK_CAMERA_PREVIEW_SIZE_CUSTOM";
    public static final String DNR_MODE_SUPPORT = "DNR_MODE_SUPPORT";

    static {
        CameraCustomXmlParser.parsexml();
    }

    public static void setFBGDValue(String value) {
        faceBeauty_grade_default = value;
    }

    public static void setCPBDValue(String value) {
        camera_picturesize_back_default = value;
    }

    public static void setCPFDValue(String value) {
        camera_picturesize_front_default = value;
    }

    public static void setCMBDValue(String value) {
        camera_mode_back_default = value;
    }

    public static void setCMFDValue(String value) {
        camera_mode_front_default = value;
    }

    public static void setCRCDValue(String value) {
        camera_resource_center_default = value;
    }

//    public static void setNavBarShowValue(String value) {
//        navigationbar_showed_default = value;
//    }

    public static void setUpdateSetValue(String value) {
        update_setting_default = value;
    }

    public static void setFakeFocusValue(String value) {
        fake_focus_default = value;
    }

    public static void setGestureShotValue(String value) {
        camera_gestureshot_default = value;
    }

    public static void setPluginPoseValue(String value) {
        camera_plugin_pose_show = value;
    }

    public static void setPluginChildValue(String value) {
        camera_plugin_child_show = value;
    }

    public static void setPluginWatermarkValue(String value) {
        camera_plugin_watermark_show = value;
    }

    public static void setPluginFilmmodeValue(String value) {
        camera_plugin_filmmode_show = value;
    }

    public static void setFrontPluginChildValue(String value) {
        camera_front_plugin_child_show = value;
    }

    private static void parsexml() {
        String name, value;
        FileReader reader = null;
        File file = new File("/system/etc/freemeCamConfig.xml");
        try {
            reader = new FileReader(file);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "Couldn\'t find or open xml file: " + file.getName());
            file = new File("/system/vendor/etc/freemeCamConfig.xml");
            try {
                reader = new FileReader(file);
            } catch (FileNotFoundException e1) {
                Log.i(TAG, "Couldn\'t find or open xml file: " + file.getName());
                return;
            }
        }

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(reader);
            for (int type = parser.getEventType(); type != XmlPullParser.END_DOCUMENT; type = parser
                    .next()) {
                switch (type) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    case XmlPullParser.START_TAG: {
                        if (parser.getName().equals("Param")) {
                            name = parser.getAttributeValue(0);
                            value = parser.getAttributeValue(1);
                            if (name.equals("FACE_BEAUTY_GRADE_DEFAULT")) {
                                setFBGDValue(value);
                            } else if (name.equals("CAMERA_PICTURESIZE_BACK_DEFAULT")) {
                                setCPBDValue(value);
                            } else if (name.equals("CAMERA_PICTURESIZE_FRONT_DEFAULT")) {
                                setCPFDValue(value);
                            } else if (name.equals("CAMERA_MODE_BACK_DEFAULT")) {
                                setCMBDValue(value);
                            } else if (name.equals("CAMERA_MODE_FRONT_DEFAULT")) {
                                setCMFDValue(value);
                            } else if (name.equals("CAMERA_RESOURCE_CENTER_DEFAULT")) {
                                setCRCDValue(value);
                            } else if (name.equals("UPDATE_SETTING_DEFAULT")) {
                                setUpdateSetValue(value);
                            } else if (name.equals("FAKE_FOCUS_DEFAULT")) {
                                setFakeFocusValue(value);
                            } else if (name.equals("CAMERA_GESTURESHOT_DEFAULT")) {
                                setGestureShotValue(value);
                            } else if (name.equals("CAMERA_PLUGIN_POSE_SHOW")) {
                                setPluginPoseValue(value);
                            } else if (name.equals("CAMERA_PLUGIN_CHILD_SHOW")) {
                                setPluginChildValue(value);
                            } else if (name.equals("CAMERA_PLUGIN_WATERMARK_SHOW")) {
                                setPluginWatermarkValue(value);
                            } else if (name.equals("CAMERA_PLUGIN_FILMMODE_SHOW")) {
                                setPluginFilmmodeValue(value);
                            } else if (name.equals("CAMERA_FRONT_PLUGIN_CHILD_SHOW")) {
                                setFrontPluginChildValue(value);
//                            } else if (name.equals("CAMERA_FACE_BEAUTY_SHOW")) {
//                                camera_face_beauty_show = value;
//                            } else if (name.equals("CAMERA_FRONT_FACE_BEAUTY_SHOW")) {
//                                camera_front_face_beauty_show = value;
                            } else if (name.equals("CAMERA_LOCATION_SWITCH")) {
                                sCameraLocationSwitch = value;
                            } else if (name.equals("CAMERA_FLASH_STASTUS")) {
                                sCameraFlashStatus = value;
                            } else if (name.equals("CAMERA_TORCH_STASTUS")) {
                                sCameraTorchStatus = value;
                            } else if (name.equals("CAMERA_GUIDE_SWITCH")) {
                                sCameraGuideSwitch = value;
                            } else if (name.equals("CAMERA_BRIGHTNESS_LEVEL")) {
                                sCameraBrightnessLevel = value;
                            } else if (name.equals("GALLERY_BRIGHTNESS_LEVEL")) {
                                sGalleryBrightnessLevel = value;
                            } else if (name.equals("CAMERA_PHOTO_MIRROR_DEFAULT")) {
                                sCameraPhotoMirrordefault = value;
                            } else if (name.equals("GALLERY_ZOOM_LEVEL")) {
                                sGalleryZoomLeval = value;
                            } else if (name.equals("CAMERA_ZSDON_DEFAULT")) {
                                sCameraZsdOnDefault = value;
                            } else if (name.equals("CAMERA_PICTURE_QUALITY")) {
                                sCameraPictureQuality = value;
                            } else if (name.equals("CAMERA_CONTINUE_DEFAULT")) {
                                sCameraContinueDefault = value;
                            } else if (name.equals("CAMERA_CONTINUE_NUM_DEFAULT")) {
                                sCameraContinueNumDefault = value;
                            } else if (name.equals("CAMERA_BACKGROUND_VIRTUAL_SUPPORT")) {
                                sCameraBvModeSupport = value;
                            } else if (name.equals("CAMERA_FOCUS_SOUND_SHOW")) {
                                sCameraFocusSoundShow = value;
                            } else if (name.equals("CAMERA_BACK_VIDEOSIZE_DEFAULT")) {
                                sCameraBackVideoSizeDefault = value;
                            } else if (name.equals("CAMERA_FRONT_VIDEOSIZE_DEFAULT")) {
                                sCameraFrontVideoSizeDefault = value;
                            }
                        } else if (parser.getName().equals("PictureSizeMap")) {
                            String key = parser.getAttributeValue(0);
                            String values = parser.getAttributeValue(1);
                            sCameraCustomPicSizeMap.put(key, values);
                        } else if (parser.getName().equals("GeneralParamMap")) {
                            String keys = parser.getAttributeValue(0);
                            String valuess = parser.getAttributeValue(1);
                            sCameraGenerParamMap.put(keys, valuess);
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            Log.i(TAG, "Got execption parsing permissions", e);
        } catch (XmlPullParserException e) {
            Log.i(TAG, "Got execption parsing permissions", e);
        }

        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
    }

}
