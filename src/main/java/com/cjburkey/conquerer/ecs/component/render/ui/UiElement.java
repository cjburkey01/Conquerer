package com.cjburkey.conquerer.ecs.component.render.ui;

import com.artemis.Component;
import com.cjburkey.conquerer.gl.Texture;
import com.cjburkey.conquerer.ui.UiComponent;
import org.joml.Vector4f;

/**
 * Created by CJ Burkey on 2019/01/27
 */
public class UiElement extends Component {

    public final Vector4f colorize = new Vector4f(1.0f);
    public Texture texture;
    public boolean isFont;
    public UiComponent uiComponent;

}
