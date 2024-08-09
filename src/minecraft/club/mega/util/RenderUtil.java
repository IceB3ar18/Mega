package club.mega.util;

import club.mega.gui.altmanager.alt.Alt;
import club.mega.interfaces.MinecraftInterface;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.UUID;

import static org.lwjgl.opengl.GL11.*;

public final class RenderUtil implements MinecraftInterface {

    private static final Frustum frustum = new Frustum();


    public static void drawRect(final double x, final double y, final double width, final double height, final Color color) {
        Gui.drawRect(x, y, x + width, y + height, color.getRGB());
    }

    public static void drawRect(final double x, final double y, final double width, final double height, final int color) {
        Gui.drawRect(x, y, x + width, y + height, color);
    }

    public static void drawGradientRect(final double x, final double y, final double width, final double height, final Color startColor, final Color endColor) {
        Gui.drawGradientRect(x, y, x + width, y + height, startColor.getRGB(), endColor.getRGB());
    }

    public static void drawGradientRect(final double x, final double y, final double width, final double height, final int startColor, final int endColor) {
        Gui.drawGradientRect(x, y, x + width, y + height, startColor, endColor);
    }

    public static void drawRoundedRect(double x, double y, double width, double height, double radius, Color color) {
        radius = radius * 2;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        double x1 = x + width;
        double y1 = y + height;
        float alpha = color.getAlpha() / 255.0F;
        float red = color.getRed() / 255.0F;
        float green = color.getGreen() / 255.0F;
        float blue = color.getBlue() / 255.0F;
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        x1 *= 2.0D;
        y1 *= 2.0D;
        GL11.glDisable(3553);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glEnable(2848);
        GL11.glBegin(9);

        int i;
        for(i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x + radius + Math.sin((double)i * 3.141592653589793D / 180.0D) * radius * -1.0D, y + radius + Math.cos((double)i * 3.141592653589793D / 180.0D) * radius * -1.0D);
        }

