#version 330 core

uniform vec4 setColor;
uniform vec4 outColor;
uniform int maxDepth;

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
        FragColor = setColor;
        gl_FragDepth = gl_FragCoord.z;
    }
    else {
        FragColor = outColor;
        FragColor.a *= log(float(a + 1)) / log(float(maxDepth));
        if(FragColor.a == 0) discard;
        else gl_FragDepth = gl_FragCoord.z;
    }
}
