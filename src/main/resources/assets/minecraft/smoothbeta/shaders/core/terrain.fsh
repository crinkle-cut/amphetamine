#version 120

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform int FogMode;

varying float vertexDistance;
varying vec2 texCoord0;
varying vec4 vertexColor;

void main() {
    vec4 color = texture2D(Sampler0, texCoord0) * vertexColor;
    gl_FragColor = fog(FogMode, color, vertexDistance, gl_Fog.density, gl_Fog.start, gl_Fog.end, gl_Fog.color.rgba);
}
