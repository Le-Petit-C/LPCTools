#version 400 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec2 complexIn;

uniform mat4 matrix;

out vec2 complex;

void main() {
    gl_Position = matrix * vec4(aPos, 1.0);
    complex = complexIn;
}