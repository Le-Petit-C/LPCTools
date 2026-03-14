#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
// 使用lineWidth表示半径
in float LineWidth;

flat out vec3 center;
flat out float rSquare;
out vec3 pos;
out vec4 vertexColor;
out vec2 depthMul;
out vec2 depthBase;

const vec3 shifts[8] = vec3[](
    vec3(-1, -1, -1),
    vec3( 1, -1, -1),
    vec3(-1,  1, -1),
    vec3( 1,  1, -1),
    vec3(-1, -1,  1),
    vec3( 1, -1,  1),
    vec3(-1,  1,  1),
    vec3( 1,  1,  1)
);

/*
void main() {
    rSquare = LineWidth * LineWidth;
    // ModelViewMat * vec4(camPos, 1) = vec4(0, 0, 0, ?)
    // ModelViewMat.xyz * vec4(camPos, 1) = vec3(0, 0, 0)
    // ModelViewMat[0,1,2].xyz * camPos = -ModelViewMat[3].xyz
    // camPos = inverse(ModelViewMat[0,1,2].xyz) * -ModelViewMat[3].xyz
    // mat4 rawMVPMatrix = ProjMat * ModelViewMat;
    mat4 rawMat = mat4(vec4(1, 0, 0, 0), vec4(0, 1, 0, 0), vec4(0, 0, 1, 0), vec4(ProjMat[3].xyz, 1)) * ModelViewMat;
    vec3 camPos = inverse(mat3(rawMat[0].xyz, rawMat[1].xyz, rawMat[2].xyz)) * -rawMat[3].xyz;
    center = Position + ModelOffset - camPos;
    pos = center + shifts[gl_VertexID % 8] * LineWidth;

    // 去除了平移分量的MVPMatrix
    mat4 MVPMatrix = ProjMat * ModelViewMat * mat4(vec4(1, 0, 0, 0), vec4(0, 1, 0, 0), vec4(0, 0, 1, 0), vec4(camPos, 1));
    gl_Position = MVPMatrix * vec4(pos, 1);
    // newDepth = newPosition.z / newPosition.w
    // newPosition = MVPMatrix * vec4(newPos, 1)
    //             = MVPMatrix * vec4(pos * k, 1)
    //             = MVPMatrix[0,1,2] * pos * k + MVPMatrix[3]
    // newPosition.zw = MVPMatrix[0,1,2].zw * pos * k + MVPMatrix[3].zw
    //                = (MVPMatrix[0].zw * pos.x + MVPMatrix[1].zw * pos.y + MVPMatrix[2].zw * pos.z) * k + MVPMatrix[3].zw
    // 令 depthMul = MVPMatrix[0].zw * pos.x + MVPMatrix[1].zw * pos.y + MVPMatrix[2].zw * pos.z
    // 令 depthBase = MVPMatrix[3].zw
    // 则可以容易计算 newPosition.zw = k * depthMul + depthBase
    depthMul = MVPMatrix[0].zw * pos.x + MVPMatrix[1].zw * pos.y + MVPMatrix[2].zw * pos.z;
    depthBase = MVPMatrix[3].zw;
    vertexColor = Color;
}
*/

//假设camPos=0
void main() {
    rSquare = LineWidth * LineWidth;
    center = Position + ModelOffset;
    pos = center + shifts[gl_VertexID % 8] * LineWidth;
    mat4 MVPMatrix = ProjMat * ModelViewMat;
    gl_Position = MVPMatrix * vec4(pos, 1);
    depthMul = MVPMatrix[0].zw * pos.x + MVPMatrix[1].zw * pos.y + MVPMatrix[2].zw * pos.z;
    depthBase = MVPMatrix[3].zw;
    vertexColor = Color;
}
