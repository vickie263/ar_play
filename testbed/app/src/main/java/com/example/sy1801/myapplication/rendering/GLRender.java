package com.example.sy1801.myapplication.rendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

public class GLRender{
    private static final String TAG = GLRender.class.getSimpleName();
    // Shader names.
    private static final String VERTEX_SHADER_NAME = "shaders/object.vert";
    private static final String FRAGMENT_SHADER_NAME = "shaders/object.frag";
    private static final int FLOAT_SIZE = 4;
    private static final int SHORT_SIZE = 2;
    private static final int COORDS_PER_VERTEX = 3;
    private static final float TINT_INTENSITY = 0.1f;
    private static final float TINT_ALPHA = 1.0f;
    private static final int[] TINT_COLORS_HEX = {
            0x000000, 0xF44336, 0xE91E63, 0x9C27B0, 0x673AB7, 0x3F51B5, 0x2196F3, 0x03A9F4, 0x00BCD4,
            0x009688, 0x4CAF50, 0x8BC34A, 0xCDDC39, 0xFFEB3B, 0xFFC107, 0xFF9800,
    };
    // Set some default material properties to use for lighting.
    private float ambient = 0.3f;
    private float diffuse = 1.0f;
    private float specular = 1.0f;
    private float specularPower = 6.0f;

    private int textureId = -1;
    private SurfaceTexture surfaceTexture;
    private MediaPlayer mediaPlayer;
    private Context context;
    private int aPositionHandle;
    // Shader location: model view projection matrix.
    private int modelViewUniform;
    private int modelViewProjectionUniform;
    // Shader location: environment properties.
    private int lightingParametersUniform;

    // Shader location: material properties.
    private int materialParametersUniform;
    // Shader location: color correction property
    private int colorCorrectionParameterUniform;

    // Shader location: color tinting
    private int colorTintParameterUniform;
    // Shader location: texture sampler.
    private int textureUniform;
    // Shader location: SurfaceTexture.getTransformMatrix.
    private int texMatrixHandle;

    private int programId;
    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordsBuffer;
    private ShortBuffer indexBuffer;

    // Temporary matrices allocated here to reduce number of allocations for each frame.
    private final float[] modelMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private final float[] viewLightDirection = new float[4];
    // Note: the last component must be zero to avoid applying the translational part of the matrix.
    private static final float[] LIGHT_DIRECTION = new float[] {0.250f, 0.866f, 0.433f, 0.0f};
    private final int[] textures = new int[1];
    private int vertexBufferId;
    private int indexBufferId;
    private int program;

    // Shader location: object attributes.
    private int positionAttribute;
    private int texCoordAttribute;
    private int verticesBaseAddress;
    private int texCoordsBaseAddress;
    private int indexCount;
//    private Context mContext;

//    private final float[] vertexData = {
//            // positions
//            0.5f,  0.5f, 0.0f,
//            0.5f, -0.5f, 0.0f,
//            -0.5f, -0.5f, 0.0f,
//            -0.5f,  0.5f, 0.0f,
//    };
private final float[] vertexData = {
        // positions
        0.1f,  0.0f, 0.1f,
        0.1f, 0.0f, -0.1f,
        -0.1f, 0.0f, -0.1f,
        -0.1f,  0.0f, 0.1f,
};

    private final float[] textCoordsData = {
            // texture coords
            1.0f, 1.0f, // top right
            1.0f, 0.0f, // bottom right
            0.0f, 0.0f, // bottom left
            -0.0f, 1.0f  // top left
    };

    private final short[] indexData = {
            0,1,3,
            1,2,3,
    };

    public GLRender() {

    }

