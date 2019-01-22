#version 330 core

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

layout (location = 0) in vec3 vertexPosition;
layout (location = 2) in vec2 texPos;

out vec2 uvPos;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(vertexPosition, 1.0);
    
    uvPos = texPos;
}
