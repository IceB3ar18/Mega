package club.mega.gui.clientManager;

import club.mega.Mega;
import club.mega.file.ModuleSaver;
import club.mega.util.ColorPicker;
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

public class ClientColor extends GuiScreen {
    private GuiScreen parent;
    private ColorPicker colorPicker;
    private ClientBackground clientBackground;
    private ClientColor clientColor;
    public ClientColor(GuiScreen parent) {
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

        int scaledHeight = sr.getScaledHeight();
        int startHeight = Math.min(40 + scaledHeight / 7, 135);
        this.buttonList.add(new GuiButton(6, 20, startHeight, 100, 20, "Backgrounds"));
        this.buttonList.add(new GuiButton(7, 20, scaledHeight - scaledHeight / 10, 100, 20, "Back"));
        this.buttonList.add(new GuiButton(8, 20, startHeight + 30, 100, 20, "ColorOptions"));
        this.buttonList.add(new GuiButton(9, 20, startHeight + 60, 100, 20, "AltManager"));
        this.buttonList.add(new GuiButton(69, 20, startHeight + 30 * 3, 100, 20, "ViaVersion"));

        // Erstelle einen neuen Color Picker und positioniere ihn im GUI
        this.colorPicker = new ColorPicker(100, ColorUtil.getMainColor(), this::setColorFromPicker);
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 6) {
            this.mc.displayGuiScreen(this.clientBackground.start(this));
        }

        if (button.id == 7) {
            this.mc.displayGuiScreen(new GuiMainMenu());
        }

        if (button.id == 8) {
            this.mc.displayGuiScreen(this.clientColor.start(this));
        }

        if (button.id == 9) {
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

        int scaledHeight = sr.getScaledHeight();
        RenderUtil.drawRect(0, 0, 130, scaledHeight, new Color(1, 1, 1, 140));
        int height = sr.getScaledHeight() / 2;
        int bgHeight = (100 * 2 + 120);
        Mega.INSTANCE.getFontManager().getFont("Roboto bold 80").drawCenteredString("Color", width / 2D, 30, ColorUtil.getMainColor());

        // Zeichne den Color Picker
        this.colorPicker.draw(mouseX, mouseY);

        //Mega.INSTANCE.getFontManager().getFont("Roboto bold 20").drawCenteredString("Color", width / 2D + 3, height - bgHeight / 2 + 30- 10 - Mega.INSTANCE.getFontManager().getFont("Roboto bold 20").getHeight("Color"), -1);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        // Überprüfe, ob der Mausklick innerhalb des Color Pickers liegt
        this.colorPicker.click(mouseX, mouseY, mouseButton);
        ModuleSaver.saveClientState();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    // Methode, um die Hauptfarbe vom Color Picker zu aktualisieren
    public void setColorFromPicker(Color color) {
        ColorUtil.setMainColor(color);
    }
}
