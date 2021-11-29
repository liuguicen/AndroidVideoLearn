package com.example.androidvideolearn;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OneGlSurfaceView extends GLSurfaceView {
    private final OneGlRenderer mRenderer;

    public OneGlSurfaceView(Context context) {
        super(context);
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new OneGlRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
    }
}

class OneGlRenderer implements GLSurfaceView.Renderer {

    private Point point;

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        // 这里初始化
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        // triangle = new Triangle();
        // square = new Square();
        point = new Point();
    }


    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // triangle.draw();
        // square.draw();
        point.draw();
    }


    public void onSurfaceChanged(GL10 unused, int width, int height) {
        //
        GLES20.glViewport(0, 0, width, height);
        point.onSurfaceChanged(unused, width, height);
    }

    public static int loadShader(int type, String shaderCode) {

        // 创造顶点着色器类型(GLES20.GL_VERTEX_SHADER)
        // 或者是片段着色器类型 (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        // 添加上面编写的着色器代码并编译它
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}

class Point {
    /**
     * 顶点着色器
     */
    private static final String VERTEX_SHADER = "" +
            // mat4：4×4的矩阵
            "uniform mat4 u_Matrix;\n" +
            // vec4：4个分量的向量：x、y、z、w
            "attribute vec4 a_Position;\n" +
            "void main()\n" +
            "{\n" +
            // gl_Position：GL中默认定义的输出变量，决定了当前顶点的最终位置
            "    gl_Position = u_Matrix * a_Position;\n" +
            // gl_PointSize：GL中默认定义的输出变量，决定了当前顶点的大小
            "    gl_PointSize = 40.0;\n" +
            "}";

    /**
     * /**
     * 片段着色器
     */
    private static final String FRAGMENT_SHADER = "" +
            // 定义所有浮点数据类型的默认精度；有lowp、mediump、highp 三种，但只有部分硬件支持片段着色器使用highp。(顶点着色器默认highp)
            "precision mediump float;\n" +
            "uniform mediump vec4 u_Color;\n" +
            "void main()\n" +
            "{\n" +
            // gl_FragColor：GL中默认定义的输出变量，决定了当前片段的最终颜色
            "    gl_FragColor = u_Color;\n" +
            "}";
    private final int mProgram;
    private final int uColorLocation;
    private final ProjectionMatrixHelper projectionMatrix;

    Point() {

        mProgram = makeProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        projectionMatrix = new ProjectionMatrixHelper(mProgram, "u_Matrix");
// 获取顶点坐标属性在OpenGL程序中的索引
        int aPositionLocation = GLES20.glGetAttribLocation(mProgram, "a_Position");

// 获取颜色Uniform在OpenGL程序中的索引
        uColorLocation = GLES20.glGetUniformLocation(mProgram, "u_Color");
        FloatBuffer mVertexData = GlHelper.createFloatBuffer(new float[]{0, 1, 0,
                1, 1, 0,
                -1, 0, 0,
                0, 0, 0,
                1, 0, 0,
                0, -1, 0,
                1, -1, 0});
// 将缓冲区的指针移动到头部，保证数据是从最开始处读取
        mVertexData.position(0);
// 关联顶点坐标属性和缓存数据
// 1. 位置索引；
// 2. 每个顶点属性需要关联的分量个数(必须为1、2、3或者4。初始值为4。)；
// 3. 数据类型；
// 4. 指定当被访问时，固定点数据值是否应该被归一化(GL_TRUE)或者直接转换为固定点值(GL_FALSE)(只有使用整数数据时)
// 5. 指定连续顶点属性之间的偏移量。如果为0，那么顶点属性会被理解为：它们是紧密排列在一起的。初始值为0。
// 6. 数据缓冲区
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT,
                false, 0, mVertexData);

// 通知GL程序使用指定的顶点属性索引
        GLES20.glEnableVertexAttribArray(aPositionLocation);
    }


    public void draw() {
        // 步骤1：使用glClearColor设置的颜色，刷新Surface
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 更新u_Color的值，即更新画笔颜色
        GLES20.glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);

        // 使用数组绘制图形：1.绘制的图形类型；2.从顶点数组读取的起点；3.从顶点数组读取的数据长度
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 7);
        GLES20.glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 7);
    }

    /**
     * 创建OpenGL程序对象
     *
     * @param vertexShader   顶点着色器代码
     * @param fragmentShader 片段着色器代码
     * @return
     */
    protected int makeProgram(String vertexShader, String fragmentShader) {
        // 步骤1：编译顶点着色器
        int vertexShaderId = GlHelper.compileVertexShader(vertexShader);
        // 步骤2：编译片段着色器
        int fragmentShaderId = GlHelper.compileFragmentShader(fragmentShader);
        // 步骤3：将顶点着色器、片段着色器进行链接，组装成一个OpenGL程序
        int mProgram = GlHelper.linkProgram(vertexShaderId, fragmentShaderId);

        if (BuildConfig.DEBUG) {
            GlHelper.validateProgram(mProgram);
        }

        // 步骤4：通知OpenGL开始使用该程序
        GLES20.glUseProgram(mProgram);
        return mProgram;
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        projectionMatrix.enable(width, height);
    }
}
