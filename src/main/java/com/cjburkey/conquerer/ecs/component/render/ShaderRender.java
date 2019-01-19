package com.cjburkey.conquerer.ecs.component.render;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.cjburkey.conquerer.gl.shader.Shader;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@PooledWeaver
public class ShaderRender extends Component {
    
    public Shader shader;
    
}
