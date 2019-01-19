package com.cjburkey.conquerer.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.cjburkey.conquerer.Conquerer;
import com.cjburkey.conquerer.ecs.component.Camera;
import com.cjburkey.conquerer.ecs.component.render.MeshRender;
import com.cjburkey.conquerer.ecs.component.render.ShaderRender;
import com.cjburkey.conquerer.ecs.component.transform.Pos;
import com.cjburkey.conquerer.ecs.component.transform.Rot;
import com.cjburkey.conquerer.ecs.component.transform.Scale;
import com.cjburkey.conquerer.math.Transformation;
import org.joml.Matrix4fc;

/**
 * Created by CJ Burkey on 2019/01/12
 */
public class RenderSystem extends IteratingSystem {
    
    private ComponentMapper<Pos> mPos;
    private ComponentMapper<Rot> mRot;
    private ComponentMapper<Scale> mScale;
    private ComponentMapper<ShaderRender> mShaderRender;
    private ComponentMapper<MeshRender> mMeshRender;
    private ComponentMapper<Camera> mCamera;
    
    public RenderSystem() {
        super(Aspect.all(ShaderRender.class, MeshRender.class));
    }
    
    protected void process(int entityId) {
        // Components
        ShaderRender shaderRender = mShaderRender.get(entityId);
        MeshRender meshRender = mMeshRender.get(entityId);
        Pos pos = mPos.has(entityId) ? mPos.get(entityId) : null;
        Rot rot = mRot.has(entityId) ? mRot.get(entityId) : null;
        Scale scale = mScale.has(entityId) ? mScale.get(entityId) : null;
        
        // Check if mesh cannot be rendered
        if (shaderRender.shader == null || meshRender.mesh == null) return;
        if (shaderRender.shader.getTransformsModel() && (pos == null || rot == null || scale == null)) return;
        
        // Get the main camera for which to render the mesh
        Camera camera = null;
        if (shaderRender.shader.getTransformsProjection() || shaderRender.shader.getTransformsView()) {
            if (!mCamera.has(Conquerer.INSTANCE.mainCamera)) return;
            camera = mCamera.get(Conquerer.INSTANCE.mainCamera);
        }
        
        Matrix4fc modelMatrix = null;
        if (shaderRender.shader.getTransformsModel() && pos != null && rot != null && scale != null) {
            modelMatrix = Transformation.getModelMatrix(pos.position, rot.rotation, scale.scale);
        }
        
        // Render the mesh
        shaderRender.shader.bind();
        if (camera != null && shaderRender.shader.getTransformsProjection()) shaderRender.shader.setUniform("projectionMatrix", camera.projectionMatrix);
        if (camera != null && shaderRender.shader.getTransformsView()) shaderRender.shader.setUniform("viewMatrix", camera.viewMatrix);
        if (modelMatrix != null && shaderRender.shader.getTransformsModel()) shaderRender.shader.setUniform("modelMatrix", modelMatrix);
        meshRender.mesh.render();
    }
    
}
