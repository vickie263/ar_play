package com.example.sy1801.myapplication;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLUtils;


import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public class EGLHelper {
    protected final SurfaceTexture surfaceTexture;

    private EGL10 egl;
    private EGLContext eglContext;
    private EGLDisplay eglDisplay;
    private EGLSurface eglSurface;
    public EGLHelper(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
    }

    public void initEGL() {
        egl = (EGL10) EGLContext.getEGL();
        //获取显示设备
        eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        //version中存放EGL 版本号，int[0]为主版本号，int[1]为子版本号
        int version[] = new int[2];
        egl.eglInitialize(eglDisplay, version);

        EGLConfig eglConfig = chooseEglConfig();
        //创建EGL 的window surface 并且返回它的handles(eslSurface)
        eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, null);

        eglContext = createContext(egl, eglDisplay, eglConfig);

        /**绑定context到当前渲染线程并且去绘制（通过opengl去绘制）和读取surface（通过eglSwapBuffers（EGLDisplay dpy, EGLContext ctx）来显示）*/
        try {
            if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
                throw new RuntimeException("GL error:" + GLUtils.getEGLErrorString(egl.eglGetError()));
            }
            if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                throw new RuntimeException("GL Make current Error" + GLUtils.getEGLErrorString(egl.eglGetError()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deinitEGL() {
        egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(eglDisplay, eglSurface);
        egl.eglDestroyContext(eglDisplay, eglContext);
        egl.eglTerminate(eglDisplay);
    }

    public void swap()
    {
        egl.eglSwapBuffers(eglDisplay, eglSurface);
    }

    /**
     * 为当前渲染的API创建一个渲染上下文
     * @return a handle to the context
     */
    private EGLContext createContext(EGL10 egl, EGLDisplay eglDisplay, EGLConfig eglConfig) {
        int[] attrs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };
        return egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrs);
    }

    /***
     *  选择一个你所期望的配置.
     * @return 一个与你所指定最相近的一个EGL 帧缓存配置.
     */
    private EGLConfig chooseEglConfig() {
        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] attributes = getAttributes();
        int confSize = 1;

        if (!egl.eglChooseConfig(eglDisplay, attributes, configs, confSize, configsCount)) {    //获取满足attributes的config个数
            throw new IllegalArgumentException("Failed to choose config:"+ GLUtils.getEGLErrorString(egl.eglGetError()));
        }
        else if (configsCount[0] > 0) {
            return configs[0];
        }

        return null;
    }

    /**
     * 构造你期望的绘制时所需要的特性配置,如ARGB,DEPTH...
     */
    private int[] getAttributes()
    {
        return new int[] {
                EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,  //指定渲染api类别
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE      //总是以EGL10.EGL_NONE结尾
        };
    }
}
