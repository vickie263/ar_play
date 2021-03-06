/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES u_Texture;

uniform vec4 u_LightingParameters;
uniform vec4 u_MaterialParameters;
uniform vec4 u_ColorCorrectionParameters;
uniform vec4 u_ColorTintParameters;
uniform vec4 ourColor;

varying vec3 v_ViewPosition;
//varying vec3 v_ViewNormal;
varying vec2 v_TexCoord;


void main() {
    // We support approximate sRGB gamma.
    const float kGamma = 0.4545454;
    const float kInverseGamma = 2.2;

    // Unpack lighting and material parameters for better naming.
    vec3 viewLightDirection = u_LightingParameters.xyz;
    vec3 colorShift = u_ColorCorrectionParameters.rgb;
    float lightIntensity = u_ColorCorrectionParameters.a;

    float materialAmbient = u_MaterialParameters.x;
    float materialDiffuse = u_MaterialParameters.y;
    float materialSpecular = u_MaterialParameters.z;
    float materialSpecularPower = u_MaterialParameters.w;

    // Normalize varying parameters, because they are linearly interpolated in the vertex shader.
 //   vec3 viewFragmentDirection = normalize(v_ViewPosition);
   // vec3 viewNormal = normalize(v_ViewNormal);

    // Apply inverse SRGB gamma to the texture before making lighting calculations.
    // Flip the y-texture coordinate to address the texture from top-left.
  //  vec4 objectColor = texture2D(u_Texture, vec2(v_TexCoord.x, 1.0 - v_TexCoord.y));
  //  objectColor.rgb += u_ColorTintParameters.rgb;
  //  objectColor.rgb = pow(objectColor.rgb, vec3(kInverseGamma));

    // Ambient light is unaffected by the light intensity.
   // float ambient = materialAmbient;

    // Approximate a hemisphere light (not a harsh directional light).
  //  float diffuse = lightIntensity * materialDiffuse *
    //        0.5 * (dot(viewNormal, viewLightDirection) + 1.0);

    // Compute specular light.
  //  vec3 reflectedLightDirection = reflect(viewLightDirection, viewNormal);
 //   float specularStrength = max(0.0, dot(viewFragmentDirection, reflectedLightDirection));
  //  float specular = lightIntensity * materialSpecular *
   //         pow(specularStrength, materialSpecularPower);

    // Apply SRGB gamma before writing the fragment color.
    //gl_FragColor.a = objectColor.a * u_ColorTintParameters.a;
   // vec3 color = pow(objectColor.rgb * (ambient + diffuse) + specular, vec3(kGamma));
    //gl_FragColor.rgb = color * colorShift;
    gl_FragColor = texture2D(u_Texture, vec2(v_TexCoord.x, 1.0 - v_TexCoord.y));
    //gl_FragColor = ourColor;
}