    public int createOnGlThread(Context context) throws IOException//, String objAssetName, String diffuseTextureAssetName)
    {
        //将textureBitmap绑定到textures[0]上
//        Bitmap textureBitmap =
//                BitmapFactory.decodeStream(context.getAssets().open("models/frame_base.png"));

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(textures.length, textures, 0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
//
//        GLES20.glTexParameteri(
//                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
//        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
//        textureBitmap.recycle();

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textures[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);//设置MIN 采样方式
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);//设置MAG采样方式
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);//设置S轴拉伸方式
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);//设置T轴拉伸方式
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        ShaderUtil.checkGLError(TAG, "Texture loading");

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * FLOAT_SIZE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);
        texCoordsBuffer = ByteBuffer.allocateDirect(textCoordsData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textCoordsData);
        texCoordsBuffer.position(0);
        indexBuffer = ByteBuffer.allocateDirect(indexData.length * SHORT_SIZE)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indexData);
        indexBuffer.position(0);
        indexCount = indexBuffer.limit();
        //将顶点数据绑定到vertexBufferId上
        int[] buffers = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);
        vertexBufferId = buffers[0];
        indexBufferId = buffers[1];
        verticesBaseAddress = 0;
        texCoordsBaseAddress = verticesBaseAddress + 4 * vertexBuffer.limit();
        final int totalBytes = texCoordsBaseAddress + 4 * texCoordsBuffer.limit();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, totalBytes, null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER, verticesBaseAddress, 4 * vertexBuffer.limit(), vertexBuffer);
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER, texCoordsBaseAddress, 4 * texCoordsBuffer.limit(), texCoordsBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //indexBufferId
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, 2*indexBuffer.limit(), indexBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        ShaderUtil.checkGLError(TAG, "glimage: OBJ buffer load");

        //load shaders
        final int vertexShader =
                ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
        final int fragmentShader =
                ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);
        //create program
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);

        ShaderUtil.checkGLError(TAG, "glimage: Program creation");

        modelViewUniform = GLES20.glGetUniformLocation(program, "u_ModelView");
        modelViewProjectionUniform = GLES20.glGetUniformLocation(program, "u_ModelViewProjection");
        positionAttribute = GLES20.glGetAttribLocation(program, "a_Position");
        texCoordAttribute = GLES20.glGetAttribLocation(program, "a_TexCoord");
        textureUniform = GLES20.glGetUniformLocation(program, "u_Texture");
        lightingParametersUniform = GLES20.glGetUniformLocation(program, "u_LightingParameters");
        materialParametersUniform = GLES20.glGetUniformLocation(program, "u_MaterialParameters");
        colorCorrectionParameterUniform =
                GLES20.glGetUniformLocation(program, "u_ColorCorrectionParameters");
        colorTintParameterUniform = GLES20.glGetUniformLocation(program, "u_ColorTintParameters");
        texMatrixHandle = GLES20.glGetUniformLocation(program,"uTexMatrix");
        ShaderUtil.checkGLError(TAG, "glimage: Program parameters");

        //init modelmatrix = 0
        Matrix.setIdentityM(modelMatrix, 0);
        setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);
