#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_index_texture;
uniform float u_time;
uniform vec4 u_colors[128];

float rand(vec2 co)
{
    float a = 12.9898;
    float b = 78.233;
    float c = 43758.5453;
    float dt= dot(co.xy ,vec2(a,b));
    float sn= mod(dt,3.14);
    return fract(sin(sn) * c);
}


void main()
{
  vec4 DiffuseColor = texture2D(u_texture, v_texCoords);
  int index = int(DiffuseColor.r);
  //if (DiffuseColor.r < .01 && DiffuseColor.g < .01 && DiffuseColor.b < .01) discard;
  float shift = (floor(v_texCoords.x * 64.0) / 64.0) + (floor(v_texCoords.y * 64.0) / 64.0);;


  float v = rand(floor((v_texCoords.xy ) * 64.0) + mod(floor(u_time * 8.0), 8000.0)) * 0.139;
  v = v * v_color.a;
  vec4 color = texture2D(u_index_texture, vec2(DiffuseColor.r, mod(v + u_time + shift, 1.0)));
  //gl_FragColor = v_color * DiffuseColor;
  //if (index == 0) discard;
  //color = u_colors[index]
  color.r *= DiffuseColor.b + 1.0;
  color.b *= DiffuseColor.b + 1.0;
  color.rgb *= DiffuseColor.g;
  //color += vec4(v, 0.0, 0.0, 1.0);
  //color *= v;

  gl_FragColor = color;
  
}
