#version 120

uniform sampler2D texture;
uniform vec2 texelSize;

uniform vec3 color;

uniform float radius;
uniform float divider;
uniform float maxSample;

void main() {
    vec4 centerCol = texture2D(texture, gl_TexCoord[0].xy);

    if (centerCol.a != 0) {
        gl_FragColor = vec4(centerCol.rgb, 0);
    } else {
        float alpha = 0.0;
        float maxDistance = maxSample * divider;

        int r = int(radius + 0.5); // Rounding radius to nearest integer
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                if (x * x + y * y <= maxDistance) {
                    vec4 currentColor = texture2D(texture, gl_TexCoord[0].xy + vec2(texelSize.x * float(x), texelSize.y * float(y)));
                    if (currentColor.a != 0.0) {
                        float dist = distance(vec2(x, y), vec2(0.0));
                        alpha += divider > 0.0 ? max(0.0, (maxSample - dist) / divider) : 1.0;
                    }
                }
            }
        }
        gl_FragColor = vec4(color, alpha);
    }
}
