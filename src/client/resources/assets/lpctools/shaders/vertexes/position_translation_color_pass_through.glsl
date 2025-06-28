#version 330 core

layout(location = 0) in vec3 aPos;     // 顶点位置
layout(location = 1) in vec4 aColor;   // 顶点颜色

uniform mat4 matrix;

// 输出到片段着色器
out vec4 vertexColor;            // 传递原始颜色

void main(){
    gl_Position = matrix * vec4(aPos, 1);
    vertexColor = aColor;
}