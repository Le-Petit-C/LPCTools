#version 150 core

uniform sampler2D uInputTexture;
uniform float uTime;

in vec2 vTexCoord;
out vec4 fragColor;

void main() {
    vec2 uv = vTexCoord;
    uv.x += sin(uv.y * 10.0 + uTime) * 0.02;
    uv.y += cos(uv.x * 10.0 + uTime) * 0.02;
    fragColor = texture(uInputTexture, uv);
}