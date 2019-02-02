package com.cjburkey.conquerer.ecs.component.input;

import com.artemis.PooledComponent;
import org.joml.Vector3f;

/**
 * Created by CJ Burkey on 2019/01/15
 */
public final class SmoothMovement extends PooledComponent {

    public final Vector3f goalPosition = new Vector3f();
    public final Vector3f velocity = new Vector3f();

    public float smoothing = 0.1f;

    protected void reset() {
        goalPosition.zero();
        velocity.zero();

        smoothing = 0.1f;
    }

}
