package com.cjburkey.conquerer.ecs.component.world;

import com.artemis.PooledComponent;
import org.joml.Random;

import static com.cjburkey.conquerer.gen.Name.*;

/**
 * Created by CJ Burkey on 2019/01/15
 */
public class Named extends PooledComponent {
    
    public String name;
    
    protected void reset() {
        name = null;
    }
    
    public void randomName(Random random, int minLengthInc, int maxLengthInc) {
        name = generateName(random, minLengthInc, maxLengthInc);
    }
    
}
