package club.mega.gui.clicknew;

import club.mega.Mega;
import club.mega.module.Module;
import club.mega.module.impl.hud.ClickGui;
import club.mega.util.ChatUtil;

import java.awt.*;
import java.util.List;

public class ModuleComponent {
    private List<Module> modules;
    private double rectX, rectY, rectWidth, rectHeight;
    private int maxVisibleModules;
    private int scrollOffset;

    public ModuleComponent(List<Module> modules, double rectX, double rectY, double rectWidth, double rectHeight, int maxVisibleModules) {
        this.modules = modules;
        this.rectX = rectX;
        this.rectY = rectY;
        this.rectWidth = rectWidth;
        this.rectHeight = rectHeight;
        this.maxVisibleModules = maxVisibleModules;
        this.scrollOffset = 0;
    }

    public void drawModules(int mouseX, int mouseY) {
        double moduleY = rectY - scrollOffset;
        int visibleModules = 0;
        for (Module module : modules) {
            if (moduleY + 20 > rectY + rectHeight) break;
            if (moduleY >= rectY) {
                Mega.INSTANCE.getFontManager().getFont("Roboto-Regular 20").drawStringWithShadow(
                        module.isExpanded() ? ">  " + module.getName() : module.getName(),
                        (int) rectX,
                        (int) moduleY,
                        module.isToggled() ? ClickGui.getInstance().color.getColor() : new Color(157, 157, 157, 255)
                );
                visibleModules++;
            }
            moduleY += 20;
        }
    }

    public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        double moduleY = rectY - scrollOffset;
        int visibleModules = 0;
        for (Module module : modules) {
            if (moduleY + 20 > rectY + rectHeight) break;
            if (moduleY >= rectY) {
                boolean isHovered = mouseX >= rectX && mouseX <= rectX +  Mega.INSTANCE.getFontManager().getFont("Roboto-Regular 20").getWidth(module.getName()) && mouseY >= moduleY && mouseY <= moduleY + 10;
                if (isHovered) {
                    if (mouseButton == 0) {
                        module.toggle();
                    } else if (mouseButton == 1) {
                        club.mega.gui.clicknew.ClickGui.setSettingScrollOffset(0);

                        if (!module.isExpanded()) {
                            // Schließe alle anderen Module
                            for (Module otherModule : modules) {
                                otherModule.setExpanded(false);
                            }
                            module.setExpanded(true);

                        } else {
                            module.setExpanded(false);
                        }
                    }
                    break;
                }
                visibleModules++;
            }
            moduleY += 20;
        }
    }

    private boolean isMouseInModuleArea(int mouseX, int mouseY) {
        // Passen Sie die Bedingungen an, um den Bereich der Einstellungen korrekt zu definieren
        return mouseX >= rectX && mouseX <= rectX + 100 && mouseY >= rectY + 30 && mouseY <= rectY + rectHeight;
    }

    public void handleMouseScroll(int scrollAmount) {
        int totalModulesHeight = modules.size() * 20;
        int maxScroll = Math.max(0, totalModulesHeight - (int) rectHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset - scrollAmount / 120 * 10, maxScroll));
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = scrollOffset;
    }
}

