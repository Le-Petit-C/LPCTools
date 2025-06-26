#version 400

layout(location = 0) in vec3 blockCenter;
layout(location = 1) in vec3 vertexPos;

uniform mat4 matrix;
uniform mat4 modelMatrix;
uniform float distanceSquared;

const vec4 outerRange = vec4(0.0f, 0.0f, 2.0f, 1.0f);

void main() {
    vec4 centerPos = modelMatrix * vec4(blockCenter, 1.0f);
    vec4 result = matrix * vec4(blockCenter + vertexPos, 1.0f);
    float distSq = dot(centerPos, centerPos);
    float blendFactor = float(distSq > distanceSquared);
    gl_Position = mix(result, outerRange, blendFactor);
}
