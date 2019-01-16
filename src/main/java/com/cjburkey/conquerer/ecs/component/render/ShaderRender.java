package com.cjburkey.conquerer.ecs.component.render;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.cjburkey.conquerer.gl.shader.Shader;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@PooledWeaver
public class ShaderRender extends Component {
    
    public Shader shader;
    public Vector3fc color = new Vector3f(1.0f, 1.0f, 1.0f);
    
}
