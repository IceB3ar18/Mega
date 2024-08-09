
package club.mega.gui.clientManager;

import club.mega.Mega;
import club.mega.file.ModuleSaver;
import club.mega.util.ColorUtil;
import club.mega.util.RenderUtil;
import club.mega.util.glsl.BGShaderUtil;
import de.florianmichael.viamcp.gui.GuiProtocolSelector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.Iterator;

public class ClientBackground extends GuiScreen {
    private GuiScreen parent;
    private ClientBackground clientBackground;
    private ClientColor clientColor;
    public ClientBackground(GuiScreen parent) {
        this.parent = parent;
    }

    public GuiScreen start(GuiScreen parent) {
        BGShaderUtil.getInstance().setup();
        this.parent = parent;
        this.clientBackground = new ClientBackground(new GuiMainMenu());
        this.clientColor = new ClientColor(new GuiMainMenu());
        return this;
    }

    public void initGui() {
        super.initGui();
        ScaledResolution sr = new ScaledResolution(this.mc);
        int i = 0;
        int j = 2;

        int scaledHeight = sr.getScaledHeight();
        int startHeight = Math.min(40 + scaledHeight / 7, 135);
        this.buttonList.add(new GuiButton(124234, 20, startHeight, 100, 20, "Backgrounds"));
        this.buttonList.add(new GuiButton(143247, 20, scaledHeight - scaledHeight / 10, 100, 20, "Back"));
        this.buttonList.add(new GuiButton(42133, 20, startHeight + 30, 100, 20, "ColorOptions"));
        this.buttonList.add(new GuiButton(3242134, 20, startHeight + 60, 100, 20, "AltManager"));
        this.buttonList.add(new GuiButton(69, 20, startHeight + 30 * 3, 100, 20, "ViaVersion"));
        for(Iterator var4 = BGShaderUtil.getFileNamesWithoutExtension().iterator(); var4.hasNext(); ++j) {
            String s = (String)var4.next();
            float x = (float)sr.getScaledWidth() / 2.0F - (float)this.fontRendererObj.getStringWidth(s) / 2.0F;
            float y = (float)(Math.min(40 + sr.getScaledHeight() / 7, 135) + i);
            float widthh = (float)this.fontRendererObj.getStringWidth(s);
            float heightt = 11.0F;
            this.buttonList.add(new BGShaderButton(j, (int)x, (int)y, (int)widthh, (int)heightt, s, new Color(139, 141, 145, 255), new Color(67, 122, 163, 255)));
            i += 13;
        }

    }

    protected void actionPerformed(GuiButton button) throws IOException {
        Iterator var2 = BGShaderUtil.getFileNamesWithoutExtension().iterator();
        while(var2.hasNext()) {
            String s = (String)var2.next();
            if (button.displayString.equals(s)) {
                BGShaderUtil.getInstance().setCurrentShader(s + ".fsh");
                ModuleSaver.saveClientState();
                BGShaderUtil.getInstance().setup();

            }
        }
        if (button.id == 124234) {
            this.mc.displayGuiScreen(this.clientBackground.start(this));
        }

        if (button.id == 143247) {
            this.mc.displayGuiScreen(new GuiMainMenu());
        }

        if (button.id == 42133) {
            this.mc.displayGuiScreen(this.clientColor.start(this));
        }
        if (button.id == 3242134) {
            this.mc.displayGuiScreen(Mega.INSTANCE.getAltManager());
        }
        if (button.id == 69)
        {
            this.mc.displayGuiScreen(new GuiProtocolSelector(this));
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        BGShaderUtil.getInstance().render(width, height, mouseX, mouseY);
        ScaledResolution sr = new ScaledResolution(this.mc);
        RenderUtil.drawRoundedRect(width / 2D - 110, Math.min(40 + sr.getScaledHeight() / 7, 135) - 10, 220, BGShaderUtil.getFileNamesWithoutExtension().size() * 13 + 20, 5, new Color(20,20,20));

        int scaledHeight = sr.getScaledHeight();
        RenderUtil.drawRect(0, 0, 130, scaledHeight, new Color(1, 1, 1, 140));
        Mega.INSTANCE.getFontManager().getFont("Roboto bold 80").drawCenteredString("Background", width / 2D, 30, ColorUtil.getMainColor());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }
}
