#version 330 core

uniform bool isTextured;
uniform bool isFont;
uniform sampler2D sampler;
uniform vec4 colorize;

in vec3 vertColor;
in vec2 uvPos;

out vec4 fragColor;

void main() {
    if (isTextured) {
        vec4 texCol = texture(sampler, uvPos);
        if (isFont) {
            float col = texCol.x;
            texCol = vec4(col, col, col, col);
        }
        fragColor = colorize * texCol;
    } else {
        fragColor = vec4(vertColor, 1.0);
    }
}
