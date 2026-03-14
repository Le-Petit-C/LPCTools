#version 330

flat in vec3 center;
flat in float rSquare;
in vec3 pos;
in vec4 vertexColor;
in vec2 depthMul;
in vec2 depthBase;

out vec4 fragColor;

void main() {
    /*
        现在问题变了
        现在是注释掉之后能在预期的位置画出预期大小的正方体，深度正确
        取消注释后能在预期位置画出球体，但是深度不正确，在它面前的方块不一定能挡住它
        能帮我分析一下是为什么吗
    */
    float posLenInv = 1.0 / length(pos);
    vec3 normalizedPos = pos * posLenInv;
    vec3 crossRes = cross(normalizedPos, center);
    float dSquare = dot(crossRes, crossRes);
    if(dSquare > rSquare) discard;
    float d = dot(normalizedPos, center);
    float l = sqrt(rSquare - dSquare);
    float s = d + ((gl_PrimitiveID & 1) != 0 ? l : -l);
    vec2 depthVec = depthMul * s * posLenInv + depthBase;
    float ndcDepth = depthVec.x / depthVec.y;
    gl_FragDepth = ndcDepth * 0.5 + 0.5;
    fragColor = vertexColor;
}
