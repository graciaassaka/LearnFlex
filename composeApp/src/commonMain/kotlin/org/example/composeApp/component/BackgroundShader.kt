package org.example.composeApp.component

import org.intellij.lang.annotations.Language

/**
 * Background shader that creates a gradient effect based on the distance from the top of the screen.
 */
@Language("AGSL")
val BACKGROUND_SHADER = """
    uniform float time;
    uniform float2 resolution;
    layout(color) uniform half4 color;
    layout(color) uniform half4 color2;
    
    half4 main(in float2 fragCoord) {
        float2 uv = fragCoord/resolution.xy;
        float mixValue = distance(uv, vec2(0, 1)) +  abs(sin(time * 0.5));
        return mix(color, color2, mixValue);
    }
""".trimIndent()