//        GLES20.glUniform1i(textureUniform, 0);
        return textures[0];
    }

    public void draw(float[] cameraView,
                     float[] cameraPerspective,
                     AugmentedImage augmentedImage,
                     Anchor centerAnchor,
                     float[] colorCorrectionRgba,
                     float[] transformMatrix) {

        Pose anchorPose = centerAnchor.getPose();
        float scaleFactor = augmentedImage.getExtentX();
        Log.d("glrender","augmentedImage.getExtentX() ="+augmentedImage.getExtentX());
        float[] modelMatrix = new float[16];
        anchorPose.toMatrix(modelMatrix, 0);
//        worldBoundaryPoses[0].toMatrix(modelMatrix, 0);
        updateModelMatrix(modelMatrix, scaleFactor);

        ShaderUtil.checkGLError(TAG, "glimage: Before draw");

        //modelmatrix,projectionmatrix 左乘
        Matrix.multiplyMM(modelViewMatrix, 0, cameraView, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, cameraPerspective, 0, modelViewMatrix, 0);

        GLES20.glUseProgram(program);
        // Set the lighting environment properties.
        Matrix.multiplyMV(viewLightDirection, 0, modelViewMatrix, 0, LIGHT_DIRECTION, 0);
        normalizeVec3(viewLightDirection);
        GLES20.glUniform4f(
                lightingParametersUniform,
                viewLightDirection[0],
                viewLightDirection[1],
                viewLightDirection[2],
                1.f);

        GLES20.glUniform4f(
                colorCorrectionParameterUniform,
                colorCorrectionRgba[0],
                colorCorrectionRgba[1],
                colorCorrectionRgba[2],
                colorCorrectionRgba[3]);
        float[] tintColor =
                convertHexToColor(TINT_COLORS_HEX[augmentedImage.getIndex() % TINT_COLORS_HEX.length]);
        GLES20.glUniform4f(
                colorTintParameterUniform,
                tintColor[0],
                tintColor[1],
                tintColor[2],
                tintColor[3]);

        if(transformMatrix!=null){
            GLES20.glUniformMatrix4fv(texMatrixHandle,1,false,transformMatrix,0);
        }
        // Set the object material properties.
        GLES20.glUniform4f(materialParametersUniform, ambient, diffuse, specular, specularPower);
                // Attach the object texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glUniform1i(textureUniform, 0);

                //将顶点数据写进buffer
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId);
        GLES20.glVertexAttribPointer(
                positionAttribute, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, verticesBaseAddress);

        GLES20.glVertexAttribPointer(
                texCoordAttribute, 2, GLES20.GL_FLOAT, false, 0, texCoordsBaseAddress);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

                // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(modelViewUniform, 1, false, modelViewMatrix, 0);
        GLES20.glUniformMatrix4fv(modelViewProjectionUniform, 1, false, modelViewProjectionMatrix, 0);
        GLES20.glUniform4f(GLES20.glGetUniformLocation(program, "ourColor"), 0.0f, 0.5f, 0.0f, 1.0f);

        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glEnableVertexAttribArray(texCoordAttribute);

//               ShaderUtil.checkGLError(TAG, "begin glDrawElements");
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.limit(), GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionAttribute);
        GLES20.glDisableVertexAttribArray(texCoordAttribute);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);



    }

    private static void normalizeVec3(float[] v) {
        float reciprocalLength = 1.0f / (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] *= reciprocalLength;
        v[1] *= reciprocalLength;
        v[2] *= reciprocalLength;
    }

    /**
     * Updates the object model matrix and applies scaling.
     *
     * @param modelMatrix A 4x4 model-to-world transformation matrix, stored in column-major order.
     * @param scaleFactor A separate scaling factor to apply before the {@code modelMatrix}.
     * @see android.opengl.Matrix
     */
    public void updateModelMatrix(float[] modelMatrix, float scaleFactor) {
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        scaleMatrix[0] = scaleFactor;
        scaleMatrix[5] = scaleFactor;
        scaleMatrix[10] = scaleFactor;
        Matrix.multiplyMM(this.modelMatrix, 0, modelMatrix, 0, scaleMatrix, 0);
    }

    private static float[] convertHexToColor(int colorHex) {
        // colorHex is in 0xRRGGBB format
        float red = ((colorHex & 0xFF0000) >> 16) / 255.0f * TINT_INTENSITY;
        float green = ((colorHex & 0x00FF00) >> 8) / 255.0f * TINT_INTENSITY;
        float blue = (colorHex & 0x0000FF) / 255.0f * TINT_INTENSITY;
        return new float[] {red, green, blue, TINT_ALPHA};
    }

    /**
     * Sets the surface characteristics of the rendered model.
     *
     * @param ambient Intensity of non-directional surface illumination.
     * @param diffuse Diffuse (matte) surface reflectivity.
     * @param specular Specular (shiny) surface reflectivity.
     * @param specularPower Surface shininess. Larger values result in a smaller, sharper specular
     *     highlight.
     */
    public void setMaterialProperties(
            float ambient, float diffuse, float specular, float specularPower) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.specularPower = specularPower;
    }

//    @Override
//    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
//        try {
//            createOnGlThread(mContext);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {

    }

//    @Override
    public void onDrawFrame(GL10 gl10) {
//        draw(null);
    }
}
