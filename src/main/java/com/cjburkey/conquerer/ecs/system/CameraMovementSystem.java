package com.cjburkey.conquerer.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.cjburkey.conquerer.ecs.component.Camera;
import com.cjburkey.conquerer.ecs.component.input.CameraMovement;
import com.cjburkey.conquerer.ecs.component.input.SmoothMovement;
import com.cjburkey.conquerer.ecs.component.transform.Pos;
import com.cjburkey.conquerer.glfw.Input;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;

import static com.cjburkey.conquerer.Conquerer.*;
import static com.cjburkey.conquerer.math.Transformation.*;
import static com.cjburkey.conquerer.util.Util.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings("unused")
public final class CameraMovementSystem extends IteratingSystem {

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
        if (Input.getAnyKeysDown(cameraMovement.rightKeys)) deltaPosition.x += 1.0f;
        if (Input.getAnyKeysDown(cameraMovement.leftKeys)) deltaPosition.x -= 1.0f;
        if (Input.getAnyKeysDown(cameraMovement.upKeys)) deltaPosition.y += 1.0f;
        if (Input.getAnyKeysDown(cameraMovement.downKeys)) deltaPosition.y -= 1.0f;
        if (deltaPosition.x != 0.0f || deltaPosition.y != 0.0f) {
            // TODO: MOVEMENT SPEED BASED ON ZOOM LEVEL
            deltaPosition.normalize().mul((cameraMovement.speed + cameraMovement.goalZoom * cameraMovement.zoomMoveRatio) * delta);
        }

        // Mouse move
        Vector2fc currMousePos = Input.mousePos();
        Vector3f mouseWorldPos = cameraToPlane(pos.position, camera, currMousePos, INSTANCE.worldPlane);

        // Change cursor for dragging
        if (Input.getAnyMousePressed(cameraMovement.activatingMouseForDrag)) {
            INSTANCE.window().setCursor(GLFW_CROSSHAIR_CURSOR);
            for (int btn : cameraMovement.activatingMouseForDrag) {
                if (Input.getMousePressed(btn)) cameraMovement.startedDragBtn = btn;
            }
        }
        if (Input.getMouseUp(cameraMovement.startedDragBtn)) {
            cameraMovement.startedDragBtn = -1;
            INSTANCE.window().setCursor(-1);
        }

        // Camera dragging
        if (Input.getAnyMouseDown(cameraMovement.activatingMouseForDrag)) {
            Vector3f prevWorldPos = cameraToPlane(pos.position, camera, cameraMovement.previousMousePos, INSTANCE.worldPlane);
            Vector3f deltaMouse = mouseWorldPos.sub(prevWorldPos, new Vector3f());
            deltaPosition.sub(deltaMouse.x, deltaMouse.y);
        }
        cameraMovement.previousMousePos.set(currMousePos);

        // TODO: ZOOM SPEED BASED ON CURRENT ZOOM LEVEL (TO MAKE ZOOMING OUT FASTER AND IN SLOWER)
        float zoom = clamp(cameraMovement.goalZoom - Input.scrollY() * cameraMovement.zoomSpeed, cameraMovement.zoomClose, cameraMovement.zoomFar);

        if (smoothMovement != null) {
            // Schedule movement with the smooth mover
            smoothMovement.goalPosition.add(deltaPosition.x, deltaPosition.y, 0.0f);
            smoothMovement.goalPosition.x = clamp(smoothMovement.goalPosition.x, cameraMovement.bounds.minX, cameraMovement.bounds.maxX);
            smoothMovement.goalPosition.y = clamp(smoothMovement.goalPosition.y, cameraMovement.bounds.minY, cameraMovement.bounds.maxY);

            // Zoom smooth movement is handled separately from the smooth movement system
            cameraMovement.goalZoom = zoom;
            camera.orthographicSize = dampSpringCrit(cameraMovement.goalZoom,
                    camera.orthographicSize,
                    cameraMovement.zoomVelocity,
                    smoothMovement.smoothing,
                    delta);
        } else {
            // Move the camera without the smooth movement
            pos.position.add(deltaPosition.x, deltaPosition.y, 0.0f);
            camera.orthographicSize = zoom;
        }
    }

}
