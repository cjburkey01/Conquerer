package com.cjburkey.conquerer.ecs.system.engine;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.IteratingSystem;
import com.cjburkey.conquerer.ecs.component.engine.Camera;
import com.cjburkey.conquerer.ecs.component.engine.Transform;
import com.cjburkey.conquerer.ecs.component.engine.render.MeshRender;
import com.cjburkey.conquerer.ecs.component.engine.render.ui.UiElement;
import com.cjburkey.conquerer.engine.GameEngine;
import com.cjburkey.conquerer.gl.shader.Shader;
import org.joml.Matrix4fc;

import static com.cjburkey.conquerer.math.Transformation.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings("unused")
public final class UiElementSystem extends IteratingSystem {

    private static Shader shader;

    private ComponentMapper<Transform> mTransform;
    private ComponentMapper<MeshRender> mMeshRender;
    private ComponentMapper<UiElement> mUiElement;

    public UiElementSystem() {
        super(Aspect.all(Transform.class, MeshRender.class, UiElement.class));
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

        // Locate the main camera and build a model matrix for the position of the element
        Entity mainCameraEnt = GameEngine.getMainCamera();
        Camera mainCamera = mainCameraEnt.getComponent(Camera.class);
        Transform transform = mTransform.get(entityId);
        Matrix4fc modelMatrix = getModelMatrix(transform, -1.0f);

        // Update the component if necessary
        uiElement.uiComponent.onEntityUpdate();

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
