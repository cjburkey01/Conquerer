package com.cjburkey.conquerer.ecs.system.engine;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.cjburkey.conquerer.ecs.component.engine.Camera;
import com.cjburkey.conquerer.ecs.component.engine.Transform;
import com.cjburkey.conquerer.engine.GameEngine;
import com.cjburkey.conquerer.glfw.Window;
import com.cjburkey.conquerer.math.Transformation;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings("unused")
public final class CameraSystem extends IteratingSystem {

    private ComponentMapper<Transform> mTransform;
    private ComponentMapper<Camera> mCamera;

    public CameraSystem() {
        super(Aspect.all(Transform.class, Camera.class));
    }

    protected void process(int entityId) {
        // Cache the window for this cycle (keeping this out of the class scope because Window may be recreated at some point)
        Window w = GameEngine.window();

        // Components
        Transform transform = mTransform.get(entityId);
        Camera camera = mCamera.get(entityId);

        // Set up the matrices for the camera
        if (camera.perspective) {
            camera.projectionMatrix.set(Transformation.getProjectionMatrix(camera.fovDegrees, w.getWidth(), w.getHeight(), camera.nearPlane, camera.farPlane));
        } else {
            camera.projectionMatrix.set(Transformation.getOrthographicMatrix(w.getWidth(), w.getHeight(), camera.orthographicSize, camera.nearPlane, camera.farPlane));
        }
        camera.viewMatrix.set(Transformation.getViewMatrix(transform.position, transform.rotation));
        camera.windowMatrix.set(Transformation.getOrthographicMatrix(0.0f, w.getWidth(), w.getHeight(), 0.0f, camera.nearPlane, camera.farPlane));
    }

}
