#version 330 core

#moj_import <minecraft:dynamictransforms.glsl>

layout(std140) uniform Mandelbrot {
    vec4 setColor;
    vec4 outColor;
    int maxDepth;
};

// 将texCoord0视为复平面上的点使用
in vec2 texCoord0;

out vec4 FragColor;

void main() {
    vec2 z = texCoord0;
    int a;
    for(a = 0; a < maxDepth; ++a){
        float x2 = z.x * z.x, y2 = z.y * z.y;
        if(x2 + y2 > 4) break;
        z = vec2(x2 - y2, 2 * z.x * z.y) + texCoord0;
    }
    if(a >= maxDepth) {
        FragColor = ColorModulator * setColor;
        gl_FragDepth = gl_FragCoord.z;
    }
    else {
        FragColor = ColorModulator * outColor;
        FragColor.a *= log(float(a + 1)) / log(float(maxDepth));
        if(FragColor.a == 0) discard;
        else gl_FragDepth = gl_FragCoord.z;
    }
}
