#version 330 core

uniform sampler2D sampler;
uniform vec4 color;

in vec2 uvPos;

out vec4 fragColor;

void main() {
    float text = texture(sampler, uvPos).x;
    fragColor = color * vec4(text, text, text, text);
}
