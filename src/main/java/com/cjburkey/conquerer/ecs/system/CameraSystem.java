package com.cjburkey.conquerer.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.cjburkey.conquerer.Conquerer;
import com.cjburkey.conquerer.ecs.component.Camera;
import com.cjburkey.conquerer.ecs.component.transform.Pos;
import com.cjburkey.conquerer.ecs.component.transform.Rot;
import com.cjburkey.conquerer.glfw.Window;
import com.cjburkey.conquerer.math.Transformation;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings("unused")
public final class CameraSystem extends IteratingSystem {

    private ComponentMapper<Pos> mPos;
    private ComponentMapper<Rot> mRot;
    private ComponentMapper<Camera> mCamera;

    public CameraSystem() {
        super(Aspect.all(Pos.class, Rot.class, Camera.class));
    }

    protected void process(int entityId) {
        // Cache the window for this cycle (keeping this out of the class scope because Window may be recreated at some point)
        Window w = Conquerer.INSTANCE.window();

        // Components
        Pos pos = mPos.get(entityId);
        Rot rot = mRot.get(entityId);
        Camera camera = mCamera.get(entityId);

        // Set up the matrices for the camera
        if (camera.perspective) {
            camera.projectionMatrix.set(Transformation.getProjectionMatrix(camera.fovDegrees, w.getWidth(), w.getHeight(), camera.nearPlane, camera.farPlane));
        } else {
            camera.projectionMatrix.set(Transformation.getOrthographicMatrix(w.getWidth(), w.getHeight(), camera.orthographicSize, camera.nearPlane, camera.farPlane));
        }
        camera.viewMatrix.set(Transformation.getViewMatrix(pos.position, rot.rotation));
        camera.windowMatrix.set(Transformation.getOrthographicMatrix(0.0f, w.getWidth(), w.getHeight(), 0.0f, camera.nearPlane, camera.farPlane));
    }

}
