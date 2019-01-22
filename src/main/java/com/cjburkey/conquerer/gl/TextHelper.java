package com.cjburkey.conquerer.gl;

import com.cjburkey.conquerer.math.Rectf;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import static com.cjburkey.conquerer.Log.*;
import static com.cjburkey.conquerer.util.Util.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Created by CJ Burkey on 2019/01/21
 */
@SuppressWarnings("unused")
public class TextHelper {
    
    public static Font loadFont(InputStream inputStream) {
        if (inputStream == null) throw new NullPointerException("Input stream cannot be null to load font");
        ByteBuffer rawFontBytes = readRawStream( inputStream).orElseThrow(() -> new NullPointerException("Failed to read raw input stream bytes"));
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, rawFontBytes, 0)) {
            throw new IllegalStateException("Failed to initialize font");
        }
        return new Font(fontInfo);
    }
    
    @SuppressWarnings("WeakerAccess")
    public static final class Font {
        
        public final STBTTFontinfo fontInfo;
        public final int ascent;
        public final int decent;
        public final int lineGap;
        
        private Font(STBTTFontinfo fontInfo) {
            this.fontInfo = fontInfo;
            try (MemoryStack stack = stackPush()) {
                IntBuffer ascent = stack.mallocInt(1);
                IntBuffer decent = stack.mallocInt(1);
                IntBuffer lineGap = stack.mallocInt(1);
                stbtt_GetFontVMetrics(fontInfo, ascent, decent, lineGap);
                this.ascent = ascent.get(0);
                this.decent = ascent.get(0);
                this.lineGap = ascent.get(0);
            }
        }
        
        public float getScale(int lineHeight) {
            return stbtt_ScaleForPixelHeight(fontInfo, lineHeight);
        }
        
        public Rectf getBoundingBox(char character, int lineHeight) {
            try (MemoryStack stack = stackPush()) {
                IntBuffer minX = stack.mallocInt(1);
                IntBuffer minY = stack.mallocInt(1);
                IntBuffer maxX = stack.mallocInt(1);
                IntBuffer maxY = stack.mallocInt(1);
                float scale = getScale(lineHeight);
                stbtt_GetCodepointBitmapBox(fontInfo, character, scale, scale, minX, minY, maxX, maxY);
                return new Rectf(minX.get(0), minY.get(0), maxX.get(0), maxY.get(0));
            }
        }
        
        public float getCharacterWidth(char character, int lineHeight) {
            try (MemoryStack stack = stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                stbtt_GetCodepointHMetrics(fontInfo, character, width, stack.mallocInt(1));
                return width.get(0) * getScale(lineHeight);
            }
        }
        
        public float getCharacterKerning(char character, char nextCharacter, int lineHeight) {
            return stbtt_GetCodepointKernAdvance(fontInfo, character, nextCharacter) * getScale(lineHeight);
        }
        
        public FontBitmap generateBitmap(String input, int lineHeight) {
            // Get a unique array of characters (no repeats to make texture small. Use uvs instead of raw image)
            final CharOpenHashSet charactersUnique = new CharOpenHashSet();
            for (char character : input.toCharArray()) charactersUnique.add(character);
            final char[] characters = charactersUnique.toArray(new char[0]);
            
            // Generate a bitmap bounding box
            // TODO: CALCULATE SIZE AUTOMATICALLY; MIGHT BE DIFFICULT...RIP ME
            int bitmapWidth = 512;
            int bitmapHeight = 512;
            
            // Keep track of the UVs for each character
            final Char2ObjectOpenHashMap<Vector4fc> uvs = new Char2ObjectOpenHashMap<>();
            final Texture texture = new Texture();
            
            // Create the image buffer
            // This contains the raw pixel data for the glyph
            ByteBuffer rawTexture = memAlloc(bitmapWidth * bitmapHeight);
            int x = 0;
            
            for (char character : characters) {
                // Write the character into the texture
                Rectf boundingBox = getBoundingBox(character, lineHeight);
                float scale = getScale(lineHeight);
                
                rawTexture.position(x);
                stbtt_MakeCodepointBitmap(fontInfo, rawTexture, (int) boundingBox.width, (int) boundingBox.height, bitmapWidth, scale, scale, character);
                
                // Increment the x and move onto the next character
                uvs.put(character, new Vector4f((float) x / bitmapWidth,
                        1.0f,
                        (x + boundingBox.width) / bitmapWidth,
                        1.0f - (boundingBox.height / bitmapHeight)));
                x += getBoundingBox(character, lineHeight).width;
            }
            
            // Send the texture to the graphics card
            rawTexture.position(0);
            texture.bufferImage(rawTexture, bitmapWidth, bitmapHeight, GL_RED, GL_RED, true, true, true);
            memFree(rawTexture);
            
            return new FontBitmap(bitmapWidth, bitmapHeight, lineHeight, this, texture, uvs);
        }
        
        public void destroy() {
            fontInfo.close();
        }
        
    }
    
    @SuppressWarnings("WeakerAccess")
    public static final class FontBitmap {
        
        public final int width;
        public final int height;
        public final int lineHeight;
        public final Font font;
        public final Texture texture;
        private final Char2ObjectOpenHashMap<Vector4fc> uvs;
        
        public FontBitmap(int width, int height, int lineHeight, Font font, Texture texture, Char2ObjectOpenHashMap<Vector4fc> uvs) {
            this.width = width;
            this.height = height;
            this.lineHeight = lineHeight;
            this.font = font;
            this.texture = texture;
            this.uvs = uvs;
        }
        
        public Vector4fc getUvs(char character) {
            return uvs.get(character);
        }
        
    }
    
}
