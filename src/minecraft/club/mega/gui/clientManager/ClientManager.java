package club.mega.gui.clientManager;

import java.awt.Color;
import java.io.IOException;

import club.mega.Mega;
import club.mega.util.ColorUtil;
import club.mega.util.RenderUtil;
import club.mega.util.animation.AnimationUtil;
import club.mega.util.glsl.BGShaderUtil;
import club.mega.util.glsl.GLSLSandboxShader;
import de.florianmichael.viamcp.ViaMCP;
import de.florianmichael.viamcp.gui.GuiProtocolSelector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;


public class ClientManager extends GuiScreen {
    private GuiScreen parent;
    private final ClientBackground clientBackground;
    private final ClientColor clientColor;
    private double current;
    public ClientManager(GuiScreen parent) {
        this.parent = parent;
        this.clientBackground = new ClientBackground(new GuiMainMenu());
        this.clientColor = new ClientColor(new GuiMainMenu());
        BGShaderUtil.getInstance().setup();
    }

    public GuiScreen start(GuiScreen parent) {
        this.parent = parent;
        return this;
    }

    public void initGui() {
        super.initGui();
        ScaledResolution sr = new ScaledResolution(this.mc);
        int scaledHeight = sr.getScaledHeight();
        int startHeight = Math.min(40 + scaledHeight / 7, 135);
        this.buttonList.add(new GuiButton(1, 20, startHeight, 100, 20, "Backgrounds"));
        this.buttonList.add(new GuiButton(2, 20, scaledHeight - scaledHeight / 10, 100, 20, "Back"));
        this.buttonList.add(new GuiButton(3, 20, startHeight + 30, 100, 20, "ColorOptions"));
        this.buttonList.add(new GuiButton(4, 20, startHeight + 30 * 2, 100, 20, "AltManager"));
        this.buttonList.add(new GuiButton(69, 20, startHeight + 30 * 3, 100, 20, "ViaVersion"));
        current = 0.5;
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            this.mc.displayGuiScreen(this.clientBackground.start(this));
        }

        if (button.id == 2) {
            this.mc.displayGuiScreen(this.parent);
        }

        if (button.id == 3) {
            this.mc.displayGuiScreen(this.clientColor.start(this));
        }

        if (button.id == 4) {
            this.mc.displayGuiScreen(Mega.INSTANCE.getAltManager());
        }

        if (button.id == 69)
        {
            this.mc.displayGuiScreen(new GuiProtocolSelector(this));
        }

        super.actionPerformed(button);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        current = AnimationUtil.animate(current, 1, 0.02);
        GL11.glPushMatrix();
        GL11.glScaled(current, current, 1);
        ScaledResolution sr = new ScaledResolution(mc);
        BGShaderUtil.getInstance().render(width, height, mouseX, mouseY);


        int scaledHeight = sr.getScaledHeight();
        RenderUtil.drawRect(0, 0, 130, scaledHeight, new Color(1, 1, 1, 140));

        super.drawScreen(mouseX, mouseY, partialTicks);
        //Mega.INSTANCE.getFontManager().getFont("Roboto bold 80").drawCenteredString("ClientManager", width / 2D, 30, ColorUtil.getMainColor());
        GL11.glPopMatrix();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }
}
