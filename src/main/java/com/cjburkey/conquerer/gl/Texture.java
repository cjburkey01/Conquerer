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
    
    public Texture() {
        texture = glGenTextures();
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glEnable(GL_TEXTURE);
        glActiveTexture(GL_TEXTURE0);
    }
    
    public void bufferImage(ByteBuffer rawData, int width, int height, int providedFormat, int internalFormat, boolean mipmap, boolean linear, boolean clamp) {
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, providedFormat, GL_UNSIGNED_BYTE, rawData);
        if (mipmap) glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, (clamp ? GL_CLAMP_TO_EDGE : GL_REPEAT));
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, (clamp ? GL_CLAMP_TO_EDGE : GL_REPEAT));
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, (linear ? GL_LINEAR_MIPMAP_LINEAR : GL_NEAREST_MIPMAP_NEAREST));
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, (linear ? GL_LINEAR_MIPMAP_LINEAR : GL_NEAREST_MIPMAP_NEAREST));
    }
    
    public void destroy() {
        glDeleteTextures(texture);
    }
    
    public void bind() {
        if (currentTexture != texture) {
            glBindTexture(GL_TEXTURE_2D, texture);
            currentTexture = texture;
        }
    }
    
}
