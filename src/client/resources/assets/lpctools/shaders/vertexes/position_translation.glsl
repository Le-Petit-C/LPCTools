#version 330 core

// 顶点属性输入
layout(location = 0) in vec3 aPos;     // 顶点位置

// 统一变量（由CPU传递）
uniform mat4 matrix;   // 最终视图矩阵（投影矩阵*模型矩阵）

void main(){
    gl_Position = matrix * vec4(aPos, 1.0);
}