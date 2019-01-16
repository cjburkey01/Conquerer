package com.cjburkey.conquerer.math;

import com.cjburkey.conquerer.Conquerer;
import com.cjburkey.conquerer.glfw.Window;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import static org.joml.Math.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings("WeakerAccess")
public final class Transformation {
    
    private static final float DEG2RAD = (float) PI / 180.0f;
    
    private static final Matrix4f projectionMatrix = new Matrix4f();
    private static final Matrix4f viewMatrix = new Matrix4f();
    private static final Matrix4f modelMatrix = new Matrix4f();
    private static final Matrix4f finalMatrix = new Matrix4f();
    
    public static Matrix4fc getProjectionMatrix(float fovDeg, float aspect, float near, float far) {
        return projectionMatrix
                .identity()
                .setPerspective(fovDeg * DEG2RAD, aspect, near, far);
    }
    
    public static Matrix4fc getProjectionMatrix(float fovDeg, float screenWidth, float screenHeight, float near, float far) {
        return getProjectionMatrix(fovDeg, screenWidth / screenHeight, near, far);
    }
    
    public static Matrix4fc getOrthographicMatrix(float left, float right, float bottom, float top, float near, float far) {
        return projectionMatrix
                .identity()
                .setOrtho(left, right, bottom, top, near, far);
    }
    
    public static Matrix4fc getOrthographicMatrix(float screenWidth, float screenHeight, float size, float near, float far) {
        float aspectX = screenWidth / screenHeight * size;
        return getOrthographicMatrix(-aspectX, aspectX, -size, size, near, far);
    }
    
    public static Matrix4fc getViewMatrix(Vector3fc cameraPosition, Quaternionfc cameraRotation) {
        return viewMatrix
                .identity()
                .rotate(cameraRotation)
                .translate(cameraPosition.mul(-1.0f, new Vector3f()));
    }
    
    public static Matrix4fc getModelMatrix(Vector3fc modelPosition, Quaternionfc modelRotation, Vector3fc modelScale) {
        return modelMatrix
                .identity()
                .translate(modelPosition)
                .rotate(modelRotation)
                .scale(modelScale);
    }
    
    public static Matrix4fc getCompleteMatrix(Matrix4fc projectionViewMatrix, Matrix4fc modelMatrix) {
        return projectionViewMatrix.mul(modelMatrix, finalMatrix);
    }
    
    public static Vector3fc transformPoint(Quaternionfc rotation, Vector3fc point) {
        return transformMatPoint(rotation.get(new Matrix4f()), point);
    }
    
    public static Vector3fc transformDir(Quaternionfc rotation, Vector3fc direction) {
        return transformMatDirection(rotation.get(new Matrix4f()), direction);
    }
    
    public static Vector3fc transformMatPoint(Matrix4fc mat, Vector3fc point) {
        return mat
                .invert(new Matrix4f())
                .transformPosition(point, new Vector3f());
    }
    
    public static Vector3fc transformMatDirection(Matrix4fc mat, Vector3fc direction) {
        return mat
                .invert(new Matrix4f())
                .transformDirection(direction, new Vector3f());
    }
    
    public static Vector3f screenToGlPos(Vector2fc screenCoords) {
        Window w = Conquerer.INSTANCE.window();
        return new Vector3f(screenCoords.x() / w.getWidth() * 2.0f - 1.0f, 1.0f - screenCoords.y() / w.getHeight() * 2.0f, 0.0f);
    }
    
    public static Vector3f glPosToWorldRay(Matrix4fc projectionMatrix, Matrix4fc viewMatrix, Vector3fc glScreenPos) {
        // We need to enter homogeneous coordinates, so w is set to 1.
        Vector4f ray = projectionMatrix
                .invert(new Matrix4f())
                .transform(new Vector4f(glScreenPos.x(), glScreenPos.y(), -1.0f, 1.0f));
        
        // We are working with a vector, so the w component can be set to 0
        ray.z = -1.0f;
        ray.w = 0.0f;
        viewMatrix
                .invert(new Matrix4f())
                .transform(ray);
        return new Vector3f(ray.x, ray.y, ray.z).normalize();
    }
    
    public static Vector3f screenToWorldRay(Matrix4fc projectionMatrix, Matrix4fc viewMatrix, Vector2fc screenCoords) {
        return glPosToWorldRay(projectionMatrix, viewMatrix, screenToGlPos(screenCoords));
    }
    
    public static Vector3f screenToPlane(Vector3fc position, Matrix4fc projectionMatrix, Matrix4fc viewMatrix, Vector2fc screenCoords, Plane plane) {
        return plane.getIntersectionPoint(position, screenToWorldRay(projectionMatrix, viewMatrix, screenCoords));
    }
    
}
