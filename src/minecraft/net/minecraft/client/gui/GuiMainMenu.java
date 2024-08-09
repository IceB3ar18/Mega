package net.minecraft.client.gui;

import club.mega.Mega;
import club.mega.gui.clientManager.ClientManager;
import club.mega.util.ColorUtil;
import club.mega.util.GuiBlurUtil;
import club.mega.util.RenderUtil;
import club.mega.util.RoundedButton;
import club.mega.util.animation.AnimationUtil;
import club.mega.util.blur.BloomUtil;
import club.mega.util.blur.GaussianBlur;
import club.mega.util.blur.KawaseBlur;
import club.mega.util.glsl.BGShaderUtil;
import club.mega.util.shader.StencilUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback {
    private static final Logger logger = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private final Object threadLock = new Object();
    private String openGLWarning1;
    private String openGLWarning2;
    private String openGLWarningLink;
    private static final ResourceLocation splashTexts = new ResourceLocation("texts/splashes.txt");

    public static final String field_96138_a = "Please click " + EnumChatFormatting.UNDERLINE + "here" + EnumChatFormatting.RESET + " for more information.";
    private int field_92024_r;
    private int field_92023_s;
    private int field_92022_t;
    private int field_92021_u;
    private int field_92020_v;
    private int field_92019_w;
    private boolean field_183502_L;
    private GuiScreen field_183503_M;
    private double current;


    public GuiMainMenu() {
        this.openGLWarning2 = field_96138_a;
        this.field_183502_L = false;
        this.openGLWarning1 = "";

        if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported()) {
            this.openGLWarning1 = I18n.format("title.oldgl1", new Object[0]);
            this.openGLWarning2 = I18n.format("title.oldgl2", new Object[0]);
            this.openGLWarningLink = "https://help.mojang.com/customer/portal/articles/325948?ref=game";
        }


        BGShaderUtil.getInstance().setup();
    }


    public boolean doesGuiPauseGame() {
        return false;
    }

    public void initGui() {
        Mega.INSTANCE.getDiscordRP().update("Idle", "Main Menu");
        int j = this.height / 4 + 48;

        this.buttonList.add(new RoundedButton(1, this.width / 2 - 50, j, 100, 20, ColorUtil.getMainColor(), ColorUtil.getMainColor(200), 3, I18n.format("menu.singleplayer", new Object[0])));
        this.buttonList.add(new RoundedButton(2, this.width / 2 - 50, j + 24 * 1, 100, 20, ColorUtil.getMainColor(), ColorUtil.getMainColor(200), 3, I18n.format("menu.multiplayer", new Object[0])));
        this.buttonList.add(new RoundedButton(15, this.width / 2 - 50, j + 24 * 2,100, 20, ColorUtil.getMainColor(), ColorUtil.getMainColor(200), 3, "Client Manager"));
        this.buttonList.add(new RoundedButton(0, this.width / 2 - 50, j + 24 * 3, 100, 20, ColorUtil.getMainColor(), ColorUtil.getMainColor(200), 3, I18n.format("Options", new Object[0])));
        this.buttonList.add(new RoundedButton(4, this.width / 2 - 50, j + 24 * 4, 100, 20, ColorUtil.getMainColor(), ColorUtil.getMainColor(200), 3, I18n.format("menu.quit", new Object[0])));

        synchronized (this.threadLock) {
            this.field_92023_s = this.fontRendererObj.getStringWidth(this.openGLWarning1);
            this.field_92024_r = this.fontRendererObj.getStringWidth(this.openGLWarning2);
            int k = Math.max(this.field_92023_s, this.field_92024_r);
            this.field_92022_t = (this.width - k) / 2;
            this.field_92021_u = (int) ((this.buttonList.get(0)).yPosition - 24);
            this.field_92020_v = this.field_92022_t + k;
            this.field_92019_w = this.field_92021_u + 24;
        }

        this.mc.setConnectedToRealms(false);

        current = 0.5;
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }

        if (button.id == 1) {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }

        if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }

        if (button.id == 15) {
            this.mc.displayGuiScreen(new ClientManager(this));
        }

        if (button.id == 4) {
            this.mc.shutdown();
        }
    }


    private Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int y = this.height / 4 + 38;
        ScaledResolution sr = new ScaledResolution(mc);
        BGShaderUtil.getInstance().render(width, height, mouseX, mouseY);
        Mega.INSTANCE.getChangelog().render(mouseX, mouseY, 13);
        // Slower animation function
        current = AnimationUtil.animate(current, 1, 0.025); // Speed adjusted

        // Animation variables
        double animProgress = Math.pow(current, 2); // Using quadratic animation for smooth start and slow end
        double animWidth = 120 * animProgress;
        double animHeight = 139 * animProgress + Mega.INSTANCE.getFontManager().getFont("Roboto bold 80").getHeight(Mega.INSTANCE.getName()) * animProgress + 10;
        double animX = width / 2D - (animWidth + 20 * animProgress) / 2; // Centering the rectangle
        double animY = height / 4.5 - 5 * animProgress;

        GuiBlurUtil.drawBlurredRect((float) animX, (float) animY, (float) (animWidth + 20 * animProgress), (float) animHeight, 5, new Color(1, 1, 1, 140));

        super.drawScreen(mouseX, mouseY, partialTicks);

        Mega.INSTANCE.getFontManager().getFont("Roboto bold 80").drawCenteredString(Mega.INSTANCE.getName(), width / 2D, animY + 10 * animProgress, new Color(255, 255, 255));
        Mega.INSTANCE.getFontManager().getFont("Arial 22").drawStringWithShadow("v" + Mega.INSTANCE.getVersion(), (float) width / 2 + 40 * animProgress, (float) animY + 10 * animProgress + 30, new Color(170, 170, 170).getRGB());

        Mega.INSTANCE.getFontManager().getFont("Roboto bold 18").drawString(Mega.INSTANCE.getName(), 2, height - 10, ColorUtil.getMainColor());
        Mega.INSTANCE.getFontManager().getFont("Arial 10").drawString("v" + Mega.INSTANCE.getVersion(), Mega.INSTANCE.getFontManager().getFont("Roboto bold 18").getWidth(Mega.INSTANCE.getName()) - 4, height - 14, new Color(255, 255, 255, 180).getRGB());
        Mega.INSTANCE.getFontManager().getFont("Arial 18").drawString("by " + Mega.INSTANCE.getDev(), 2 + Mega.INSTANCE.getFontManager().getFont("Roboto bold 18").getWidth(Mega.INSTANCE.getName()) + 8, height - 10, new Color(255, 255, 255, 180).getRGB());
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        Mega.INSTANCE.getChangelog().mouseClicked(mouseX, mouseY, mouseButton);
        synchronized (this.threadLock) {
            if (this.openGLWarning1.length() > 0 && mouseX >= this.field_92022_t && mouseX <= this.field_92020_v && mouseY >= this.field_92021_u && mouseY <= this.field_92019_w) {
                GuiConfirmOpenLink guiconfirmopenlink = new GuiConfirmOpenLink(this, this.openGLWarningLink, 13, true);
                guiconfirmopenlink.disableSecurityWarning();
                this.mc.displayGuiScreen(guiconfirmopenlink);
            }
        }

    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        Mega.INSTANCE.getChangelog().mouseReleased(state);
    }

    public void onGuiClosed() {
        if (this.field_183503_M != null) {
            this.field_183503_M.onGuiClosed();
        }
    }
}
