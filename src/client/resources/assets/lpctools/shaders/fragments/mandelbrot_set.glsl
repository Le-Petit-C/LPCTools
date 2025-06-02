#version 400 core

in vec2 complex;

uniform int maxDepth;
uniform vec4 setColor;
uniform vec4 outColor;
uniform uvec4 shift;

out vec4 FragColor;


uvec2 add22(uvec2 v1, uvec2 v2){
    v1.x += v2.x;
    v1.y += v2.y;
    v1.y += (v1.x < v2.x) ? 1 : 0;
    return v1;
}

uvec2 usbu22(uvec2 v1, uvec2 v2){
    v1.x -= v2.x;
    v1.y -= v2.y;
    v1.y -= (v1.x > v2.x) ? 1 : 0;
    return v1;
}

uvec2 ssbu22(uvec2 v1, uvec2 v2){
    int h = int(v1.y);
    v1.x -= v2.x;
    h -= int(v2.y);
    h -= (v1.x > v2.x) ? 1 : 0;
    v1.y = h;
    return v1;
}

uvec2 uadd21(uvec2 v1, uint v2){
    v1.x += v2;
    v1.y += (v1.x < v2) ? 1 : 0;
    return v1;
}

uvec2 umul11(uint v1, uint v2){
    uvec2 v;
    uint a1, a2;
    v.x = (v1 & 0xffffu) * (v2 & 0xffffu);
    v.y = (v1 >> 16) * (v2 >> 16);
    a1 = (v1 & 0xffffu) * (v2 >> 16);
    a2 = (v1 >> 16) * (v2 & 0xffffu);
    v.y += (a1 >> 16) + (a2 >> 16);
    a1 <<= 16; a2 <<= 16;
    v.x += a1; v.y += (v.x < a1) ? 1 : 0;
    v.x += a2; v.y += (v.x < a2) ? 1 : 0;
    return v;
}

uvec2 sumul11(int v1, uint v2){
    uvec2 v;
    uint a1;
    int a2;
    v.x = (v1 & 0xffff) * (v2 & 0xffffu);
    v.y = (v1 >> 16) * (v2 >> 16);
    a1 = (v1 & 0xffff) * (v2 >> 16);
    a2 = (v1 >> 16) * int(v2 & 0xffffu);
    v.y += (a1 >> 16) + (a2 >> 16);
    a1 <<= 16; a2 <<= 16;
    v.x += a1; v.y += (v.x < a1) ? 1 : 0;
    v.x += a2; v.y += (v.x < a2) ? 1 : 0;
    return v;
}

uvec2 smul11(int v1, int v2){
    uvec2 v;
    int a1, a2;
    v.x = (v1 & 0xffff) * (v2 & 0xffff);
    v.y = (v1 >> 16) * (v2 >> 16);
    a1 = (v1 & 0xffff) * (v2 >> 16);
    a2 = (v1 >> 16) * (v2 & 0xffff);
    v.y += (a1 >> 16) + (a2 >> 16);
    a1 <<= 16; a2 <<= 16;
    v.x += a1; v.y += (v.x < a1) ? 1 : 0;
    v.x += a2; v.y += (v.x < a2) ? 1 : 0;
    return v;
}

uvec2 usquare1(uint v1){
    uvec2 v; uint a;
    v.x = (v1 & 0xffffu) * (v1 & 0xffffu);
    v.y = (v1 >> 16) * (v1 >> 16);
    a = (v1 & 0xffffu) * (v1 >> 16);
    v.y += a >> 15;
    a <<= 17;
    v.x += a;
    v.y += (v.x < a) ? 1 : 0;
    return v;
}

uvec2 ssquare1(int v1){
    uvec2 v; uint a;
    v.x = (v1 & 0xffff) * (v1 & 0xffff);
    v.y = (v1 >> 16) * (v1 >> 16);
    a = (v1 & 0xffff) * (uint(v1) >> 16);
    v.y += a >> 15;
    a <<= 17;
    v.x += a;
    v.y += (v.x < a) ? 1 : 0;
    v.y -= (v1 < 0) ? ((v1 & 0xffff) << 1) : 0;
    return v;
}

//smul定义后56位为小数
//定义0x00000000,0x00000000为0
//定义0x01000000,0x00000000为1
//定义0xff000000,0x00000000为-1

//视作smul((v1,0),(v2,0))
uvec2 xmulhh(int v1, int v2){
    uvec2 v = smul11(v1, v2);
    v.y = (v.x >> 24) | (v.y << 8);
    v.x <<= 8;
    return v;
}

//运算结果乘以2
uvec2 xmulhh_x2(int v1, int v2){
    uvec2 v = smul11(v1, v2);
    v.y = (v.x >> 23) | (v.y << 9);
    v.x <<= 9;
    return v;
}

