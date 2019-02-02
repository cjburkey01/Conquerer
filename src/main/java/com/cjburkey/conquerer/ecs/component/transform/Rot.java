package com.cjburkey.conquerer.ecs.component.transform;

import com.artemis.PooledComponent;
import org.joml.Quaternionf;

/**
 * Created by CJ Burkey on 2019/01/12
 */
public final class Rot extends PooledComponent {

    public final Quaternionf rotation = new Quaternionf();

    public Rot() {
        reset();
    }

    protected void reset() {
        rotation.identity();
    }

}
