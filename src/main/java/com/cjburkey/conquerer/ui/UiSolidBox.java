package com.cjburkey.conquerer.ui;

import com.cjburkey.conquerer.gl.Mesh;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

/**
 * Created by CJ Burkey on 2019/01/27
 */
@SuppressWarnings("UnusedReturnValue")
public class UiSolidBox extends UiBox {

    public UiSolidBox setColor(Vector4fc color) {
        uiElement().colorize.set(new Vector4f(color));
        return this;
    }

    public UiSolidBox setColor(Vector3fc color) {
        return setColor(new Vector4f(color, 1.0f));
    }

    @Override
    public UiSolidBox setSize(Vector2fc size) {
        return (UiSolidBox) super.setSize(size);
    }

    @Override
    protected void generateMesh(Mesh mesh) {
        Mesh.builder()
            .addColorQuad(new Vector2f(), size, new Vector3f(1.0f))
            .apply(mesh);
    }

}
