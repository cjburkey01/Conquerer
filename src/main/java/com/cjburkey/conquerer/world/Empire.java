package com.cjburkey.conquerer.world;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings("WeakerAccess")
public class Empire {
    
    public final String name;
    public final Vector3fc color;
    
    public Empire(String name, Vector3fc color) {
        this.name = name;
        this.color = new Vector3f(color);
    }
    
}
