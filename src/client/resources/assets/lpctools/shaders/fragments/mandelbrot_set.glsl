#version 400 core

in vec2 complex;

uniform int maxDepth;
uniform vec4 setColor;
uniform vec4 outColor;

out vec4 FragColor;

void main() {
    vec2 z = complex;
    int a;
    for(a = 0; a < maxDepth; ++a){
        float x2 = z.x * z.x, y2 = z.y * z.y;
        if(x2 + y2 > 4) break;
        z = vec2(x2 - y2, 2 * z.x * z.y) + complex;
    }
    if(a >= maxDepth) FragColor = setColor;
    else {
        FragColor = outColor;
        FragColor.a *= log(float(a + 1)) / log(float(maxDepth));
        if(FragColor.a == 0) gl_FragDepth = 1;
    }
}
