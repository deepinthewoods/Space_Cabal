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
uniform float alpha;
uniform float u_time;

vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float getsat(vec3 c)
{
    float mi = min(min(c.x, c.y), c.z);
    float ma = max(max(c.x, c.y), c.z);
    return (ma - mi)/(ma+ 1e-7);
}
#define DSP_STR 1.5

vec3 iLerp(in vec3 a, in vec3 b, in float x)
{
    //Interpolated base color (with singularity fix)
    vec3 ic = mix(a, b, x) + vec3(1e-6,0.,0.);

    //Saturation difference from ideal scenario
    float sd = abs(getsat(ic) - mix(getsat(a), getsat(b), x));

    //Displacement direction
    vec3 dir = normalize(vec3(2.*ic.x - ic.y - ic.z, 2.*ic.y - ic.x - ic.z, 2.*ic.z - ic.y - ic.x));
    //Simple Lighntess
    float lgt = dot(vec3(1.0), ic);

    //Extra scaling factor for the displacement
    float ff = dot(dir, normalize(ic));

    //Displace the color
    ic += DSP_STR*dir*sd*ff*lgt;
    return clamp(ic,0.,1.);
}

vec3 lerpHSV(in vec3 a, in vec3 b, in float x)
{
    float hue = (mod(mod((b.x-a.x), 1.) + 1.5, 1.)-0.5)*x + a.x;
    return vec3(hue, mix(a.yz, b.yz, x));
}
#define HALF_PI 1.5707963267948966
#define PI 3.141592653589793
float rand(vec2 co)
{
    float a = 12.9898;
    float b = 78.233;
    float c = 43758.5453;
    float dt= dot(co.xy ,vec2(a,b));
    float sn= mod(dt,3.14);
    return fract(sin(sn) * c);
}

float sineInOut(float t) {
  return -0.5 * (cos(PI * t) - 1.0);
}
float elasticInOut(float t) {
  return t < 0.5
    ? 0.5 * sin(+13.0 * HALF_PI * 2.0 * t) * pow(2.0, 10.0 * (2.0 * t - 1.0))
    : 0.5 * sin(-13.0 * HALF_PI * ((2.0 * t - 1.0) + 1.0)) * pow(2.0, -10.0 * (2.0 * t - 1.0)) + 1.0;
}


void main()
{
  vec4 DiffuseColor = texture2D(u_texture, v_texCoords);

  int index = int(v_texCoords.x * 128.0);
  //if (DiffuseColor.r < .01 && DiffuseColor.g < .01 && DiffuseColor.b < .01) discard;

//  alpha = elasticInOut(alpha);
  //gl_FragColor = v_color * DiffuseColor;
  //if (index == 0) discard;
  vec4 color = u_colors[index];// * DiffuseColor.g;
  if (index != 4 && index != 5 && index != 10){
  	  	  		  vec3 c = rgb2hsv(color.xyz);
  	  	  		  c.y = .5f ;
  	  	  		  color = vec4(hsv2rgb(c), color.a);
  	  	  	  }
  if (index < 16){
	  float alpha = (v_texCoords.y) * 2.0;
	  	    if (alpha > 1.0) alpha = 2.0 - alpha;
	  	    alpha = 1.0 - alpha;

  } else if (index < 32){
	  //alpha *= alpha;
	  //float alphar = abs(sin(v_texCoords.y * PI * 22.0));
	  float alpha = (rand(vec2(v_texCoords * 23.0))) ;
	  alpha = max(0.0, alpha * 2 - 1.0);
	  alpha = sin(v_texCoords.y * PI * 13.0 ) * 0.5 + 0.5;

	  //float alphar = rand(v_texCoords);
	  //alphar = mod(v_texCoords.y * 8.0, 2.0) * 0.5;
	  float alphar = sin(v_texCoords.y * PI * 6) * 0.5 + 0.5;

	  vec4 rc = vec4(1.0, 0.0, 0.0, 1.0);
	  rc =  mix(rc, vec4(1.0, 1.0, 0.0, 1.0), alphar);

	  color = mix(rc, color, alpha);


  } else if (index < 48){
	  float alpha = (v_texCoords.y) * 2.0;
	    if (alpha > 1.0) alpha = 2.0 - alpha;
	    alpha = 1.0 - alpha;
	  if (index != 4){
	  		  vec3 c = rgb2hsv(color.xyz);
	  		  //c.y *= ( (alpha * 0.75) + 0.25);
	  		  c.y *= alpha;
	  		  c.z = max(c.z, 1.0 - alpha);
	  		  color = vec4(hsv2rgb(c), color.a);
	  	  }else {
	  		 // color = vec4(1.0, 1.0, 1.0, 1.0);

	  	  }

  }else if (index < 64){
	  //alpha *= alpha;
	  //float alphar = abs(sin(v_texCoords.y * PI * 22.0));
	  float alpha = (rand(vec2(v_texCoords * 32.0))) ;
	  alpha = max(0.0, alpha * 2 - 1.0);
	  alpha = sin(v_texCoords.y * PI * 16.0 ) * 0.5 + 0.5;
      alpha = mod(v_texCoords.y * 2, 1.0);
	  //float alphar = rand(v_texCoords);
	  //alphar = mod(v_texCoords.y * 8.0, 2.0) * 0.5;
	  float alphar = sin(v_texCoords.y * PI * 1) * 0.5 + 0.5;

	  vec4 rc = vec4(1.0, 0.0, 1.0, 1.0);
	  rc =  mix(rc, vec4(1.0, 0.0, 1.0, 1.0), alphar);

	  color = mix(rc, color, floor(alpha * 2));


  }

  //color.a = 1.0;
  //color.r += v_texCoords.x;
  //color.r *= DiffuseColor.b + 1.0;
  
  //color.a *= min(1.0, int(DiffuseColor.r * 128));

  gl_FragColor = color;
  
}
