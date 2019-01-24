#version 330 core

uniform sampler2D sampler;

in vec2 uvPos;

out vec4 fragColor;

void main() {
    fragColor = texture(sampler, uvPos);
}
