package com.cjburkey.conquerer.ecs.component.transform;

import com.artemis.PooledComponent;
import org.joml.Vector3f;

/**
 * Created by CJ Burkey on 2019/01/12
 */
public final class Pos extends PooledComponent {
    
    public final Vector3f position = new Vector3f();
    
    public Pos() {
        reset();
    }
    
    protected void reset() {
        position.zero();
    }
    
}
