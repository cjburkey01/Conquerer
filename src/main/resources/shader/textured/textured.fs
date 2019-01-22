#version 330 core

uniform sampler2D sampler;

in vec2 uvPos;

out vec4 fragColor;

void main() {
    float textureColor = texture(sampler, uvPos).x;
    fragColor = vec4(textureColor, textureColor, textureColor, 1.0);
}
