/*
 * File name: FaceBeautyUtil.java
 * 
 * Description: TODO
 *
 * Author: Kimi Wu, contact with wuqizhi@droi.com
 * 
 * Date: Dec 23, 2015  5:24:12 PM
 * 
 * Copyright (C) 2015 Shanghai Droi Technology Co.,Ltd.
 * 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.camera.feature.mode.beautyface;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

//import com.mediatek.camera.custom.CameraCustomXmlParser;

public class FaceBeautyUtil {
    // grades
    public static final int NATURAL = 0;
    public static final int CLASSIC = 1;
    public static final int EXTREME = 2;
    public static final int CUSTOM = 3;
    // public static final int GRADES = 4;

    // types
    public static final int FB_GRADE = -1;
    public static final int SKIN_SMOOTHING = 0;
    public static final int SKIN_TONING = 0;
    public static final int FACE_SLIMING = 1;
    public static final int EYE_ENLARGE = 2;
    public static final int EYE_CICLES = 3;
    private static final int TYPES = 5;
    private static final int GRADE_INDEX = 3;
    private static int DEFAULT_GRADE = 1;

    // preference key
    private static final String[] sPrefKeys = new String[]{
//            "pref_key_skin_smoothing", // [0, 255]
            "pref_key_skin_toning", // [0, 255]
            "pref_key_face_sliming", // [0, 255]
            "pref_key_eye_enlarge", // [0, 255]
//            "pref_key_eye_circles", // [0, 255]
            "pref_key_fb_grade", // NATURAL, CLASSIC, EXTREME, CUSTOM
    };

    // level [0, 5]
    private static final int DEFAULT_LEVEL = 2;
    private static final int LEVELS = 6;
    private static final int[] sGradeLevelIndex = new int[]{
            0, // Natural
            2, // Classic
            4, // Extreme
    };

    // for preview
    private static final int[][] sVFBParamCfgTbl = new int[TYPES][];
    // for capture
    private static final int[][] sSFBParamCfgTbl = new int[TYPES][];

    // make VFB effect near to SFB effect at the most
    static {
        // level [0, 5]
        sVFBParamCfgTbl[SKIN_SMOOTHING] = new int[]{
                0, 30, 60, 90, 120, 150
        };
        sVFBParamCfgTbl[SKIN_TONING] = new int[]{
                0, 40, 70, 100, 130, 160
        };
        sVFBParamCfgTbl[FACE_SLIMING] = new int[]{
                0, 20, 40, 60, 80, 100
        };
        sVFBParamCfgTbl[EYE_ENLARGE] = new int[]{
                0, 30, 60, 90, 120, 150
        };
        sVFBParamCfgTbl[EYE_CICLES] = new int[]{
                20, 50, 90, 130, 180, 200
        };

        sSFBParamCfgTbl[SKIN_SMOOTHING] = new int[]{
                0, 30, 60, 90, 120, 150
        };
        sSFBParamCfgTbl[SKIN_TONING] = new int[]{
                0, 40, 70, 100, 130, 160
        };
        sSFBParamCfgTbl[FACE_SLIMING] = new int[]{
                0, 20, 40, 60, 80, 100
        };
        sSFBParamCfgTbl[EYE_ENLARGE] = new int[]{
                0, 30, 60, 90, 120, 150
        };
        sSFBParamCfgTbl[EYE_CICLES] = new int[]{
                20, 50, 90, 130, 180, 200
        };
    }

    private static final String NAME = "fb_fb_config_level";
    private static SharedPreferences mSharedPreferences;

    public static void init(Context context) {
        /*if (CameraCustomXmlParser.faceBeauty_grade_default != null) {
            DEFAULT_GRADE = Integer.parseInt(CameraCustomXmlParser.faceBeauty_grade_default);
        }*/
        mSharedPreferences = context.getSharedPreferences(NAME, Activity.MODE_PRIVATE);
        Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < sPrefKeys.length; i++) {
            if (i == GRADE_INDEX) {
                editor.putInt(sPrefKeys[i], DEFAULT_GRADE);
                break;
            }

            editor.putInt(sPrefKeys[i], DEFAULT_LEVEL);
        }

        editor.commit();
    }

    public static void release() {
        mSharedPreferences = null;
    }

    /**
     * @Description: Get real VFB parameter value
     * @param: type include SKIN_SMOOTHING, SKIN_TONING, FACE_SLIMING,
     * EYE_ENLARGE, EYE_CICLES
     * @return: parameter value, [0, 255]
     */
    public static int getVfbParamsLevel(int type) {
        if (mSharedPreferences == null) {
            return 0;
        }
        int level = mSharedPreferences.getInt(getKey(type), DEFAULT_LEVEL);
        if (type < 0 || type >= TYPES || level < 0 || level >= LEVELS) {
            throw new IllegalArgumentException("getVfbParamsLevel(): Illegal type and level value!");
        }

        return sVFBParamCfgTbl[type][level];
    }

    /**
     * @Description: Get real SFB parameter value
     * @param: type include SKIN_SMOOTHING, SKIN_TONING, FACE_SLIMING,
     * EYE_ENLARGE, EYE_CICLES
     * @return: parameter value, [0, 255]
     */
    public static int getSfbParamsLevel(int type) {
        if (mSharedPreferences == null) {
            return 0;
        }
        int level = mSharedPreferences.getInt(getKey(type), DEFAULT_LEVEL);
        if (type < 0 || type >= TYPES || level < 0 || level >= LEVELS) {
            throw new IllegalArgumentException("getSfbParamsLevel(): Illegal type and level value!");
        }

        return sSFBParamCfgTbl[type][level];
    }

    /**
     * @param type, which parameter
     * @Description: save level index of face beauty parameters
     * @param: level, level of types and grade level that save
     */
    public static void saveLevelIndex(int type, int level) {
        if (mSharedPreferences == null) {
            return;
        }
        Editor editor = mSharedPreferences.edit();

        if (type == FB_GRADE) {
            editor.putInt(getKey(type), level);
            if (level < CUSTOM) {
                for (int i = 0; i < sPrefKeys.length - 1; i++) {
                    editor.putInt(getKey(i), getGradeLevelIndex(level));
                }
            }
        } else {
            editor.putInt(getKey(type), level);
        }

        editor.commit();
    }

    /**
     * @param type, which parameter
     * @Description: get level index of face beauty parameters
     */
    public static int getLevelIndex(int type) {
        int result = 0;
        if (mSharedPreferences == null) {
            return result;
        }
        if (type == FB_GRADE) {
            result = mSharedPreferences.getInt(getKey(type), DEFAULT_GRADE);
        } else {
            result = mSharedPreferences.getInt(getKey(type), DEFAULT_LEVEL);
        }

        return result;
    }

    /**
     * @Description: Get preference key
     * @param: index, level of types and grade level that save
     */
    private static String getKey(int index) {
        if (index < 0 || index > sPrefKeys.length - 1) {
            index = sPrefKeys.length - 1;
        }

        return sPrefKeys[index];
    }

    /**
     * @Description: Get level index of grade, e.s., the classic level is 2, 2,
     * 2, 2, 2
     * @param: grade
     */
    private static int getGradeLevelIndex(int grade) {
        if (grade < 0 || grade >= CUSTOM) {
            throw new IllegalArgumentException("getGradeLevelIndex(): this grade not support!");
        }

        return sGradeLevelIndex[grade];
    }
}
