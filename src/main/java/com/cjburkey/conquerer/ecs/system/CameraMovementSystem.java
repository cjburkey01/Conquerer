package com.cjburkey.conquerer.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.cjburkey.conquerer.Util;
import com.cjburkey.conquerer.ecs.component.Camera;
import com.cjburkey.conquerer.ecs.component.input.CameraMovement;
import com.cjburkey.conquerer.ecs.component.input.SmoothMovement;
import com.cjburkey.conquerer.ecs.component.transform.Pos;
import com.cjburkey.conquerer.glfw.Input;
import org.joml.Vector2f;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
public class CameraMovementSystem extends IteratingSystem {
    
    private ComponentMapper<Pos> mPos;
    private ComponentMapper<Camera> mCamera;
    private ComponentMapper<CameraMovement> mCameraMovement;
    private ComponentMapper<SmoothMovement> mSmoothMovement;
    
    public CameraMovementSystem() {
        super(Aspect.all(Pos.class, Camera.class, CameraMovement.class));
    }
    
    protected void process(int entityId) {
        float delta = world.getDelta();
        
        // Components
        Pos pos = mPos.get(entityId);
        Camera camera = mCamera.get(entityId);
        CameraMovement cameraMovement = mCameraMovement.get(entityId);
        SmoothMovement smoothMovement = mSmoothMovement.has(entityId) ? mSmoothMovement.get(entityId) : null;
        
        // Calculate delta position
        Vector2f deltaPosition = new Vector2f();
        if (Input.getAnyKeysDown(GLFW_KEY_D, GLFW_KEY_RIGHT)) deltaPosition.x += 1.0f;
        if (Input.getAnyKeysDown(GLFW_KEY_A, GLFW_KEY_LEFT)) deltaPosition.x -= 1.0f;
        if (Input.getAnyKeysDown(GLFW_KEY_W, GLFW_KEY_UP)) deltaPosition.y += 1.0f;
        if (Input.getAnyKeysDown(GLFW_KEY_S, GLFW_KEY_DOWN)) deltaPosition.y -= 1.0f;
        if (deltaPosition.x != 0.0f || deltaPosition.y != 0.0f) {
            deltaPosition.normalize().mul((cameraMovement.speed + cameraMovement.goalZoom * cameraMovement.zoomMoveRatio) * delta);
        }
        
        float scroll = min(max(5.0f, cameraMovement.goalZoom - Input.scrollY() * cameraMovement.zoomSpeed), 50.0f);
        
        // Perform or schedule movement
        if (smoothMovement != null) {
            smoothMovement.goalPosition.add(deltaPosition.x, deltaPosition.y, 0.0f);
            cameraMovement.goalZoom = scroll;
            camera.orthographicSize = Util.dampSpringCrit(cameraMovement.goalZoom, camera.orthographicSize, cameraMovement.zoomVelocity, smoothMovement.smoothing, delta);
        } else {
            pos.position.add(deltaPosition.x, deltaPosition.y, 0.0f);
            camera.orthographicSize = scroll;
        }
    }
    
}
