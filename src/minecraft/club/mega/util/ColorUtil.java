package club.mega.util;

import club.mega.module.setting.impl.*;
import net.minecraft.util.MathHelper;

import java.awt.*;

import static club.mega.util.MathUtil.interpolateFloat;
import static club.mega.util.MathUtil.interpolateInt;


public final class ColorUtil {

    private static Color mainColor = new Color(0, 166, 255);

    public static Color getMainColor() {
        return mainColor;
    }

    public static Color getMainColor(final int alpha) {
        return new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), alpha);
    }

    public static void setMainColor(Color color) {
        mainColor = color;
    }

    public static Color getGradientOffset(final Color color1, final Color color2, final double index) {
        double offs = (Math.abs(((System.currentTimeMillis()) / 16D)) / 60D) + index;
        if(offs >1)
        {
            double left = offs % 1;
            int off = (int) offs;
            offs = off % 2 == 0 ? left : 1 - left;
        }

        final double inverse_percent = 1 - offs;
        int redPart = (int) (color1.getRed() * inverse_percent + color2.getRed() * offs);
        int greenPart = (int) (color1.getGreen() * inverse_percent + color2.getGreen() * offs);
        int bluePart = (int) (color1.getBlue() * inverse_percent + color2.getBlue() * offs);
        int alphaPart = (int) (color1.getAlpha() * inverse_percent + color2.getAlpha() * offs);
        return new Color(redPart, greenPart, bluePart, alphaPart);
    }

    public static Color getGradientOffset(final Color color1, final Color color2, final Color color3, final double index) {
        double offs = (Math.abs(((System.currentTimeMillis()) / 16D)) / 60D) + index;
        if (offs > 2) {
            double left = offs % 1;
            int off = (int) offs;
            offs = off % 2 == 0 ? left : 1 - left;
        } else if (offs > 1) {
            offs = 2 - offs;
        }

        final double inverse_percent = 1 - offs;
        int redPart = (int) (color1.getRed() * inverse_percent + color2.getRed() * offs);
        int greenPart = (int) (color1.getGreen() * inverse_percent + color2.getGreen() * offs);
        int bluePart = (int) (color1.getBlue() * inverse_percent + color2.getBlue() * offs);
        int alphaPart = (int) (color1.getAlpha() * inverse_percent + color2.getAlpha() * offs);

        int redPart2 = (int) (color2.getRed() * inverse_percent + color3.getRed() * offs);
        int greenPart2 = (int) (color2.getGreen() * inverse_percent + color3.getGreen() * offs);
        int bluePart2 = (int) (color2.getBlue() * inverse_percent + color3.getBlue() * offs);
        int alphaPart2 = (int) (color2.getAlpha() * inverse_percent + color3.getAlpha() * offs);

        int finalRed = (int) (redPart * inverse_percent + redPart2 * offs);
        int finalGreen = (int) (greenPart * inverse_percent + greenPart2 * offs);
        int finalBlue = (int) (bluePart * inverse_percent + bluePart2 * offs);
        int finalAlpha = (int) (alphaPart * inverse_percent + alphaPart2 * offs);

        return new Color(finalRed, finalGreen, finalBlue, finalAlpha);
    }
    public static int getColor(int red, int green, int blue, int alpha) {
        int color = MathHelper.clamp_int(alpha, 0, 255) << 24;
        color |= MathHelper.clamp_int(red, 0, 255) << 16;
        color |= MathHelper.clamp_int(green, 0, 255) << 8;
        color |= MathHelper.clamp_int(blue, 0, 255);
        return color;
    }

    public static int[] getRGB(int hex) {
        int a = (hex >> 24) & 0xFF;
        int r = (hex >> 16) & 0xFF;
        int g = (hex >> 8) & 0xFF;
        int b = hex & 0xFF;
        return new int[] {r, g, b, a};
    }
    public Color interpolateColorsBackAndForth(ColorSetting colornormal, ColorSetting color1, ColorSetting color2, ColorSetting color3, BooleanSetting thirdColor, ListSetting mode, NumberSetting fadeSpeed) {

        Color[] colors;
        if (mode.is("Normal")) {
            return colornormal.getColor();
        } else if (mode.is("Fade") && !thirdColor.get()) {
            colors = new Color[]{color1.getColor(), color3.getColor()};
        } else {
            colors = new Color[]{color1.getColor(), color2.getColor(), color3.getColor()};

        }
        return interpolateColorsBackAndForth(fadeSpeed.getAsInt(), 0, colors,false);

    }
    public Color interpolateColorsBackAndForth(ColorSetting colornormal, ColorSetting color1, ColorSetting color3, ListSetting mode, NumberSetting fadeSpeed) {

        Color[] colors;
        if (mode.is("Normal")) {
            return colornormal.getColor();
        } else {
            colors = new Color[]{color1.getColor(), color3.getColor()};
        }
        return interpolateColorsBackAndForth(fadeSpeed.getAsInt(), 0, colors,false);

    }

    public static Color interpolateColorsBackAndForth(int speed, int index, Color[] colors, boolean trueColor) {
        int numColors = colors.length;
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        int segmentSize = 360 / numColors;
        int segmentIndex = angle / segmentSize;
        int segmentAngle = angle % segmentSize;

        Color startColor = colors[segmentIndex];
        Color endColor = colors[(segmentIndex + 1) % numColors];

        float amount = (float) segmentAngle / segmentSize;
        return trueColor ? ColorUtil.interpolateColorHue(startColor, endColor, amount) : ColorUtil.interpolateColorC(startColor, endColor, amount);
    }
    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        Color resultColor = Color.getHSBColor(interpolateFloat(color1HSB[0], color2HSB[0], amount),
                interpolateFloat(color1HSB[1], color2HSB[1], amount), interpolateFloat(color1HSB[2], color2HSB[2], amount));

        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }
    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount),
                interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }
}
