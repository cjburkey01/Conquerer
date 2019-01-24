package com.cjburkey.conquerer.ecs.component.render;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.cjburkey.conquerer.gl.Texture;

/**
 * Created by CJ Burkey on 2019/01/24
 */
@PooledWeaver
public final class Textured extends Component {
    
    public Texture texture;
    
}
