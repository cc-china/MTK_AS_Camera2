
package com.mediatek.camera.feature.mode.beautyface.gdx.helper;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;

public class GraphicsImpl implements Graphics {
    private GL20Impl mGL20Impl;
    private int mWidth;
    private int mHeight;

    public GraphicsImpl(Context context) {
        mGL20Impl = new GL20Impl();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(displayMetrics);
        mWidth = displayMetrics.widthPixels;
        mHeight = displayMetrics.heightPixels;
    }

    public void resize(int w, int h) {
        mWidth = w;
        mHeight = h;
    }

    @Override
    public boolean isGL30Available() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public GL20 getGL20() {
        return mGL20Impl;
    }

    @Override
    public GL30 getGL30() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    @Override
    public long getFrameId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getDeltaTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getRawDeltaTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getFramesPerSecond() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public GraphicsType getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float getPpiX() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getPpiY() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getPpcX() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getPpcY() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getDensity() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean supportsDisplayModeChange() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public DisplayMode[] getDisplayModes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTitle(String title) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setVSync(boolean vsync) {
        // TODO Auto-generated method stub

    }

    @Override
    public BufferFormat getBufferFormat() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean supportsExtension(String extension) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setContinuousRendering(boolean isContinuous) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isContinuousRendering() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void requestRendering() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isFullscreen() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public DisplayMode getDesktopDisplayMode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean setDisplayMode(DisplayMode arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean setDisplayMode(int arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub
        return false;
    }

}
