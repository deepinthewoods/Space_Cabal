#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec4 u_colors[128];

void main()
{
  vec4 DiffuseColor = texture2D(u_texture, v_texCoords);
  int index = int(DiffuseColor.r * 128.0);
  //if (DiffuseColor.r < .01 && DiffuseColor.g < .01 && DiffuseColor.b < .01) discard;
  
  //gl_FragColor = v_color * DiffuseColor;
  if (index == 0) discard;
  vec4 color = u_colors[index] * DiffuseColor.g;
  color.r *= DiffuseColor.b + 1.0;
  
  gl_FragColor = color;
  
}