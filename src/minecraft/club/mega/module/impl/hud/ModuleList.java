package club.mega.module.impl.hud;

import club.mega.Mega;
import club.mega.event.impl.EventBlur;
import club.mega.event.impl.EventRender2D;
import club.mega.event.impl.EventResize;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.*;
import club.mega.util.animation.AnimationUtil;
import org.lwjgl.opengl.GL11;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Module.ModuleInfo(name = "ModuleList", description = "Render List of Modules", category = Category.HUD)
public class ModuleList extends Module {

    public final ListSetting font = new ListSetting("Font", this, new String[]{"Arial"});
    private final ListSetting mode = new ListSetting("Name Color", this, new String[]{"Normal", "Fade"});
    private final NumberSetting speed = new NumberSetting("Fade Speed", this, 0.1, 1, 0.2, 0.1, () -> mode.is("Fade"));
    private final ColorSetting colornormal = new ColorSetting("Color", this, new Color(255, 255, 255), () -> mode.is("Normal"));
    private final BooleanSetting thirdColor = new BooleanSetting("Third Color", this, true, () -> mode.is("Fade"));
    private final ColorSetting color1 = new ColorSetting("Start Color", this, new Color(255, 255, 255), () -> mode.is("Fade"));
    private final ColorSetting color2 = new ColorSetting("Mid Color", this, new Color(255, 255, 255), () -> mode.is("Fade") && thirdColor.get());
    private final ColorSetting color3 = new ColorSetting("End Color", this, new Color(255, 255, 255), () -> mode.is("Fade"));

    public final NumberSetting fontSize = new NumberSetting("Font Size", this, 8, 25, 19, 1);
    public final NumberSetting moduleHeight = new NumberSetting("Height", this, 8, 15, 11, 1);
    public final ColorSetting bgColor = new ColorSetting("BGColor", this, new Color(0, 0, 0, 150));
    public final NumberSetting verticalOffset = new NumberSetting("VerticalOff", this, 0, 20, 0, 1);
    public final NumberSetting horizontalOffset = new NumberSetting("HorizontalOff", this, 0, 20, 0, 1);
    public final BooleanSetting blur = new BooleanSetting("Blur", this, true);

    @Handler
    public final void render2d(EventRender2D event) {
        int index = 0;
        double offset = 1 + verticalOffset.getAsInt();
        updateModuleWidths();
        String selectedFont = font.getCurrent() + " " + fontSize.getAsInt();

        List<Module> modulesToRender = new ArrayList<>(Mega.INSTANCE.getModuleManager().getModules());

        modulesToRender = getSortedModules(modulesToRender, selectedFont);



        for (final Module module : modulesToRender) {
            if (!module.isToggled() && module.getCurrentWidth() >= module.getTargetWidth()) continue;

            module.setCurrentHeight(AnimationUtil.animate(module.getCurrentHeight(), module.getTargetHeight(), 0.3));
            module.setCurrentWidth(AnimationUtil.animate(module.getCurrentWidth(), module.getTargetWidth(), 0.3));

            RenderUtil.drawRect(module.getCurrentWidth() - 1, offset - 1, 1 + Mega.INSTANCE.getFontManager().getFont(selectedFont).getWidth(module.getName()), module.getCurrentHeight(), bgColor.getColor());
            double fadeSpeed = speed.getAsDouble() + 1.2;
            Color color = new Color(255, 255, 255);

            if (mode.is("Normal")) {
                color = colornormal.getColor();
            } else if (mode.is("Fade") && !thirdColor.get()) {
                color = ColorUtil.getGradientOffset(color1.getColor(), color3.getColor(), index * 10.5D / (fadeSpeed + 4));

            } else if (mode.is("Fade") && thirdColor.get()) {
                color = ColorUtil.getGradientOffset(color1.getColor(), color2.getColor(), color3.getColor(), index * 10.5D / (fadeSpeed + 4));
            }

            Mega.INSTANCE.getFontManager().getFont(selectedFont).drawString(module.getName(), module.getCurrentWidth(), offset - 2, color);

            GL11.glDisable(GL11.GL_BLEND);
            index--;
            offset += module.getCurrentHeight();
        }
    }

    @Handler
    public final void onBlur(EventBlur event) {
        double offset = 1 + verticalOffset.getAsInt();
        updateModuleWidths();
        String selectedFont = font.getCurrent() + " " + fontSize.getAsInt();

        List<Module> modulesToRender = new ArrayList<>(Mega.INSTANCE.getModuleManager().getModules());

        modulesToRender = getSortedModules(modulesToRender, selectedFont);



        for (final Module module : modulesToRender) {
            if (!module.isToggled() && module.getCurrentWidth() >= module.getTargetWidth()) continue;

            module.setCurrentHeight(AnimationUtil.animate(module.getCurrentHeight(), module.getTargetHeight(), 0.3));
            module.setCurrentWidth(AnimationUtil.animate(module.getCurrentWidth(), module.getTargetWidth(), 0.3));

            RenderUtil.drawRect(module.getCurrentWidth() - 1, offset - 1, 1 + Mega.INSTANCE.getFontManager().getFont(selectedFont).getWidth(module.getName()), module.getCurrentHeight(), -1);

            GL11.glDisable(GL11.GL_BLEND);
            offset += module.getCurrentHeight();
        }
    }

    public final int getHeight() {
        int height = 0;
        for (final Module module : Mega.INSTANCE.getModuleManager().getModules()) {
            if (!module.isToggled() && module.getCurrentWidth() >= module.getTargetWidth()) continue;
            height += module.getCurrentHeight();
        }
        return height;
    }

    public List<Module> getSortedModules(List<Module> modules, String font) {
        List<Module> sortedModules = new ArrayList<>(modules);
        sortedModules.sort(new Comparator<Module>() {
            @Override
            public int compare(Module m1, Module m2) {
                return Float.compare(
                        Mega.INSTANCE.getFontManager().getFont(font).getWidth(m2.getName()),
                        Mega.INSTANCE.getFontManager().getFont(font).getWidth(m1.getName())
                );
            }
        });
        return sortedModules;
    }

    private void updateModuleWidths() {
        for (final Module module : Mega.INSTANCE.getModuleManager().getModules()) {
            String selectedFont = font.getCurrent() + " " + fontSize.getAsInt();
            if(module.isToggled()) {
                module.setTargetWidth(RenderUtil.getScaledResolution().getScaledWidth() - Mega.INSTANCE.getFontManager().getFont(selectedFont).getWidth(module.getName()) - horizontalOffset.getAsInt());
                module.setTargetHeight(moduleHeight.getAsInt());
            } else {
                module.setTargetWidth(RenderUtil.getScaledResolution().getScaledWidth());
                module.setTargetHeight(0);
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        for (final Module module : Mega.INSTANCE.getModuleManager().getModules()) {
            module.setCurrentWidth(RenderUtil.getScaledResolution().getScaledWidth());
            module.setCurrentHeight(0);
        }

    }
}
