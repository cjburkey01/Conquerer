package com.cjburkey.conquerer.gl;

import com.cjburkey.conquerer.GameEngine;
import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.util.Util;
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
import static org.joml.Math.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Created by CJ Burkey on 2019/01/21
 */
@SuppressWarnings("unused")
public final class FontHelper {

    public static Font loadFont(InputStream inputStream) {
        if (inputStream == null) throw new NullPointerException("Input stream cannot be null to load font");
        ByteBuffer rawFontBytes = readRawStream(inputStream).orElseThrow(() -> new NullPointerException("Failed to read raw input stream bytes"));
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, rawFontBytes, 0)) {
            throw new IllegalStateException("Failed to initialize font");
        }
        return new Font(rawFontBytes, fontInfo);
    }

    @SuppressWarnings("WeakerAccess")
    public static final class Font {

        public final int ascent;
        public final int decent;
        public final int lineGap;
        private final ByteBuffer rawFontBytes;
        private final STBTTFontinfo fontInfo;

        private Font(ByteBuffer rawFontBytes, STBTTFontinfo fontInfo) {
            this.rawFontBytes = rawFontBytes;
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
            GameEngine.onExit(() -> memFree(rawFontBytes));  // We can't call "close()" on fontInfo because it causes a crash in Java 8 (for some reason?)
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

        public FontBitmap generateBitmap(CharSequence input, int bitmapSizePower, int lineHeight) {
            // Get a unique array of characters (no repeats to make texture small. Use uvs instead of raw image)
            final char[] characters = unique(input);

            // Generate the power-of-two bitmap size
            // (0x1 << x) == (2^x)
            final int bitmapSize = 0x1 << bitmapSizePower;

            // Keep track of the UVs for each character
            final Char2ObjectOpenHashMap<Vector4fc> uvs = new Char2ObjectOpenHashMap<>();
            final Texture texture = new Texture();
            texture.initSubImage(bitmapSize, bitmapSize, GL_RGB);

            int x = 0;
            int y = 0;
            int nextY = 0;
            final float scale = getScale(lineHeight);

            for (char character : characters) {
                // Get this character's bounding box
                Rectf boundingBox = getBoundingBox(character, lineHeight);
                int w = boundingBox.widthi();
                int h = boundingBox.heighti();

                // Verify this character will fit into the bitmap without going out of bounds at all
                if ((x + w) >= bitmapSize) {
                    x = 0;
                    y = nextY + 2;
                }
                if ((y + h) >= bitmapSize) {
                    exception(new IllegalStateException("Character bitmap of size " + bitmapSize + " not large enough to hold " + characters.length + " characters"));
                    break;
                }

                // Generate a texture large enough to hold this bitmap
                ByteBuffer rawTexture = memAlloc(w * h);
                stbtt_MakeCodepointBitmap(fontInfo, rawTexture, w, h, w, scale, scale, character);
                rawTexture.flip();

                // Send the generated texture into the texture via subimage
                texture.subBufferImage(rawTexture, x, y, w, h, GL_RED, true, true);

                // Cleanup!
                memFree(rawTexture);

                // Add to the list of uvs so we can build the text mesh
                uvs.put(character, new Vector4f((float) x / bitmapSize,
                        (float) y / bitmapSize,
                        (float) (x + w) / bitmapSize,
                        (float) (y + h) / bitmapSize));

                // The next y position should be clear of all previous lines' characters
                nextY = Util.max(nextY, y + h);

                // Increment the position for the next character
                // This is checked to be a valid position on the next loop around
                // Even if this is out of bounds, it could be the last character required
                x += w + 2;
            }

            texture.generateMipmaps();

            return new FontBitmap(bitmapSize, lineHeight, this, texture, uvs);
        }

        public FontBitmap generateBitmap(CharSequence input, int lineHeight) {
            // Calculate the area required for all of the characters
            int totalArea = 0;
            char[] chars = unique(input);
            for (char character : chars) {
                Rectf boundingBox = getBoundingBox(character, lineHeight);
                totalArea += boundingBox.widthi() * boundingBox.heighti() + 4;  // The 4 is the 2 pixel padding added to each character
            }

            // Find the next-highest power-of-two width that gives us at least the required area.
            int size = (int) Util.ceil(Math.log(Util.ceil(sqrt(totalArea))) / Math.log(2.0f));
            debug("Generating font bitmap of size {} for {} characters with a total area of {} pixels", (int) Math.pow(2.0f, size), input.length(), totalArea);
            return generateBitmap(input, size, lineHeight);
        }

        public FontBitmap generateAsciiBitmap(int lineHeight) {
            StringBuilder ascii = new StringBuilder();
            for (int i = 0; i < 127; i++) {
                ascii.append((char) i);
            }
            return generateBitmap(ascii, lineHeight);
        }

        private char[] unique(CharSequence input) {
            final CharOpenHashSet charactersUnique = new CharOpenHashSet();
            for (int i = 0; i < input.length(); i++) charactersUnique.add(input.charAt(i));
            return charactersUnique.toArray(new char[0]);
        }

    }

    @SuppressWarnings("WeakerAccess")
    public static final class FontBitmap {

        public final int width;
        public final int lineHeight;
        public final Font font;
        public final Texture texture;
        private final Char2ObjectOpenHashMap<Vector4fc> uvs;

        private FontBitmap(int width, int lineHeight, Font font, Texture texture, Char2ObjectOpenHashMap<Vector4fc> uvs) {
            this.width = width;
            this.lineHeight = lineHeight;
            this.font = font;
            this.texture = texture;
            this.uvs = uvs;
        }

        public Vector4fc getUv(char character) {
            return uvs.get(character);
        }

    }

}
