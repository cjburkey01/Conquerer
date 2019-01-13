package com.cjburkey.conquerer.ecs.component.transform;

import com.artemis.PooledComponent;
import org.joml.Vector3f;

/**
 * Created by CJ Burkey on 2019/01/12
 */
public class Scale extends PooledComponent {
    
    public final Vector3f scale = new Vector3f();
    
    public Scale() {
        reset();
    }
    
    protected void reset() {
        scale.set(1.0f);
    }
    
}
