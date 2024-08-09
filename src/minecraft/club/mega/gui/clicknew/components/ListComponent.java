package club.mega.gui.clicknew.components;

import club.mega.Mega;
import club.mega.gui.clicknew.SettingComponent;
import club.mega.module.impl.hud.ClickGui;
import club.mega.module.setting.impl.ListSetting;
import club.mega.util.RenderUtil;

import java.awt.Color;

public class ListComponent extends SettingComponent {

    private final ListSetting setting;

    public ListComponent(final ListSetting setting, final double x, final double y, final double width, final double height) {
        super(x, y, width, height);
        this.setting = setting;
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(setting.getName() + ": ", (int) x , (int) (y + 5), new Color(199, 199, 199, 255));


        double modeX =  Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth(setting.getName() + ": ");
        String[] modes = setting.getModes();
        for (int i = 0; i < modes.length; i++) {
            String mode = modes[i];
            Mega.INSTANCE.getFontManager().getFont("Arial 18").drawStringWithShadow(mode + (setting.getModes().length -1 == i ? "" : ", "), (float) x + modeX, (float) (y + 5), setting.getCurrent().equals(mode) ? ClickGui.getInstance().color.getColor().getRGB() : new Color(199, 199, 199, 255).getRGB());
            modeX += Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth(mode + (setting.getModes().length -1 == i ? "" : ", "));
        }
    }

    @Override
    public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        double modeX = x + Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth(setting.getName() + ": ");
        String[] modes = setting.getModes();
        for (int i = 0; i < modes.length; i++) {
            String mode = modes[i];
            double modeWidth = Mega.INSTANCE.getFontManager().getFont("Arial 18").getWidth(mode + (setting.getModes().length - 1 == i ? "" : ", "));
            if (mouseX >= modeX && mouseX <= modeX + modeWidth && mouseY >= y && mouseY <= y + 20) { // Adjust the y + 20 based on your font height and padding
                setting.setCurrent(mode);
                break;
            }
            modeX += modeWidth;
        }
    }


    @Override
    public void handleMouseRelease(int mouseX, int mouseY, int state) {

    }

    @Override
    public void handleKeyPress(char typedChar, int keyCode) {

    }
}
