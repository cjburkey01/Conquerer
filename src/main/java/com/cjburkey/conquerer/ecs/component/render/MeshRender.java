package com.cjburkey.conquerer.ecs.component.render;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.cjburkey.conquerer.gl.Mesh;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@PooledWeaver
public final class MeshRender extends Component {

    public Mesh mesh;

}