        for(i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x + radius + Math.sin((double)i * 3.141592653589793D / 180.0D) * radius * -1.0D, y1 - radius + Math.cos((double)i * 3.141592653589793D / 180.0D) * radius * -1.0D);
        }

        for(i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x1 - radius + Math.sin((double)i * 3.141592653589793D / 180.0D) * radius, y1 - radius + Math.cos((double)i * 3.141592653589793D / 180.0D) * radius);
        }

        for(i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x1 - radius + Math.sin((double)i * 3.141592653589793D / 180.0D) * radius, y + radius + Math.cos((double)i * 3.141592653589793D / 180.0D) * radius);
        }

        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glScaled(2.0D, 2.0D, 2.0D);
        GL11.glPopAttrib();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }


    public static void drawPartialRoundedRect(double x, double y, double width, double height, double radius, Color color) {
        RenderUtil.drawRect(x, y + radius / 2, width, height - radius / 2, color);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        float alpha = color.getAlpha() / 255.0F;
        float red = color.getRed() / 255.0F;
        float green = color.getGreen() / 255.0F;
        float blue = color.getBlue() / 255.0F;

        GL11.glPushAttrib(0);
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        double x1 = x + width * 2.0D;
        double y1 = y + height * 2.0D;

        GL11.glDisable(3553);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glEnable(2848);
        GL11.glBegin(9);

        for(int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x + radius + Math.sin(Math.toRadians(i)) * radius * -1.0D, y + radius + Math.cos(Math.toRadians(i)) * radius * -1.0D);
        }

        for(int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x1 - radius + Math.sin(Math.toRadians(i)) * radius, y + radius + Math.cos(Math.toRadians(i)) * radius);
        }

        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glScaled(2.0D, 2.0D, 2.0D);
        GL11.glPopAttrib();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

    }
    public static void drawPartialRoundedRectBottom(double x, double y, double width, double height, double radius, Color color) {
        RenderUtil.drawRect(x, y, width, height - radius / 2, color); // Rechteck oben
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        float alpha = color.getAlpha() / 255.0F;
        float red = color.getRed() / 255.0F;
        float green = color.getGreen() / 255.0F;
        float blue = color.getBlue() / 255.0F;

        GL11.glPushAttrib(0);
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0D;
        y *= 2.0D;
        double x1 = x + width * 2.0D;
        double y1 = y + height * 2.0D;

        GL11.glDisable(3553);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glEnable(2848);
        GL11.glBegin(9);

        for(int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x1 - radius + Math.sin(Math.toRadians(i)) * radius, y1 - radius + Math.cos(Math.toRadians(i)) * radius);
        }

        for(int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x + radius + Math.sin(Math.toRadians(i)) * radius * -1.0D, y1 - radius + Math.cos(Math.toRadians(i)) * radius * -1.0D);
        }

        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glScaled(2.0D, 2.0D, 2.0D);
        GL11.glPopAttrib();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawFullCircle(double cx, double cy, double r, final int c) {
        GL11.glPushMatrix();
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        r *= 2.0D;
        cx *= 2.0D;
        cy *= 2.0D;
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;
        boolean blend = GL11.glIsEnabled(3042);
        boolean texture2d = GL11.glIsEnabled(3553);
        boolean line = GL11.glIsEnabled(2848);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(6);
        for (int i = 0; i <= 360; i++) {
            double x = Math.sin(i * Math.PI / 180.0D) * r;
            double y = Math.cos(i * Math.PI / 180.0D) * r;
            GL11.glVertex2d(cx + x, cy + y);
        }
        GL11.glEnd();
        f = (c >> 24 & 0xFF) / 255.0F;
        f1 = (c >> 16 & 0xFF) / 255.0F;
        f2 = (c >> 8 & 0xFF) / 255.0F;
        f3 = (c & 0xFF) / 255.0F;
        GL11.glColor4f(f1, f2, f3, f);
        if (!line)
            GL11.glDisable(2848);
        if (texture2d)
            GL11.glEnable(3553);
        if (!blend)
            GL11.glDisable(3042);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        GL11.glPopMatrix();
    }

    public static void rectangleBordered(double x, double y, double x1, double y1, double width, int internalColor, int borderColor)
    {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rectangle(x + width, y, x1 - width, y + width, borderColor);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rectangle(x, y, x + width, y1, borderColor);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rectangle(x1 - width, y, x1, y1, borderColor);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rectangle(x + width, y1 - width, x1 - width, y1, borderColor);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    public static void rectangle(double left, double top, double right, double bottom, int color)
    {
        if (left < right)
        {
            double var5 = left;
            left = right;
            right = var5;
        }
        if (top < bottom)
        {
            double var5 = top;
            top = bottom;
            bottom = var5;
        }
        float var11 = (color >> 24 & 0xFF) / 255.0F;
        float var6 = (color >> 16 & 0xFF) / 255.0F;
        float var7 = (color >> 8 & 0xFF) / 255.0F;
        float var8 = (color & 0xFF) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var6, var7, var8, var11);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(left, bottom, 0.0D).endVertex();
        worldRenderer.pos(right, bottom, 0.0D).endVertex();
        worldRenderer.pos(right, top, 0.0D).endVertex();
        worldRenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void color(float red, float green, float blue, float alpha) {
        glColor4f(red / 255f, green / 255f, blue / 255f, alpha / 255f);
    }

    public static void color(Color color) {
        color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }


    public static void color(int argb) {

        float alpha = (argb >> 24 & 255) / 255f;
        float red = (argb >> 16 & 255) / 255f;
        float green = (argb >> 8 & 255) / 255f;
        float blue = (argb & 255) / 255f;

        glColor4f(red, green, blue, alpha);
    }

    public static void otherDrawBoundingBox(Entity entity, float x, float y, float z, double width, double height) {
        width *= 1.5;
        float yaw1 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 45.0F;
        float newYaw1;
        if (yaw1 < 0.0F) {
            newYaw1 = 0.0F;
            newYaw1 += 360.0F - Math.abs(yaw1);
        } else {
            newYaw1 = yaw1;
        }

        newYaw1 *= -1.0F;
        newYaw1 = (float) ((double) newYaw1 * 0.017453292519943295);
        float yaw2 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 135.0F;
        float newYaw2;
        if (yaw2 < 0.0F) {
            newYaw2 = 0.0F;
            newYaw2 += 360.0F - Math.abs(yaw2);
        } else {
            newYaw2 = yaw2;
        }

        newYaw2 *= -1.0F;
        newYaw2 = (float) ((double) newYaw2 * 0.017453292519943295);
        float yaw3 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 225.0F;
        float newYaw3;
        if (yaw3 < 0.0F) {
            newYaw3 = 0.0F;
            newYaw3 += 360.0F - Math.abs(yaw3);
        } else {
            newYaw3 = yaw3;
        }

        newYaw3 *= -1.0F;
        newYaw3 = (float) ((double) newYaw3 * 0.017453292519943295);
        float yaw4 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 315.0F;
        float newYaw4;
        if (yaw4 < 0.0F) {
            newYaw4 = 0.0F;
            newYaw4 += 360.0F - Math.abs(yaw4);
        } else {
            newYaw4 = yaw4;
        }

        newYaw4 *= -1.0F;
        newYaw4 = (float) ((double) newYaw4 * 0.017453292519943295);
        float x1 = (float) (Math.sin((double) newYaw1) * width + (double) x);
        float z1 = (float) (Math.cos((double) newYaw1) * width + (double) z);
        float x2 = (float) (Math.sin((double) newYaw2) * width + (double) x);
        float z2 = (float) (Math.cos((double) newYaw2) * width + (double) z);
        float x3 = (float) (Math.sin((double) newYaw3) * width + (double) x);
        float z3 = (float) (Math.cos((double) newYaw3) * width + (double) z);
        float x4 = (float) (Math.sin((double) newYaw4) * width + (double) x);
        float z4 = (float) (Math.cos((double) newYaw4) * width + (double) z);
        float y2 = (float) ((double) y + height);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double) x1, (double) y, (double) z1).endVertex();
        worldrenderer.pos((double) x1, (double) y2, (double) z1).endVertex();
        worldrenderer.pos((double) x2, (double) y2, (double) z2).endVertex();
        worldrenderer.pos((double) x2, (double) y, (double) z2).endVertex();
        worldrenderer.pos((double) x2, (double) y, (double) z2).endVertex();
        worldrenderer.pos((double) x2, (double) y2, (double) z2).endVertex();
        worldrenderer.pos((double) x3, (double) y2, (double) z3).endVertex();
        worldrenderer.pos((double) x3, (double) y, (double) z3).endVertex();
        worldrenderer.pos((double) x3, (double) y, (double) z3).endVertex();
        worldrenderer.pos((double) x3, (double) y2, (double) z3).endVertex();
        worldrenderer.pos((double) x4, (double) y2, (double) z4).endVertex();
        worldrenderer.pos((double) x4, (double) y, (double) z4).endVertex();
        worldrenderer.pos((double) x4, (double) y, (double) z4).endVertex();
        worldrenderer.pos((double) x4, (double) y2, (double) z4).endVertex();
        worldrenderer.pos((double) x1, (double) y2, (double) z1).endVertex();
        worldrenderer.pos((double) x1, (double) y, (double) z1).endVertex();
        worldrenderer.pos((double) x1, (double) y, (double) z1).endVertex();
        worldrenderer.pos((double) x2, (double) y, (double) z2).endVertex();
        worldrenderer.pos((double) x3, (double) y, (double) z3).endVertex();
        worldrenderer.pos((double) x4, (double) y, (double) z4).endVertex();
        worldrenderer.pos((double) x1, (double) y2, (double) z1).endVertex();
        worldrenderer.pos((double) x2, (double) y2, (double) z2).endVertex();
        worldrenderer.pos((double) x3, (double) y2, (double) z3).endVertex();
        worldrenderer.pos((double) x4, (double) y2, (double) z4).endVertex();
        worldrenderer.endVertex();
        tessellator.draw();
    }

    public static void otherDrawOutlinedBoundingBox(Entity entity, float x, float y, float z, double width, double height) {
        width *= 1.5;
        float yaw1 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 45.0F;
        float newYaw1;
        if (yaw1 < 0.0F) {
            newYaw1 = 0.0F;
            newYaw1 += 360.0F - Math.abs(yaw1);
        } else {
            newYaw1 = yaw1;
        }

        newYaw1 *= -1.0F;
        newYaw1 = (float) ((double) newYaw1 * 0.017453292519943295);
        float yaw2 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 135.0F;
        float newYaw2;
        if (yaw2 < 0.0F) {
            newYaw2 = 0.0F;
            newYaw2 += 360.0F - Math.abs(yaw2);
        } else {
            newYaw2 = yaw2;
        }

        newYaw2 *= -1.0F;
        newYaw2 = (float) ((double) newYaw2 * 0.017453292519943295);
        float yaw3 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 225.0F;
        float newYaw3;
        if (yaw3 < 0.0F) {
            newYaw3 = 0.0F;
            newYaw3 += 360.0F - Math.abs(yaw3);
        } else {
            newYaw3 = yaw3;
        }

        newYaw3 *= -1.0F;
        newYaw3 = (float) ((double) newYaw3 * 0.017453292519943295);
        float yaw4 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 315.0F;
        float newYaw4;
        if (yaw4 < 0.0F) {
            newYaw4 = 0.0F;
            newYaw4 += 360.0F - Math.abs(yaw4);
        } else {
            newYaw4 = yaw4;
        }

        newYaw4 *= -1.0F;
        newYaw4 = (float) ((double) newYaw4 * 0.017453292519943295);
        float x1 = (float) (Math.sin((double) newYaw1) * width + (double) x);
        float z1 = (float) (Math.cos((double) newYaw1) * width + (double) z);
        float x2 = (float) (Math.sin((double) newYaw2) * width + (double) x);
        float z2 = (float) (Math.cos((double) newYaw2) * width + (double) z);
        float x3 = (float) (Math.sin((double) newYaw3) * width + (double) x);
        float z3 = (float) (Math.cos((double) newYaw3) * width + (double) z);
        float x4 = (float) (Math.sin((double) newYaw4) * width + (double) x);
        float z4 = (float) (Math.cos((double) newYaw4) * width + (double) z);
        float y2 = (float) ((double) y + height);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double) x1, (double) y, (double) z1).endVertex();
        worldrenderer.pos((double) x1, (double) y2, (double) z1).endVertex();
        worldrenderer.pos((double) x2, (double) y2, (double) z2).endVertex();
        worldrenderer.pos((double) x2, (double) y, (double) z2).endVertex();
        worldrenderer.pos((double) x1, (double) y, (double) z1).endVertex();
        worldrenderer.pos((double) x4, (double) y, (double) z4).endVertex();
        worldrenderer.pos((double) x3, (double) y, (double) z3).endVertex();
        worldrenderer.pos((double) x3, (double) y2, (double) z3).endVertex();
        worldrenderer.pos((double) x4, (double) y2, (double) z4).endVertex();
        worldrenderer.pos((double) x4, (double) y, (double) z4).endVertex();
        worldrenderer.pos((double) x4, (double) y2, (double) z4).endVertex();
        worldrenderer.pos((double) x3, (double) y2, (double) z3).endVertex();
        worldrenderer.pos((double) x2, (double) y2, (double) z2).endVertex();
        worldrenderer.pos((double) x2, (double) y, (double) z2).endVertex();
        worldrenderer.pos((double) x3, (double) y, (double) z3).endVertex();
        worldrenderer.pos((double) x4, (double) y, (double) z4).endVertex();
        worldrenderer.pos((double) x4, (double) y2, (double) z4).endVertex();
        worldrenderer.pos((double) x1, (double) y2, (double) z1).endVertex();
        worldrenderer.pos((double) x1, (double) y, (double) z1).endVertex();
        worldrenderer.endVertex();
        tessellator.draw();
    }



    public static void prepareScissorBox(final double x, final double y, final double width, final double height) {
        int factor = getScaledResolution().getScaleFactor();
        GL11.glScissor((int) (x * (float) factor), (int) (((float) getScaledResolution().getScaledHeight() - (y + height)) * (float) factor), (int) (((x + width) - x) * (float) factor), (int) (((y + height) - y) * (float) factor));
    }

    public static boolean isInViewFrustrum(Entity entity) {
        return isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    public static boolean isInViewFrustrum(AxisAlignedBB bb) {
        final Entity current = MC.getRenderViewEntity();
        frustum.setPosition(current.posX, current.posY, current.posZ);
        return frustum.isBoundingBoxInFrustum(bb);
    }


    public static ScaledResolution getScaledResolution() {
        return new ScaledResolution(MC);
    }



    public static void drawGradientSideways(double left, double top, double right, double bottom, Color col1, Color col2) {
        float f = col1.getAlpha() / 255.0F;
        float f1 = col1.getRed() / 255.0F;
        float f2 = col1.getGreen() / 255.0F;
        float f3 = col1.getBlue() / 255.0F;

        float f4 = col2.getAlpha() / 255.0F;
        float f5 = col2.getRed() / 255.0F;
        float f6 = col2.getGreen() / 255.0F;
        float f7 = col2.getBlue() / 255.0F;

        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);

        GL11.glPushMatrix();
        GL11.glBegin(7);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glVertex2d(left, top);
        GL11.glVertex2d(left, bottom);

        GL11.glColor4f(f5, f6, f7, f4);
        GL11.glVertex2d(right, bottom);
        GL11.glVertex2d(right, top);
        GL11.glEnd();
        GL11.glPopMatrix();

        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glShadeModel(7424);
        GL11.glColor4d(1.0, 1.0, 1.0, 1.0); // Standard-Weißfarbe

    }


    public static void drawEntityServerESP(Entity entity, float partialTicks, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        double d0 = (double) entity.serverPosX / 32.0;
        double d1 = (double) entity.serverPosY / 32.0;
        double d2 = (double) entity.serverPosZ / 32.0;
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase livingBase = (EntityLivingBase) entity;
            d0 = (double) livingBase.realPosX / 32.0;
            d1 = (double) livingBase.realPosY / 32.0;
            d2 = (double) livingBase.realPosZ / 32.0;
        }

        float x = (float) (d0 - RenderManager.getRenderPosX());
        float y = (float) (d1 - RenderManager.getRenderPosY());
        float z = (float) (d2 - RenderManager.getRenderPosZ());

        int i = entity.getBrightnessForRender(partialTicks);
        if (entity.isBurning()) {
            i = 15728880;
        }

        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j / 1.0F, (float) k / 1.0F);

        GL11.glColor4f(f, f1, f2, f3);
        MC.getRenderManager().doRenderEntity(entity, x, y, z, partialTicks, 0, true);
        GL11.glColor4f(1, 1, 1, 1);
        //otherDrawBoundingBox(entity, x, y, z, (double)(entity.width - 0.2F), (double)(entity.height + 0.1F));

    }



    public int rainbow(int speed, double offset) {
        float hue = (float) (((double) System.currentTimeMillis() + offset) % (double) speed);
        hue /= (float) speed;
        return Color.getHSBColor(hue, 0.5F, 1.0F).getRGB();
    }

    public void drawRainbowSpiral(double radius, double x, double lastTickX, double y, double lastTickY, double z, double lastTickZ, double height, double coils, double phaseShift, int rainbow, Color c) {
        if (phaseShift > 6.283185307179586) {
            phaseShift = 0.0;
        }
        float pTicks = Minecraft.getMinecraft().timer.renderPartialTicks;
        double posX = lastTickX + (x - lastTickX) * (double) pTicks - RenderManager.getRenderPosX();
        double posY = lastTickY + (y - lastTickY) * (double) pTicks - RenderManager.getRenderPosY();
        double posZ = lastTickZ + (z - lastTickZ) * (double) pTicks - RenderManager.getRenderPosZ();
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(3, DefaultVertexFormats.POSITION_COLOR);

        for (double a = 0.0; a < 6.283185307179586 * coils; a += 6.283185307179586 * coils / 250.0) {
            if (rainbow > 0) {
                c = new Color(this.rainbow(20000, (double) (rainbow * 50) * a));
            }

            wr.pos(posX + Math.cos(a + phaseShift) * radius, posY + a * (height / (6.283185307179586 * coils)), posZ + Math.sin(a + phaseShift) * radius).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        }

        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }


    public void renderBox(double x, double y, double z, float width, float height, Color c) {
        float halfwidth = width / 2.0F;
        float halfheight = height / 2.0F;
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        ++y;
        worldRenderer.pos(x - (double) halfwidth, y - (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y + (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y + (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y - (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y - (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y + (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y + (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y - (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y - (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y + (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y + (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y - (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y - (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y + (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y + (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y - (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y + (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y + (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y + (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y + (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y - (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y - (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y - (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y - (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        tessellator.draw();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
    }

    public void renderOutlines(double x, double y, double z, float width, float height, Color c) {
        float halfwidth = width / 2.0F;
        float halfheight = height / 2.0F;
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        ++y;
        GL11.glLineWidth(1.2F);
        worldRenderer.pos(x - (double) halfwidth, y - (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y + (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y - (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y + (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y - (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y + (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y - (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y + (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y - (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y - (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y - (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y - (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y - (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y - (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y - (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y - (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y + (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y + (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y + (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y + (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y + (double) halfheight, z - (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y + (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x - (double) halfwidth, y + (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        worldRenderer.pos(x + (double) halfwidth, y + (double) halfheight, z + (double) halfwidth).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();
        tessellator.draw();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
    }

    public void renderBoxWithOutline(double x, double y, double z, float width, float height, Color c) {
        this.renderBox(x, y, z, width, height, c);
        this.renderOutlines(x, y, z, width, height, c);
    }

    public static void drawColoredCircle(double x, double y, double radius, float brightness) {
        GL11.glPushMatrix();
        GL11.glLineWidth(3.5F);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glBegin(3);

        for (int i = 0; i < 360; ++i) {
            color(Color.HSBtoRGB(1.0F, 0.0F, brightness));
            GL11.glVertex2d(x, y);
            color(Color.HSBtoRGB((float) i / 360.0F, 1.0F, brightness));
            GL11.glVertex2d(x + Math.sin(Math.toRadians((double) i)) * radius, y + Math.cos(Math.toRadians((double) i)) * radius);
        }

        GL11.glEnd();
        GL11.glShadeModel(7424);
        GL11.glDisable(2848);
        GL11.glPopMatrix();
    }

    public void setColor(Color color) {
        color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }




    public static void drawBlockESP(BlockPos blockPos, float red, float green, float blue, float alpha, float lineAlpha, float lineWidth) {
        GlStateManager.color(red, green, blue, alpha);
        float x = (float)((double)blockPos.getX() - MC.getRenderManager().getRenderPosX());
        float y = (float)((double)blockPos.getY() - MC.getRenderManager().getRenderPosY());
        float z = (float)((double)blockPos.getZ() - MC.getRenderManager().getRenderPosZ());
        Block block = MC.theWorld.getBlockState(blockPos).getBlock();

        if (block instanceof BlockChest) {
            double chestWidth = 0.875; // Breite der Truhe in Blocks (eine Truhe ist 0.875 Blöcke breit)
            double chestHeight = 0.75; // Höhe der Truhe in Blocks (eine Truhe ist 0.75 Blöcke hoch)

            // Hier wird die Bounding Box für die Truhe entsprechend ihrer tatsächlichen Größe erstellt
            drawBoundingBox(new AxisAlignedBB(
                    (double)x + (1 - chestWidth) / 2,
                    (double)y,
                    (double)z + (1 - chestWidth) / 2,
                    (double)x + (1 + chestWidth) / 2,
                    (double)y + block.getBlockBoundsMaxY(),
                    (double)z + (1 + chestWidth) / 2
            ));

            if (lineWidth > 0.0F) {
                GL11.glLineWidth(lineWidth);
                GlStateManager.color(red, green, blue, lineAlpha);
                drawOutlinedBoundingBox(new AxisAlignedBB(
                        (double)x + (1 - chestWidth) / 2,
                        (double)y,
                        (double)z + (1 - chestWidth) / 2,
                        (double)x + (1 + chestWidth) / 2,
                        (double)y + block.getBlockBoundsMaxY(),
                        (double)z + (1 + chestWidth) / 2
                ));
            }
        } else {
            // Für alle anderen Blöcke die normale Größe verwenden
            drawBoundingBox(new AxisAlignedBB(
                    (double)x,
                    (double)y,
                    (double)z,
                    (double)x + block.getBlockBoundsMaxX(),
                    (double)y + block.getBlockBoundsMaxY(),
                    (double)z + block.getBlockBoundsMaxZ()
            ));

            if (lineWidth > 0.0F) {
                GL11.glLineWidth(lineWidth);
                GlStateManager.color(red, green, blue, lineAlpha);
                drawOutlinedBoundingBox(new AxisAlignedBB(
                        (double)x,
                        (double)y,
                        (double)z,
                        (double)x + block.getBlockBoundsMaxX(),
                        (double)y + block.getBlockBoundsMaxY(),
                        (double)z + block.getBlockBoundsMaxZ()
                ));
            }
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }


    public static void drawEntityESP(Entity entity, float red, float green, float blue, float alpha, float lineAlpha, float lineWidth) {
        float x = (float)(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)MC.getTimer().renderPartialTicks - MC.getRenderManager().getRenderPosX());
        float y = (float)(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)MC.getTimer().renderPartialTicks - MC.getRenderManager().getRenderPosY());
        float z = (float)(entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)MC.getTimer().renderPartialTicks - MC.getRenderManager().getRenderPosZ());
        GL11.glColor4f(red, green, blue, alpha);
        otherDrawBoundingBox(entity, x, y, z, (double)(entity.width - 0.2F), (double)(entity.height + 0.1F));
        if (lineWidth > 0.0F) {
            GL11.glLineWidth(lineWidth);
            GL11.glColor4f(red, green, blue, lineAlpha);
            otherDrawOutlinedBoundingBox(entity, x, y, z, (double)(entity.width - 0.2F), (double)(entity.height + 0.1F));
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawOutlinedBoundingBox(AxisAlignedBB a) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.minY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.minY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.maxY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.maxY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.minY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.minY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.maxY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.maxY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.minY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.minY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.minY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.minY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.maxY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.maxY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.maxY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.maxY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.endVertex();
        tessellator.draw();
    }

    public static void drawBoundingBox(AxisAlignedBB a) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.minY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.minY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.maxY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.maxY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.minY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.minY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.maxY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.maxY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.minY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.minY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.maxY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.maxY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.minY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.minY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.maxY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.maxY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.minY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.minY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.minY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.minY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.maxY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.pos((double) ((float) a.minX), (double) ((float) a.maxY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.maxY), (double) ((float) a.maxZ)).endVertex();
        worldrenderer.pos((double) ((float) a.maxX), (double) ((float) a.maxY), (double) ((float) a.minZ)).endVertex();
        worldrenderer.endVertex();
        tessellator.draw();
    }


    public static void draw3DRect(float x1, float y1, float x2, float y2) {
        GL11.glBegin(7);
        GL11.glVertex2d((double) x2, (double) y1);
        GL11.glVertex2d((double) x1, (double) y1);
        GL11.glVertex2d((double) x1, (double) y2);
        GL11.glVertex2d((double) x2, (double) y2);
        GL11.glEnd();
    }

    public static void drawCornerESP(Entity entity, float red, float green, float blue) {
        float x = (float)(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)MC.getTimer().renderPartialTicks - MC.getRenderManager().getRenderPosX());
        float y = (float)(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)MC.getTimer().renderPartialTicks - MC.getRenderManager().getRenderPosY());
        float z = (float)(entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)MC.getTimer().renderPartialTicks - MC.getRenderManager().getRenderPosZ());
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height / 2.0F, z);
        GlStateManager.rotate(-MC.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(-0.098, -0.098, 0.098);
        float width = (float)(26.6 * (double)entity.width / 2.0);
        float height = entity instanceof EntityPlayer ? 12.0F : (float)(11.98 * (double)(entity.height / 2.0F));
        GlStateManager.color(red, green, blue);
        draw3DRect(width, height - 1.0F, width - 4.0F, height);
        draw3DRect(-width, height - 1.0F, -width + 4.0F, height);
        draw3DRect(-width, height, -width + 1.0F, height - 4.0F);
        draw3DRect(width, height, width - 1.0F, height - 4.0F);
        draw3DRect(width, -height, width - 4.0F, -height + 1.0F);
        draw3DRect(-width, -height, -width + 4.0F, -height + 1.0F);
        draw3DRect(-width, -height + 1.0F, -width + 1.0F, -height + 4.0F);
        draw3DRect(width, -height + 1.0F, width - 1.0F, -height + 4.0F);
        GlStateManager.color(0.0F, 0.0F, 0.0F);
        draw3DRect(width, height, width - 4.0F, height + 0.2F);
        draw3DRect(-width, height, -width + 4.0F, height + 0.2F);
        draw3DRect(-width - 0.2F, height + 0.2F, -width, height - 4.0F);
        draw3DRect(width + 0.2F, height + 0.2F, width, height - 4.0F);
        draw3DRect(width + 0.2F, -height, width - 4.0F, -height - 0.2F);
        draw3DRect(-width - 0.2F, -height, -width + 4.0F, -height - 0.2F);
        draw3DRect(-width - 0.2F, -height, -width, -height + 4.0F);
        draw3DRect(width + 0.2F, -height, width, -height + 4.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
    public static Color getColor(float red, float green, float blue, float alpha) {
        return new Color(red / 255.0F, green / 255.0F, blue / 255.0F, alpha / 255.0F);
    }

    public static void drawCirclePicker(double x, double y, double radius, int color) {
        GL11.glPushMatrix();
        color(color);
        GL11.glBegin(9);

        for(int i = 0; i < 360; ++i) {
            GL11.glVertex2d(x + Math.sin(Math.toRadians((double)i)) * radius, y + Math.cos(Math.toRadians((double)i)) * radius);
        }

        GL11.glEnd();
        GL11.glPopMatrix();
    }
    public static void drawCircle(double cx, double cy, double r, final int c) {
        float alpha = (c >> 24 & 0xFF) / 255.0F;
        float red = (c >> 16 & 0xFF) / 255.0F;
        float green = (c >> 8 & 0xFF) / 255.0F;
        float blue = (c & 0xFF) / 255.0F;

        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean texture2d = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean lineSmooth = GL11.glIsEnabled(GL11.GL_LINE_SMOOTH);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) cx, (float) cy, 0.0F);
        GL11.glScalef((float) r, (float) r, 1.0F);

        if (!blend) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        if (!texture2d) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        if (!lineSmooth) {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
        }

        GL11.glColor4f(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        for (int i = 0; i <= 360; i++) {
            double x = Math.sin(Math.toRadians(i)) * 1.0;
            double y = Math.cos(Math.toRadians(i)) * 1.0;
            GL11.glVertex2d(x, y);
        }
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnd();

        GL11.glPopMatrix();
    }

    public static void bindTexture(int texture) {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        if (framebuffer == null || framebuffer.framebufferWidth != MC.displayWidth || framebuffer.framebufferHeight != MC.displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(MC.displayWidth, MC.displayHeight, true);
        }
        return framebuffer;
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * .01));
    }

    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }

    public void drawTargetBox(Entity entity, float[] rotations) {
        drawBox(MC.objectMouseOver.hitVec.xCoord - (MC.getRenderManager().viewerPosX), MC.objectMouseOver.hitVec.yCoord - (MC.getRenderManager().viewerPosY), MC.objectMouseOver.hitVec.zCoord - (MC.getRenderManager().viewerPosZ));
    }


    private void drawBox(double x, double y, double z) {
        float size = 0.09F; // Größe des Würfels

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull();
        GL11.glDepthMask(false);

        // Zeichne die transparente Box
        Color color = new Color(255, 255, 255, 255);
        GL11.glColor4f(color.getRed() / 255, color.getGreen() / 255, color.getBlue() / 255, 0.5F); // Rote Box mit 50% Transparenz
        GL11.glBegin(GL11.GL_QUADS);

        // Vorderseite
        GL11.glVertex3d(-size, -size, -size);
        GL11.glVertex3d(size, -size, -size);
        GL11.glVertex3d(size, size, -size);
        GL11.glVertex3d(-size, size, -size);

        // Rückseite
        GL11.glVertex3d(-size, -size, size);
        GL11.glVertex3d(size, -size, size);
        GL11.glVertex3d(size, size, size);
        GL11.glVertex3d(-size, size, size);

        // Linke Seite
        GL11.glVertex3d(-size, -size, -size);
        GL11.glVertex3d(-size, -size, size);
        GL11.glVertex3d(-size, size, size);
        GL11.glVertex3d(-size, size, -size);

        // Rechte Seite
        GL11.glVertex3d(size, -size, -size);
        GL11.glVertex3d(size, -size, size);
        GL11.glVertex3d(size, size, size);
        GL11.glVertex3d(size, size, -size);

        // Oben
        GL11.glVertex3d(-size, size, -size);
        GL11.glVertex3d(size, size, -size);
        GL11.glVertex3d(size, size, size);
        GL11.glVertex3d(-size, size, size);

        // Unten
        GL11.glVertex3d(-size, -size, -size);
        GL11.glVertex3d(size, -size, -size);
        GL11.glVertex3d(size, -size, size);
        GL11.glVertex3d(-size, -size, size);

        GL11.glEnd();

        // Zeichne die Kanten der Box
        GL11.glLineWidth(2.0F);
        Color color1 = new Color(0, 0, 0, 255);
        GL11.glColor4f(color1.getRed() / 255, color1.getGreen() / 255, color1.getBlue() / 255, 1.0F); // Rote Box mit 50% Transparenz
        GL11.glBegin(GL11.GL_LINES);
/*
        // Vorderseite
        GL11.glVertex3d(-size, -size, -size);
        GL11.glVertex3d(size, -size, -size);

        GL11.glVertex3d(size, -size, -size);
        GL11.glVertex3d(size, size, -size);

        GL11.glVertex3d(size, size, -size);
        GL11.glVertex3d(-size, size, -size);

        GL11.glVertex3d(-size, size, -size);
        GL11.glVertex3d(-size, -size, -size);

        // Rückseite
        GL11.glVertex3d(-size, -size, size);
        GL11.glVertex3d(size, -size, size);

        GL11.glVertex3d(size, -size, size);
        GL11.glVertex3d(size, size, size);

        GL11.glVertex3d(size, size, size);
        GL11.glVertex3d(-size, size, size);

        GL11.glVertex3d(-size, size, size);
        GL11.glVertex3d(-size, -size, size);

        // Verbindungen
        GL11.glVertex3d(-size, -size, -size);
        GL11.glVertex3d(-size, -size, size);

        GL11.glVertex3d(size, -size, -size);
        GL11.glVertex3d(size, -size, size);

        GL11.glVertex3d(size, size, -size);
        GL11.glVertex3d(size, size, size);

        GL11.glVertex3d(-size, size, -size);
        GL11.glVertex3d(-size, size, size);*/

        GL11.glEnd();

        GL11.glDepthMask(true);
        GlStateManager.enableCull();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
}

