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

void main() {
    rSquare = LineWidth * LineWidth;
    vertexColor = Color;
    // MVP * vec4(camPos, 1) = vec4(0, 0, ?, 0)
    // MVP.xyw * vec4(camPos, 1) = vec3(0, 0, 0)
    // MVP[0,1,2].xyw * camPos = -MVP[3].xyw
    // camPos = inverse(MVP[0,1,2].xyw) * -MVP[3].xyw
    mat4 rawMVP = ProjMat * ModelViewMat;
    vec3 camOffset = inverse(mat3(rawMVP[0].xyw, rawMVP[1].xyw, rawMVP[2].xyw)) * rawMVP[3].xyw;
    center = Position + ModelOffset + camOffset;
    pos = center + shifts[gl_VertexID % 8] * LineWidth;

    mat4 MVPMatrix = rawMVP * mat4(vec4(1, 0, 0, 0), vec4(0, 1, 0, 0), vec4(0, 0, 1, 0), vec4(-camOffset, 1));
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
}
