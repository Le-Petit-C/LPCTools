#version 330 core

// 顶点属性输入
layout(location = 0) in vec3 aPos;     // 顶点位置
layout(location = 1) in vec4 aColor;   // 顶点颜色

// 统一变量（由CPU传递）
uniform mat4 modelViewMatrix;   // 模型视图矩阵
uniform mat4 projectionMatrix;  // 投影矩阵

// 输出到片段着色器
out vec4 vertexColor;            // 传递原始颜色

void main()
{
    // 应用模型视图变换
    vec4 viewSpacePos = modelViewMatrix * vec4(aPos, 1.0);

    // 应用投影变换
    gl_Position = projectionMatrix * viewSpacePos;

    // 直接传递顶点颜色
    vertexColor = aColor;
}