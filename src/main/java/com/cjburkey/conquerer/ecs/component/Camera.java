package com.cjburkey.conquerer.ecs.component;

import com.artemis.Component;
import org.joml.Matrix4f;

/**
 * Created by CJ Burkey on 2019/01/12
 */
public final class Camera extends Component {
    
    public boolean perspective = false;
    public float orthographicSize = 10.0f;
    public float fovDegrees = 75.0f;
    public float nearPlane = 0.01f;
    public float farPlane = 20.0f;
    
    // Filled in by the system
    public final Matrix4f projectionMatrix = new Matrix4f();
    public final Matrix4f viewMatrix = new Matrix4f();
    
}
