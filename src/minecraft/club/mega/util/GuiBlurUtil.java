package club.mega.util;

import club.mega.util.blur.BloomUtil;
import club.mega.util.blur.GaussianBlur;
import club.mega.util.blur.KawaseBlur;
import club.mega.util.shader.StencilUtil;
import net.minecraft.client.shader.Framebuffer;

import java.awt.*;

public class GuiBlurUtil {
    private static Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);



    public static void drawBlurredRect(float x, float y, float width, float height, float rectRadius, Color color) {
        String currentMode = "Kawase";
        StencilUtil.initStencilToWrite();

        RenderUtil.drawRoundedRect(x, y, width, height, rectRadius, Color.red);

        StencilUtil.readStencilBuffer(1);

        float radius = 17;
        int iterations = 15;
        int offset = 2;
        int shadowRadius = 6;
        int shadowOffset = 2;
        switch (currentMode) {
            case "Gaussian":
                GaussianBlur.renderBlur(radius);
                break;
            case "Kawase":
                KawaseBlur.renderBlur(iterations, offset);
                break;
        }

        StencilUtil.uninitStencilBuffer();

        bloomFramebuffer = RenderUtil.createFrameBuffer(bloomFramebuffer);

        bloomFramebuffer.framebufferClear();
        bloomFramebuffer.bindFramebuffer(true);
        RenderUtil.drawRoundedRect(x, y, width, height, rectRadius, Color.red);

        bloomFramebuffer.unbindFramebuffer();

        BloomUtil.renderBlur(bloomFramebuffer.framebufferTexture, shadowRadius, shadowOffset);
        RenderUtil.drawRoundedRect(x, y, width, height, rectRadius, color);
    }
}
