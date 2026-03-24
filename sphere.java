package com.appeng.tests.movingshapes.objects;

import android.content.Context;
import android.opengl.GLES20;

import com.appeng.tests.movingshapes.MyGLRenderer;
import com.appeng.tests.movingshapes.R;
import com.appeng.tests.movingshapes.utils.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable sphere for OpenGL ES 2.0.
 *
 * This implementation:
 * - generates a sphere using stacks and slices
 * - builds one triangle strip per stack band
 * - uses a single vertex buffer and multiple draw calls
 *
 * Notes:
 * - no indices are used here, to keep the code easier to read
 * - shaders are expected to provide:
 *      attribute vec4 vPosition;
 *      uniform mat4 uMVPMatrix;
 *      uniform vec4 vColor;
 */
public class Sphere {

    private static final int COORDS_PER_VERTEX = 3;
    private static final int BYTES_PER_FLOAT = 4;

    private final FloatBuffer vertexBuffer;
    private final int program;

    private final int[] stripOffsets;
    private final int[] stripVertexCounts;

    private int positionHandle;
    private int colorHandle;
    private int mvpMatrixHandle;

    private final float[] color = {0.2f, 0.709803922f, 0.898039216f, 1.0f};

    /**
     * @param context Android context
     * @param radius sphere radius
     * @param stacks number of horizontal divisions (minimum 2, recommended 12+)
     * @param slices number of vertical divisions (minimum 3, recommended 24+)
     */
    public Sphere(Context context, float radius, int stacks, int slices) {
        if (stacks < 2) {
            throw new IllegalArgumentException("stacks must be >= 2");
        }
        if (slices < 3) {
            throw new IllegalArgumentException("slices must be >= 3");
        }

        List<Float> vertexList = new ArrayList<>();
        stripOffsets = new int[stacks];
        stripVertexCounts = new int[stacks];

        int vertexOffset = 0;

        // Build one triangle strip for each band between two latitudes.
        for (int stack = 0; stack < stacks; stack++) {
            float phi0 = (float) Math.PI * (-0.5f + (float) stack / stacks);
            float phi1 = (float) Math.PI * (-0.5f + (float) (stack + 1) / stacks);

            float y0 = (float) Math.sin(phi0);
            float r0 = (float) Math.cos(phi0);

            float y1 = (float) Math.sin(phi1);
            float r1 = (float) Math.cos(phi1);

            stripOffsets[stack] = vertexOffset;

            for (int slice = 0; slice <= slices; slice++) {
                float theta = (float) (2.0f * Math.PI * slice / slices);

                float x = (float) Math.cos(theta);
                float z = (float) Math.sin(theta);

                // Vertex on current latitude
                vertexList.add(radius * r0 * x);
                vertexList.add(radius * y0);
                vertexList.add(radius * r0 * z);
                vertexOffset++;

                // Vertex on next latitude
                vertexList.add(radius * r1 * x);
                vertexList.add(radius * y1);
                vertexList.add(radius * r1 * z);
                vertexOffset++;
            }

            stripVertexCounts[stack] = (slices + 1) * 2;
        }

        float[] vertices = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            vertices[i] = vertexList.get(i);
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        program = ShaderHelper.getProgram(
                context,
                R.raw.sphere_vertex_shader,
                R.raw.sphere_fragment_shader
        );
    }

    public void setColor(float r, float g, float b, float a) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");

        MyGLRenderer.checkGlError("glGetAttribLocation / glGetUniformLocation");

        GLES20.glEnableVertexAttribArray(positionHandle);

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(
                positionHandle,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                COORDS_PER_VERTEX * BYTES_PER_FLOAT,
                vertexBuffer
        );

        GLES20.glUniform4fv(colorHandle, 1, color, 0);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        MyGLRenderer.checkGlError("glVertexAttribPointer / uniforms");

        for (int i = 0; i < stripOffsets.length; i++) {
            GLES20.glDrawArrays(
                    GLES20.GL_TRIANGLE_STRIP,
                    stripOffsets[i],
                    stripVertexCounts[i]
            );
        }

        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
