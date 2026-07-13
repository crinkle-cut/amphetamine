#version 120

#moj_import <fog.glsl>

attribute vec3 Position;
attribute vec2 UV0;
attribute vec4 Color;

uniform vec3 ChunkOffset;

varying float vertexDistance;
varying vec2 texCoord0;
varying vec4 vertexColor;

void main() {
    vec3 pos = Position + ChunkOffset;
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * vec4(pos, 1.0);

    vertexDistance = fog_distance(gl_ModelViewMatrix, pos, 0);
    texCoord0 = UV0;
    vertexColor = Color;
}
