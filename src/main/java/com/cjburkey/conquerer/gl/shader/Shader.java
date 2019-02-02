package com.cjburkey.conquerer.gl.shader;

import com.cjburkey.conquerer.Conquerer;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.nio.FloatBuffer;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.joml.Vector4ic;
import org.lwjgl.system.MemoryStack;

import static com.cjburkey.conquerer.Log.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;

/**
 * Created by CJ Burkey on 2018/11/25
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Shader {

    private static int currentProgram = -1;

    private final Int2IntOpenHashMap shaders = new Int2IntOpenHashMap();
    private final Object2IntOpenHashMap<String> uniforms = new Object2IntOpenHashMap<>();
    private int program;
    private boolean done = false;

    protected boolean transformsProjection = false;
    protected boolean transformsView = false;
    protected boolean transformsModel = false;

    public Shader() {
        program = glCreateProgram();
        Conquerer.onExit.add(this::destroy);
    }

    public boolean getTransformsProjection() {
        return transformsProjection;
    }

    public boolean getTransformsView() {
        return transformsView;
    }

    public boolean getTransformsModel() {
        return transformsModel;
    }

    protected final void addShader(int type, String source) {
        if (done) throw new IllegalStateException("Cannot add shader to a finished shader program");
        if (source == null) throw new NullPointerException("Shader source was null");
        if (shaders.containsKey(type)) return;
        int shader = glCreateShader(type);
        if (shader <= 0) return;
        glShaderSource(shader, source);
        shaders.put(type, shader);
    }

    protected final void finish() {
        if (done) throw new IllegalStateException("Cannot finish a finished shader program");
        for (int shader : shaders.values()) {
            glCompileShader(shader);
            String info = glGetShaderInfoLog(shader).trim();
            if (!info.isEmpty()) {
                error("Failed to compile shader: {}", info);
                return;
            }
            glAttachShader(program, shader);
        }
        glLinkProgram(program);
        String info = glGetProgramInfoLog(program).trim();
        if (!info.isEmpty()) {
            error("Failed to link shader program: {}", info);
            return;
        }
        glValidateProgram(program);
        info = glGetProgramInfoLog(program).trim();
        if (!info.isEmpty()) warn("Failed to validate shader program: {}", info);
        for (int shader : shaders.values()) {
            glDetachShader(program, shader);
            glDeleteShader(shader);
        }
        shaders.clear();
        done = true;
    }

    public void destroy() {
        if (shaders.size() > 0) {
            for (int shader : shaders.values()) glDeleteShader(shader);
        }
        glDeleteProgram(program);
        done = false;
    }

    public void bind() {
        if (!done) throw new IllegalStateException("Cannot bind an unfinished shader program");
        if (isBound()) return;
        currentProgram = program;
        glUseProgram(program);
    }

    public boolean isBound() {
        return currentProgram == program;
    }

    // Uniforms

    public void setUniform(String name, float value) {
        int at = getUniformLocation(name);
        if (at >= 0) glUniform1f(at, value);
    }

    public void setUniform(String name, Vector2fc value) {
        int at = getUniformLocation(name);
        if (at >= 0) glUniform2f(at, value.x(), value.y());
    }

    public void setUniform(String name, Vector3fc value) {
        int at = getUniformLocation(name);
        if (at >= 0) glUniform3f(at, value.x(), value.y(), value.z());
    }

    public void setUniform(String name, Vector4fc value) {
        int at = getUniformLocation(name);
        if (at >= 0) glUniform4f(at, value.x(), value.y(), value.z(), value.w());
    }

    public void setUniform(String name, boolean value) {
        setUniform(name, value ? 1 : 0);
    }

    public void setUniform(String name, int value) {
        int at = getUniformLocation(name);
        if (at >= 0) glUniform1i(at, value);
    }

    public void setUniform(String name, Vector2ic value) {
        int at = getUniformLocation(name);
        if (at >= 0) glUniform2i(at, value.x(), value.y());
    }

    public void setUniform(String name, Vector3ic value) {
        int at = getUniformLocation(name);
        if (at >= 0) glUniform3i(at, value.x(), value.y(), value.z());
    }

    public void setUniform(String name, Vector4ic value) {
        int at = getUniformLocation(name);
        if (at >= 0) glUniform4i(at, value.x(), value.y(), value.z(), value.w());
    }

    public void setUniform(String name, Matrix3fc value) {
        int at = getUniformLocation(name);
        if (at < 0) return;
        try (MemoryStack stack = stackPush()) {
            FloatBuffer buff = stack.mallocFloat(9);
            glUniformMatrix3fv(at, false, value.get(buff));
        }
    }

    public void setUniform(String name, Matrix4fc value) {
        int at = getUniformLocation(name);
        if (at < 0) return;
        try (MemoryStack stack = stackPush()) {
            FloatBuffer buff = stack.mallocFloat(16);
            glUniformMatrix4fv(at, false, value.get(buff));
        }
    }

    private int getUniformLocation(String name) {
        bind();
        if (uniforms.containsKey(name)) return uniforms.getInt(name);
        int at = glGetUniformLocation(program, name);
        uniforms.put(name, at);
        if (at < 0) error("Uniform \"{}\" could not be found", name);
        return at;
    }

}
