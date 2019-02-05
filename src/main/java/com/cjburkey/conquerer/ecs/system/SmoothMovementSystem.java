package com.cjburkey.conquerer.ecs.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.cjburkey.conquerer.ecs.component.engine.Transform;
import com.cjburkey.conquerer.ecs.component.input.SmoothMovement;
import com.cjburkey.conquerer.util.Util;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings("unused")
public final class SmoothMovementSystem extends IteratingSystem {

    private ComponentMapper<Transform> mTransform;
    private ComponentMapper<SmoothMovement> mSmoothMovement;

    public SmoothMovementSystem() {
        super(Aspect.all(Transform.class, SmoothMovement.class));
    }

    protected void process(int entityId) {
        // Components
        Transform transform = mTransform.get(entityId);
        SmoothMovement smoothMovement = mSmoothMovement.get(entityId);

        float delta = world.getDelta();

        transform.position.set(Util.dampSpringCrit(smoothMovement.goalPosition, transform.position, smoothMovement.velocity, smoothMovement.smoothing, delta));
    }

}
