package com.cjburkey.conquerer.ecs.system.engine;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.cjburkey.conquerer.GameEngine;
import com.cjburkey.conquerer.ecs.component.Camera;
import com.cjburkey.conquerer.ecs.component.render.MeshRender;
import com.cjburkey.conquerer.ecs.component.render.ui.UiElement;
import com.cjburkey.conquerer.ecs.component.transform.Pos;
import com.cjburkey.conquerer.ecs.component.transform.Rot;
import com.cjburkey.conquerer.ecs.component.transform.Scale;
import com.cjburkey.conquerer.gl.shader.Shader;
import org.joml.Matrix4fc;

import static com.cjburkey.conquerer.math.Transformation.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings("unused")
public final class UiElementSystem extends IteratingSystem {

    private static Shader shader;

    private ComponentMapper<Pos> mPos;
    private ComponentMapper<Rot> mRot;
    private ComponentMapper<Scale> mScale;
    private ComponentMapper<MeshRender> mMeshRender;
    private ComponentMapper<UiElement> mUiElement;

    public UiElementSystem() {
        super(Aspect.all(Pos.class, Rot.class, Scale.class, MeshRender.class, UiElement.class));
    }

    public static void initShader(Shader newShader) {
        if (shader == null) shader = newShader;
    }

    public static Shader shader() {
        return shader;
    }

    protected void process(int entityId) {
        // Only render if the UI shader has been created
        if (shader == null) return;

        // Check if this UI element has a mesh
        MeshRender meshRender = mMeshRender.get(entityId);
        if (meshRender.mesh == null) return;

        UiElement uiElement = mUiElement.get(entityId);

        // Locate the main camera and generate a model matrix for the position of the element
        Camera mainCamera = GameEngine.getMainCamera().getComponent(Camera.class);
        Matrix4fc modelMatrix = getModelMatrix(mPos.get(entityId).position, mRot.get(entityId).rotation, mScale.get(entityId).scale, -1.0f);

        // Render the mesh
        boolean textured = (uiElement.texture != null);
        shader.bind();
        shader.setUniform("projectionMatrix", mainCamera.windowMatrix);
        shader.setUniform("modelMatrix", modelMatrix);
        shader.setUniform("isTextured", textured);
        shader.setUniform("isFont", uiElement.isFont);
        shader.setUniform("colorize", uiElement.colorize);
        if (textured) uiElement.texture.bind();
        meshRender.mesh.render();
    }

}
