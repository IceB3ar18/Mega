package club.mega.gui.clicknew.components;

import club.mega.Mega;
import club.mega.gui.clicknew.SettingComponent;
import club.mega.module.impl.hud.ClickGui;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class TitleComponent extends SettingComponent {

    private final FontRenderer fontRenderer;
    private String text;
    private String name;
    private int key;
    public TitleComponent(String name, String text, int key, double x, double y, double width, double height) {
        super(x, y, width, height);
        this.fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        this.text = text;
        this.key = key;
        this.name = name;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        Mega.INSTANCE.getFontManager().getFont("Arial 30").drawStringWithShadow(name, x, y, ClickGui.getInstance().color.getColor());

        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow("Description: ", x, y + Mega.INSTANCE.getFontManager().getFont("Arial 30").getHeight(name), ClickGui.getInstance().color.getColor());
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(text, x + Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth("Description: "), y + Mega.INSTANCE.getFontManager().getFont("Arial 30").getHeight(name), new Color(199, 199, 199, 255));
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow("Key: ", x, y + 12 + Mega.INSTANCE.getFontManager().getFont("Arial 30").getHeight(name), ClickGui.getInstance().color.getColor());
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(key == 0 ? "not bound" : Keyboard.getKeyName(key) + " (" + key + ")", x + Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth("Key: "), y + 12 + Mega.INSTANCE.getFontManager().getFont("Arial 30").getHeight(name), new Color(199, 199, 199, 255));

    }

    @Override
    public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {

    }


    @Override
    public void handleMouseRelease(int mouseX, int mouseY, int state) {
        // This component does not handle mouse release events directly
    }

    @Override
    public void handleKeyPress(char typedChar, int keyCode) {

    }

}