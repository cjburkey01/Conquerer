package com.cjburkey.conquerer.gl;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.*;

/**
 * Created by CJ Burkey on 2019/01/21
 */
@SuppressWarnings("WeakerAccess")
public class Texture {
    
    private static int currentTexture = -1;
    
    private int texture;
    private boolean isSub = false;
    
    public Texture() {
        texture = glGenTextures();
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);      // Stop alignment checks by OpenGL
        glEnable(GL_TEXTURE);
        glActiveTexture(GL_TEXTURE0);
    }
    
    public void apply(boolean linear, boolean clamp) {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, (clamp ? GL_CLAMP_TO_EDGE : GL_REPEAT));
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, (clamp ? GL_CLAMP_TO_EDGE : GL_REPEAT));
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, (linear ? GL_LINEAR_MIPMAP_LINEAR : GL_NEAREST_MIPMAP_LINEAR));
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, (linear ? GL_LINEAR_MIPMAP_LINEAR : GL_NEAREST_MIPMAP_LINEAR));
    }
    
    public void generateMipmaps() {
        glGenerateMipmap(GL_TEXTURE_2D);
    }
    
    public void bufferImage(ByteBuffer rawData, int width, int height, int providedFormat, int internalFormat, boolean mipmap, boolean linear, boolean clamp) {
        if (isSub) return;
        bind();
        apply(linear, clamp);
        if (rawData == null) {
            glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, providedFormat, GL_UNSIGNED_BYTE, 0L);
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, providedFormat, GL_UNSIGNED_BYTE, rawData);
            if (mipmap) generateMipmaps();
        }
    }
    
    public void initSubImage(int width, int height, int internalFormat) {
        bufferImage(null, width, height, GL_RED, internalFormat, false, false, false);
        isSub = true;
    }
    
    public void subBufferImage(ByteBuffer rawData, int x, int y, int width, int height, int providedFormat, boolean linear, boolean clamp) {
        if (!isSub) return;
        bind();
        apply(linear, clamp);
        glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, width, height, providedFormat, GL_UNSIGNED_BYTE, rawData);
    }
    
    public void destroy() {
        if (isValid()) {
            glDeleteTextures(texture);
            texture = 0;
            isSub = false;
        }
    }
    
    public void bind() {
        if (currentTexture != texture) {
            glBindTexture(GL_TEXTURE_2D, texture);
            currentTexture = texture;
        }
    }
    
    public boolean isValid() {
        return texture > 0;
    }
    
}
