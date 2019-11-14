
package com.mediatek.camera.feature.mode.beautyface.jni;

import android.graphics.PointF;
import android.graphics.RectF;

public class JniStub {
    public static class Rect {
        public int top;
        public int left;
        public int bottom;
        public int right;

        public Rect() {
            top = 0;
            left = 0;
            bottom = 0;
            right = 0;
        }

        public Rect(int top, int left, int bottom, int right) {
            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
        }

        public int getTop() {
            return top;
        }

        public int getLeft() {
            return left;
        }

        public int getBottom() {
            return bottom;
        }

        public int getRight() {
            return right;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Rect top = ").append(top).append(", left = ").append(left)
                    .append(",bottom = ").append(bottom).append(",right = ").append(right);
            return sb.toString();
        }
    }

    public static class LandmarkInfo {
        public PointF mLeftEye;
        public PointF mRightEye;
        public PointF mLeftMouth;
        public PointF mRightMouth;
        public PointF mNose;
        public RectF mFaceRect;
        public float x_angle;
        public float y_angle;
        public float z_angle;

        public LandmarkInfo() {
            mLeftEye = new PointF();
            mRightEye = new PointF();
            mLeftMouth = new PointF();
            mRightEye = new PointF();
            mNose = new PointF();
            mRightMouth = new PointF();
            mFaceRect = new RectF();
        }
    }

    public static class MarkSimpleInfo {
        public int leftEye_x;
        public int leftEye_y;
        public int rightEye_x;
        public int rightEye_y;
        public int leftMouth_x;
        public int leftMouth_y;
        public int rightMouth_x;
        public int rightMouth_y;
        public int nose_x;
        public int nose_y;
        public int x_angle;
        public int y_angle;
        public int z_angle;

        public MarkSimpleInfo() {
            leftEye_x = 0;
            leftEye_y = 0;
            rightEye_x = 0;
            rightEye_y = 0;
            leftMouth_x = 0;
            leftMouth_y = 0;
            rightMouth_x = 0;
            rightMouth_y = 0;
            nose_x = 0;
            nose_y = 0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(" leftEye info x = ").append(leftEye_x).append(", y = ").append(leftEye_y)
                    .append(";").append(" rightEye info x = ").append(rightEye_x).append(", y = ")
                    .append(rightEye_y).append(";").append(" leftMouth info x = ")
                    .append(leftMouth_x).append(", y = ").append(leftMouth_y).append(";")
                    .append(" rightMouth info x = ").append(rightMouth_x).append(", y = ")
                    .append(rightMouth_y).append(";").append(" nose info x = ").append(nose_x)
                    .append(", y = ").append(nose_y).append(";").append(", x_angle = ").append(x_angle)
                    .append(";").append(",y_angle = ").append(y_angle).append(";").append(",z_angle = ")
                    .append(z_angle).append(";");
            return sb.toString();
        }

    }
}
