#version 400 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec2 complexIn;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

out vec2 complex;

void main() {
    vec4 viewSpacePos = modelViewMatrix * vec4(aPos, 1.0);
    gl_Position = projectionMatrix * viewSpacePos;
    complex = complexIn;
}