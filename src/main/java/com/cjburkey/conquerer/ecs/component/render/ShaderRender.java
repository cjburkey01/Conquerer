package com.cjburkey.conquerer.ecs.component.render;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.cjburkey.conquerer.gl.shader.Shader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@PooledWeaver
public final class ShaderRender extends Component {

    public Shader shader;
    public Object2ObjectOpenHashMap<String, ShaderCallback> uniformCallbacks = new Object2ObjectOpenHashMap<>();

    @FunctionalInterface
    public interface ShaderCallback {

        void onCall(Shader shader);

    }

}
