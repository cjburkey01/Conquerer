package com.cjburkey.conquerer.ecs.component.engine;

import com.artemis.PooledComponent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Created by CJ Burkey on 2019/02/04
 */
public class Transform extends PooledComponent {

    public final Vector3f position = new Vector3f();
    public final Quaternionf rotation = new Quaternionf();
    public final Vector3f scale = new Vector3f(1.0f);
    public Transform parent;

    protected void reset() {
        parent = null;
        position.zero();
        rotation.identity();
        scale.set(1.0f);
    }

}
