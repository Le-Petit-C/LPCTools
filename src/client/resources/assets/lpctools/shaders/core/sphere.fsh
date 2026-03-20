#version 330

flat in vec3 center;
flat in float rSquare;
in vec3 pos;
in vec4 vertexColor;
in vec2 depthMul;
in vec2 depthBase;

out vec4 fragColor;

void main() {
    float posLenInv = 1.0 / length(pos);
    vec3 normalizedPos = pos * posLenInv;
    vec3 crossRes = cross(normalizedPos, center);
    float dSquare = dot(crossRes, crossRes);
    if(dSquare > rSquare) discard;
    float d = dot(normalizedPos, center);
    float l = sqrt(rSquare - dSquare);
    float s = d + ((gl_PrimitiveID & 1) != 0 ? l : -l);
    if(s < 0) discard;
    vec2 depthVec = depthMul * s * posLenInv + depthBase;
    float ndcDepth = depthVec.x / depthVec.y;
    gl_FragDepth = ndcDepth * 0.5 + 0.5;
    fragColor = vertexColor;
}
