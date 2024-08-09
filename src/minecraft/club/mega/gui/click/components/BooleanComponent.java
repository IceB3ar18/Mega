package club.mega.gui.click.components;

import club.mega.Mega;
import club.mega.gui.click.Component;
import club.mega.module.impl.hud.ClickGui;
import club.mega.module.impl.hud.Shadow;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.util.ColorUtil;
import club.mega.util.RenderUtil;
import club.mega.util.animation.impl.DecelerateAnimation;
import club.mega.util.shader.Animation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;

import java.awt.*;

public class BooleanComponent extends Component {

    private final BooleanSetting setting;
    public Framebuffer framebuffer;
    public Framebuffer outlineFrameBuffer;
    public Framebuffer glowFrameBuffer;
    public static Animation fadeIn;

    public BooleanComponent(BooleanSetting setting, double width, double height) {
        super(setting, width, height);
        fadeIn = new DecelerateAnimation(250, 1);
        this.setting = setting;
    }

    @Override
    public void drawComponent(final int mouseX, final int mouseY) {
        super.drawComponent(mouseX, mouseY);
        double x1 = x + width / 2 + width / 4 - 4;
        double y1 = y + height / 4 - 1;
        double width1 = width / 4;
        double height1 = height / 2 + 2;
        String toggled = "OFF";
        if (setting.get()) {
            toggled = "ON";
        }

        if(isInside(mouseX, mouseY)) {
            RenderUtil.drawRoundedRect(x1 , y1, width1, height1, 2, setting.get() ? ClickGui.getInstance().color.getColor().darker() : new Color(51, 51, 51).darker());
        } else {
            RenderUtil.drawRoundedRect(x1, y1, width1 , height1, 2, setting.get() ? ClickGui.getInstance().color.getColor() : new Color(51, 51, 51));
        }
        Mega.INSTANCE.getFontManager().getFont("Arial 19").drawCenteredString(toggled, x1 + width1 / 2, y1, setting.get() ? Color.white : new Color(145, 145, 145));
        Mega.INSTANCE.getFontManager().getFont("Arial 19").drawString(setting.getName(), x + 5, y + 5 - 1, -1);

    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (isInside(mouseX, mouseY))
            setting.set(!setting.get());
    }

    public void createFrameBuffers() {
        framebuffer = RenderUtil.createFrameBuffer(framebuffer);
        outlineFrameBuffer = RenderUtil.createFrameBuffer(outlineFrameBuffer);
        glowFrameBuffer = RenderUtil.createFrameBuffer(glowFrameBuffer);
    }
}
