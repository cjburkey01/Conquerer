package com.cjburkey.conquerer.ecs.component.world;

import com.artemis.PooledComponent;
import org.joml.Vector2fc;

/**
 * Created by CJ Burkey on 2019/01/15
 */
public class Location extends PooledComponent {
    
    public Vector2fc location;
    
    protected void reset() {
        location = null;
    }
    
}
