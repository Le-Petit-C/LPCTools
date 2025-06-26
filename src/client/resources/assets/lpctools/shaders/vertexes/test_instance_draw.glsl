#version 400

// 顶点属性输入
layout(location = 0) in vec3 aPos;      //正三角形中心位置
layout(location = 1) in float angle;    //旋转角度
layout(location = 2) in vec4 color;

flat out vec4 vertexColor;

uniform mat4 matrix;
uniform float timeAngle;

void main() {
    int id = gl_VertexID % 3;
    float cangle = angle + (id - 1) * 2.094395f + timeAngle;
    float dx = cos(cangle);
    float dz = sin(cangle);
    gl_Position = matrix * vec4(aPos.x + dx, aPos.y, aPos.z + dz, 1.0f);
    vertexColor = color;
}
