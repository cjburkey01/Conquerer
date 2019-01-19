package com.cjburkey.conquerer.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.cjburkey.conquerer.util.Util;
import com.cjburkey.conquerer.ecs.component.input.SmoothMovement;
import com.cjburkey.conquerer.ecs.component.transform.Pos;

/**
 * Created by CJ Burkey on 2019/01/12
 */
public class SmoothMovementSystem extends IteratingSystem {
    
    private ComponentMapper<Pos> mPos;
    private ComponentMapper<SmoothMovement> mSmoothMovement;
    
    public SmoothMovementSystem() {
        super(Aspect.all(Pos.class, SmoothMovement.class));
    }
    
    protected void process(int entityId) {
        // Components
        Pos pos = mPos.get(entityId);
        SmoothMovement smoothMovement = mSmoothMovement.get(entityId);
        
        float delta = world.getDelta();
        
        pos.position.set(Util.dampSpringCrit(smoothMovement.goalPosition, pos.position, smoothMovement.velocity, smoothMovement.smoothing, delta));
    }
    
}
