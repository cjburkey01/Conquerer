#version 330 core

uniform bool isTextured;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec3 vertexColor;
layout (location = 2) in vec2 texPos;

out vec3 vertColor;
out vec2 uvPos;

void main() {
    gl_Position = projectionMatrix * modelMatrix * vec4(vertexPosition, 1.0);
    
    if (isTextured) {
        uvPos = texPos;
    } else {
        vertColor = vertexColor;
    }
}
