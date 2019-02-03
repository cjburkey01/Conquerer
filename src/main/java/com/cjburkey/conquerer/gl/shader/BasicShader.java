package com.cjburkey.conquerer.gl.shader;

import com.cjburkey.conquerer.util.Util;

import static org.lwjgl.opengl.GL20.*;

/**
 * Created by CJ Burkey on 2018/12/05
 */
public class BasicShader extends Shader {

    public BasicShader(String name, boolean transformsProjection, boolean transformsView, boolean transformsModel) {
        addShader(GL_VERTEX_SHADER, Util.readResource("shader/" + name + ".vs").orElse(null));
        addShader(GL_FRAGMENT_SHADER, Util.readResource("shader/" + name + ".fs").orElse(null));
        finish();

        this.transformsProjection = transformsProjection;
        this.transformsView = transformsView;
        this.transformsModel = transformsModel;
    }

}
