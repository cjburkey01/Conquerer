package com.cjburkey.conquerer.ui;

import com.cjburkey.conquerer.gl.FontHelper;
import com.cjburkey.conquerer.gl.Mesh;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

/**
 * Created by CJ Burkey on 2019/01/27
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class UiText extends UiComponent {
    
    private boolean ignoreRegen = true;
    private CharSequence text;
    private FontHelper.FontBitmap fontBitmap;
    private float size = 12.0f;
    
    public UiText() {
        setColor(new Vector3f(1.0f));
        isFont = true;
    }
    
    public UiText(CharSequence text, FontHelper.FontBitmap fontBitmap) {
        this();
        setText(text);
        setFontBitmap(fontBitmap);
        
        ignoreRegen = false;
        regenerateMesh();
    }
    
    public UiText setColor(Vector3fc color) {
        uiElement().colorize.set(new Vector4f(color, 1.0f));
        // Regeneration is not needed because the color (for textured meshes) is a uniform, not a mesh attribute
        return this;
    }
    
    public UiText setText(CharSequence text) {
        this.text = text;
        regenerateMesh();
        return this;
    }
    
    public UiText setFontBitmap(FontHelper.FontBitmap fontBitmap) {
        this.fontBitmap = fontBitmap;
        this.texture = fontBitmap.texture;
        regenerateMesh();
        return this;
    }
    
    public UiText setSize(float size) {
        this.size = size;
        regenerateMesh();
        return this;
    }
    
    @Override
    protected void generateMesh(Mesh mesh) {
        if (ignoreRegen) return;
        if (text == null || fontBitmap == null || size == 0.0f) {
            // If missing text or a font, empty the mesh
            Mesh.builder().apply(mesh);
            return;
        }
        Mesh.builder()
                .addText(fontBitmap, text, size, null, true)
                .apply(mesh);
    }
    
}
