package com.cjburkey.conquerer.ecs.component.input;

import com.artemis.Component;
import org.joml.Vector2f;

/**
 * Created by CJ Burkey on 2019/01/15
 */
public class CameraMovement extends Component {
    
    public float goalZoom = 0.0f;
    public float[] zoomVelocity = new float[] { 0.0f };
    
    public float speed = 50.0f;
    public float zoomSpeed = 1.0f;
    public float zoomMoveRatio = 0.5f;
    
    public final Vector2f previousMousePos = new Vector2f();
    
}
