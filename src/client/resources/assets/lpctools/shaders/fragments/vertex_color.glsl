#version 330 core

// 从顶点着色器输入
in vec4 vertexColor;

// 最终输出颜色
out vec4 FragColor;

void main()
{
    // 直接输出原始颜色
    FragColor = vertexColor;
}