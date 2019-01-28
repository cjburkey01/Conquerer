package com.cjburkey.conquerer.ui;

import com.cjburkey.conquerer.gl.Mesh;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;

/**
 * Created by CJ Burkey on 2019/01/27
 */
@SuppressWarnings("UnusedReturnValue")
public class UiSolidBox extends UiComponent {
    
    private final Vector3f color = new Vector3f(1.0f);
    private final Vector2f size = new Vector2f();
    
    public UiSolidBox() {
    }
    
    public UiSolidBox(Vector2fc size) {
        setSize(size);
    }
    
    public UiSolidBox setSize(Vector2fc size) {
        this.size.set(size);
        regenerateMesh();
        return this;
    }
    
    public UiSolidBox setColor(Vector3f color) {
        this.color.set(color);
        regenerateMesh();
        return this;
    }
    
    @Override
    protected void generateMesh(Mesh mesh) {
        Mesh.builder()
                .addColorQuad(new Vector2f(), size, color)
                .apply(mesh);
    }
    
}