uvec2 xsquarehh(int v1){
    uvec2 v = ssquare1(v1);
    v.y = (v.x >> 24) | (v.y << 8);
    v.x <<= 8;
    return v;
}

//视作smul((v1,0),(0,v2))
//实际与smul((0,v1),(v2,0))等价
uvec2 xmulhl(int v1, uint v2){
    uvec2 v = sumul11(v1, v2);
    v.x = (v.x >> 24) | (v.y << 8);
    v.y >>= 24;
    return v;
}

//运算结果乘以2
uvec2 xmulhl_x2(int v1, uint v2){
    uvec2 v = sumul11(v1, v2);
    v.x = (v.x >> 23) | (v.y << 9);
    v.y >>= 23;
    return v;
}

//视作smul((0,v1),(0,v2))
//仅返回低位
uint xmulll(uint v1, uint v2){
    //更低位的内容可能会影响到结果，但是忽略此误差(小于2的-60次方)
    return (v1 >> 16) * (v2 >> 16) >> 24;
}

//运算结果乘以2
uint xmulll_x2(uint v1, uint v2){
    return (v1 >> 16) * (v2 >> 16) >> 23;
}

uint xsquarell(uint v1){
    return (v1 >> 16) * (v1 >> 16) >> 24;
}

//全乘法
uvec2 xmul(uvec2 v1, uvec2 v2){
    //更低位的内容可能会影响到结果，但是忽略此误差(小于2的-59次方)
    uvec2 v = uadd21(xmulhh(int(v1.y), int(v2.y)),xmulll(v1.x, v2.x));
    uvec2 a = add22(xmulhl(int(v2.y), v1.x),xmulhl(int(v1.y), v2.x));
    return add22(v, a);
}

//运算结果乘以2
uvec2 xmul_x2(uvec2 v1, uvec2 v2){
    uvec2 v = uadd21(xmulhh_x2(int(v1.y), int(v2.y)),xmulll_x2(v1.x, v2.x));
    uvec2 a = add22(xmulhl_x2(int(v2.y), v1.x),xmulhl_x2(int(v1.y), v2.x));
    return add22(v, a);
}

uvec2 xsquare(uvec2 v1){
    uvec2 v = uadd21(xsquarehh(int(v1.y)), xsquarell(v1.x));
    uvec2 a = xmulhl_x2(int(v1.y), v1.x);
    return add22(v, a);
}

bool sgreater(uvec2 v1, uvec2 v2){
    return int(v1.y) > int(v2.y) || ((v1.y == v2.y) && (v1.x > v2.x));
}

//将一个uvec4视作两个(s)uvec2组成的复数进行运算
uvec4 cadd(uvec4 v1, uvec4 v2){
    return uvec4(add22(v1.xy, v2.xy), add22(v1.zw, v2.zw));
}

uvec4 csmul(uvec4 v1, uvec4 v2){
    return uvec4(add22(xmul(v1.xy, v2.xy), -xmul(v1.zw, v2.zw)), add22(xmul(v1.xy, v2.zw), xmul(v1.zw, v2.xy)));
}

uvec4 cssquare2(uvec4 v){
    return uvec4(add22(xmul(v.xy, v.xy), -xmul(v.zw, v.zw)), 2 * xmul(v.xy, v.zw));
}

uvec4 csuvec4fromvec2(vec2 c){
    c.x *= 0x01000000;
    c.y *= 0x01000000;
    uint x = uint(int(floor(c.x)));
    uint y = uint(int(floor(c.y)));
    c.x -= x; c.y -= y;
    return uvec4(uint(c.x * 0x10000 * 0x10000), x, uint(c.y * 0x10000 * 0x10000), y);
}

/*
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
    }
}
*/

uvec2 ushiftleft2(uvec2 v, int n){
    return uvec2(v.x << n, (v.y << n) | (v.x >> (32 - n)));
}

void main() {
    uvec4 c = csuvec4fromvec2(complex);
    c = cadd(c, shift);
    uvec4 z = c;
    uvec2 lim = uvec2(0, 0x04000000);
    int a;
    for(a = 0; a < maxDepth; ++a){
        uvec2 x2 = xsquare(z.xy), y2 = xsquare(z.zw);
        //if(sgreater(umul11(y2.y, y2.y), lim)) break;
        if(sgreater(add22(x2, y2), lim)) break;
        z = cadd(uvec4(ssbu22(x2, y2), xmul_x2(z.xy, z.zw)), c);
    }
    if(a >= maxDepth) FragColor = setColor;
    else {
        FragColor = outColor;
        FragColor.a *= log(float(a + 1)) / log(float(maxDepth));
    }
}
