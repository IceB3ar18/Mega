package club.mega.gui.click;

import club.mega.Mega;
import club.mega.file.ModuleSaver;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.hud.ClickGui;
import club.mega.util.ColorUtil;
import club.mega.util.RenderUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ClickGUI extends GuiScreen {

    private final ArrayList<Panel> panels = new ArrayList<>();
    private double current;

    public ClickGUI() {
        for (final Category category : Category.values())
        {
            final Panel panel = new Panel(category);
            panel.setX(150 * (Arrays.asList(Category.values()).indexOf(category)) + 20);
            panel.setWidth(110);
            panel.setY(50);
            for (final Module module : Mega.INSTANCE.getModuleManager().getModules(category))
            {
                panel.add(module, 20);
            }
            panels.add(panel);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        current = -(height / 2D);

        if (Mega.INSTANCE.getModuleManager().getModule(ClickGui.class).blur.get())
            mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));

        buttonList.clear();
        buttonList.add(new GuiButton(69, 5, height - 25, 50, 20, "Fix gui"));
        ScaledResolution sr = new ScaledResolution(this.mc);
        this.buttonList.add(new GuiButton(2142132, sr.getScaledWidth() - 70, sr.getScaledHeight() - 40, 50, 20, "Configs"));
        for (final Panel panel : panels)
        {
            panel.init();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        current = 1;
        RenderUtil.drawGradientRect(0, height / 7D, width, height, ColorUtil.getMainColor(0).darker(), ColorUtil.getMainColor().darker());
        super.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glPushMatrix();
        //GL11.glScaled(current, current, 1);
        GL11.glTranslated(0, current, 0);
        panels.forEach(panel -> {
            panel.drawScreen(mouseX, mouseY);
        });
        GL11.glPopMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        panels.forEach(panel -> {
            panel.mouseClicked(mouseX, mouseY, mouseButton);
        });
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        panels.forEach(panel -> {
            panel.mouseReleased(mouseX, mouseY, state);
        });
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        panels.forEach(panel -> {
            try {
                panel.keyTyped(typedChar, keyCode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == 69) {
            Mega.INSTANCE.fixClickGui();
        }
        if(button.id == 2142132) {
            this.mc.displayGuiScreen(new ConfigGui(this));
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Mega.INSTANCE.getModuleManager().getModule(ClickGui.class).setToggled(false);
        mc.entityRenderer.stopUseShader();
        ModuleSaver.save();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public final double getCurrent() {
        return current;
    }
}
