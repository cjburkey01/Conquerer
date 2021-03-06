package com.cjburkey.conquerer.ecs.system.engine;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.cjburkey.conquerer.ecs.component.engine.Camera;
import com.cjburkey.conquerer.ecs.component.engine.Transform;
import com.cjburkey.conquerer.ecs.component.engine.render.MeshRender;
import com.cjburkey.conquerer.ecs.component.engine.render.ShaderRender;
import com.cjburkey.conquerer.ecs.component.engine.render.Textured;
import com.cjburkey.conquerer.engine.GameEngine;
import com.cjburkey.conquerer.math.Transformation;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.joml.Matrix4fc;

import static com.cjburkey.conquerer.util.Log.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class RenderSystem extends BaseEntitySystem {

    // Keep sorted map of objects by their z-values to draw furthest away first
    private final IntArrayList entities = new IntArrayList();

    private ComponentMapper<Transform> mTransform;
    private ComponentMapper<ShaderRender> mShaderRender;
    private ComponentMapper<MeshRender> mMeshRender;
    private ComponentMapper<Textured> mTextured;
    private ComponentMapper<Camera> mCamera;

    public RenderSystem() {
        super(Aspect.all(ShaderRender.class, MeshRender.class));
    }

    protected void inserted(int entityId) {
        super.inserted(entityId);

        entities.add(entityId);
        forceSort();
    }

    protected void removed(int entityId) {
        super.removed(entityId);

        int at = entities.indexOf(entityId);
        if (at < 0) {
            debug("Failed to remove entity {}", entityId);
        } else {
            entities.removeInt(at);
        }
    }

    protected void processSystem() {
        for (int entityId : entities) {
            process(entityId);
        }
    }

    protected void process(int entityId) {
        // Components
        ShaderRender shaderRender = mShaderRender.get(entityId);
        MeshRender meshRender = mMeshRender.get(entityId);
        Transform transform = mTransform.has(entityId) ? mTransform.get(entityId) : null;

        // Check if mesh cannot be rendered
        if (shaderRender.shader == null || meshRender.mesh == null) return;
        if (shaderRender.shader.getTransformsModel() && transform == null) return;

        // Get the main camera for which to render the mesh
        Camera camera = null;
        if (shaderRender.shader.getTransformsProjection() || shaderRender.shader.getTransformsView()) {
            if (!mCamera.has(GameEngine.mainCamera)) return;
            camera = mCamera.get(GameEngine.mainCamera);
        }

        Matrix4fc modelMatrix = null;
        if (shaderRender.shader.getTransformsModel() && transform != null) {
            modelMatrix = Transformation.getModelMatrix(transform);
        }

        // Check if texture needs to be bound
        if (mTextured.has(entityId)) {
            Textured textured = mTextured.get(entityId);
            if (textured.texture != null) textured.texture.bind();
        }

        // Render the mesh
        shaderRender.shader.bind();
        shaderRender.uniformCallbacks.values().forEach(callback -> callback.onCall(shaderRender.shader));
        if (camera != null && shaderRender.shader.getTransformsProjection())
            shaderRender.shader.setUniform("projectionMatrix", camera.projectionMatrix);
        if (camera != null && shaderRender.shader.getTransformsView())
            shaderRender.shader.setUniform("viewMatrix", camera.viewMatrix);
        if (modelMatrix != null && shaderRender.shader.getTransformsModel())
            shaderRender.shader.setUniform("modelMatrix", modelMatrix);
        meshRender.mesh.render();
    }

    // Sort the entities according to z-value
    public void forceSort() {
        entities.sort((o1, o2) -> {
            float o1Z = (mTransform.has(o1) ? mTransform.get(o1).position.z : 0.0f);
            float o2Z = (mTransform.has(o2) ? mTransform.get(o2).position.z : 0.0f);
            return (int) Math.signum(o1Z - o2Z);
        });
    }

}
