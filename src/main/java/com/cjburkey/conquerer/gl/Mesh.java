package com.cjburkey.conquerer.gl;

import com.cjburkey.conquerer.Log;
import com.cjburkey.conquerer.Util;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings({"WeakerAccess", "SameParameterValue", "unused"})
public final class Mesh {
    
    private static int currentVao = -1;
    
    // Buffers
    private final int vao;
    private int[] vbo = new int[1];
    private int[] ebo = new int[1];
    
    private IntOpenHashSet buffers = new IntOpenHashSet();
    private IntOpenHashSet attributes = new IntOpenHashSet();
    
    // Info
    private int triangles;
    
    public Mesh() {
        vao = glGenVertexArrays();
    }
    
    public Mesh setVertices(FloatBuffer vertices) {
        bufferData(GL_ARRAY_BUFFER, vbo, vertices, 3, GL_FLOAT, 0);
        return this;
    }
    
    public Mesh setVertices(final float[] vertices) {
        try (final MemoryStack stack = stackPush()) {
            return setVertices(Util.bufferFloat(stack, vertices));
        }
    }
    
    public Mesh setIndices(ShortBuffer indices) {
        triangles = indices.limit();
        bufferData(GL_ELEMENT_ARRAY_BUFFER, ebo, indices);
        return this;
    }
    
    public Mesh setIndices(final short[] indices) {
        try (final MemoryStack stack = stackPush()) {
            return setIndices(Util.bufferShort(stack, indices));
        }
    }
    
    public void destroy() {
        // Delete buffers
        for (int buffer : buffers) glDeleteBuffers(buffer);
        buffers.clear();
        
        // Delete vertex array
        glDeleteVertexArrays(vao);
        
        currentVao = 0;
    }
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public final boolean bind() {
        if (isBound()) {
            return true;
        }
        currentVao = vao;
        if (currentVao > 0) {
            glBindVertexArray(vao);
            return true;
        }
        return false;
    }
    
    public boolean isBound() {
        return vao == currentVao;
    }
    
    public int getTriangleCount() {
        return triangles;
    }
    
    private boolean preBuff(int buffer, int[] pointer, boolean hasData, int attribId) {
        if (!bind()) {
            return false;
        }
        if (!hasData && pointer[0] > 0) {
            glDeleteBuffers(pointer[0]);
            buffers.remove(pointer[0]);
            pointer[0] = 0;
            if (attribId >= 0) {
                attributes.remove(attribId);
            }
            return false;
        } else if (!hasData) {
            return false;
        }
        if (pointer[0] <= 0) {
            pointer[0] = glGenBuffers();
            buffers.add(pointer[0]);
        }
        glBindBuffer(buffer, pointer[0]);
        return true;
    }
    
    private void postBuff(int buffer, int attribSize, int attribType, int attribId) {
        if (attribSize > 0 && attribId >= 0) {
            glVertexAttribPointer(attribId, attribSize, attribType, false, 0, 0L);
            attributes.add(attribId);
        }
        glBindBuffer(buffer, 0);
    }
    
    private void bufferData(int buffer, int[] pointer, FloatBuffer data, int attribSize, int attribType, int attribId) {
        if (preBuff(buffer, pointer, data != null, attribId)) {
            glBufferData(buffer, Objects.requireNonNull(data), GL_STATIC_DRAW);
            postBuff(buffer, attribSize, attribType, attribId);
        }
    }
    
    private void bufferData(int buffer, int[] pointer, FloatBuffer data) {
        bufferData(buffer, pointer, data, -1, -1, -1);
    }
    
    private void bufferData(int buffer, int[] pointer, ShortBuffer data, int attribSize, int attribType, int attribId) {
        if (preBuff(buffer, pointer, data != null, attribId)) {
            glBufferData(buffer, Objects.requireNonNull(data), GL_STATIC_DRAW);
            postBuff(buffer, attribSize, attribType, attribId);
        }
    }
    
    private void bufferData(int buffer, int[] pointer, ShortBuffer data) {
        bufferData(buffer, pointer, data, -1, -1, -1);
    }
    
    public void render() {
        if (!bind() || vbo[0] <= 0 || ebo[0] <= 0) {
            return;
        }
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
        for (int attribute : attributes) glEnableVertexAttribArray(attribute);
        glDrawElements(GL_TRIANGLES, triangles, GL_UNSIGNED_SHORT, 0L);
        for (int attribute : attributes) glDisableVertexAttribArray(attribute);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
//    public final void render() {
//        if (!built || material == null) return;
//        material.bind();
//        bind();
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
//        glEnableVertexAttribArray(0);
//        onPreRender();
//        if (!onRender()) glDrawElements(GL_TRIANGLES, triangles, GL_UNSIGNED_SHORT, 0);
//        onPostRender();
//        glDisableVertexAttribArray(0);
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
//    }
    
}
