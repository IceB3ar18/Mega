package de.florianmichael.viamcp.gui;

import club.mega.Mega;
import club.mega.gui.clientManager.ClientBackground;
import club.mega.gui.clientManager.ClientColor;
import club.mega.util.ChatUtil;
import club.mega.util.RenderUtil;
import club.mega.util.glsl.BGShaderUtil;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.protocolinfo.ProtocolInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.io.IOException;

public class GuiProtocolSelector extends GuiScreen {
    private final GuiScreen parent;
    public SlotList list;
    private ClientBackground clientBackground;
    private ClientColor clientColor;
    public GuiProtocolSelector(GuiScreen parent) {
        this.parent = parent;
        BGShaderUtil.getInstance().setup();
        this.clientBackground = new ClientBackground(new GuiMainMenu());
        this.clientColor = new ClientColor(new GuiMainMenu());
    }

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sr = new ScaledResolution(this.mc);
        int scaledHeight = sr.getScaledHeight();
        int startHeight = Math.min(40 + scaledHeight / 7, 135);
        this.buttonList.add(new GuiButton(124234, 20, startHeight, 100, 20, "Backgrounds"));
        this.buttonList.add(new GuiButton(143247, 20, scaledHeight - scaledHeight / 10, 100, 20, "Back"));
        this.buttonList.add(new GuiButton(42133, 20, startHeight + 30, 100, 20, "ColorOptions"));
        this.buttonList.add(new GuiButton(3242134, 20, startHeight + 60, 100, 20, "AltManager"));
        this.buttonList.add(new GuiButton(69, 20, startHeight + 30 * 3, 100, 20, "ViaVersion"));
        list = new SlotList(mc, width, height, 32, height - 32);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        list.actionPerformed(button);
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

    @Override
    public void handleMouseInput() throws IOException {
        list.handleMouseInput();
        super.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        BGShaderUtil.getInstance().render(width, height, mouseX, mouseY);
        list.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0, 2.0, 2.0);
        String title = EnumChatFormatting.BOLD + "ViaMCP";
        drawString(this.fontRendererObj, title, (this.width - (this.fontRendererObj.getStringWidth(title) * 2)) / 4, 5, -1);
        GlStateManager.popMatrix();

        ScaledResolution sr = new ScaledResolution(this.mc);

        int scaledHeight = sr.getScaledHeight();
        RenderUtil.drawRect(0, 60, 130, scaledHeight, new Color(1, 1, 1, 140));

        drawString(this.fontRendererObj, "by EnZaXD/Flori2007", 1, 1, -1);
        drawString(this.fontRendererObj, "Discord: EnZaXD#6257", 1, 11, -1);

        final ProtocolInfo protocolInfo = ProtocolInfo.fromProtocolVersion(ViaLoadingBase.getInstance().getTargetVersion());

        final String versionTitle = "Version: " + ViaLoadingBase.getInstance().getTargetVersion().getName() + " - " + protocolInfo.getName();
        final String versionReleased = "Released: " + protocolInfo.getReleaseDate();

        final int fixedHeight = ((5 + this.fontRendererObj.FONT_HEIGHT) * 2) + 2;

        drawString(this.fontRendererObj, EnumChatFormatting.GRAY + (EnumChatFormatting.BOLD + "Version Information"), (width - this.fontRendererObj.getStringWidth("Version Information")) / 2, fixedHeight, -1);
        drawString(this.fontRendererObj, versionTitle, (width - this.fontRendererObj.getStringWidth(versionTitle)) / 2, fixedHeight + this.fontRendererObj.FONT_HEIGHT, -1);
        drawString(this.fontRendererObj, versionReleased, (width - this.fontRendererObj.getStringWidth(versionReleased)) / 2, fixedHeight + this.fontRendererObj.FONT_HEIGHT * 2, -1);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class SlotList extends GuiSlot {

        public SlotList(Minecraft mc, int width, int height, int top, int bottom) {
            super(mc, width, height, top + 30, bottom, 18);
        }

        @Override
        protected int getSize() {
            return ViaLoadingBase.getProtocols().size();
        }

        @Override
        protected void elementClicked(int i, boolean b, int i1, int i2) {
            final ProtocolVersion protocolVersion = ViaLoadingBase.getProtocols().get(i);
            ViaLoadingBase.getInstance().reload(protocolVersion);
        }

        @Override
        protected boolean isSelected(int i) {
            return false;
        }

        @Override
        protected void drawBackground() {
            drawDefaultBackground();
        }

        @Override
        protected void drawSlot(int i, int i1, int i2, int i3, int i4, int i5) {
            drawCenteredString(mc.fontRendererObj,(ViaLoadingBase.PROTOCOLS.indexOf(ViaLoadingBase.getInstance().getTargetVersion()) == i ? EnumChatFormatting.GREEN.toString() + EnumChatFormatting.BOLD : EnumChatFormatting.GRAY.toString()) + ViaLoadingBase.getProtocols().get(i).getName(), width / 2, i2 + 2, -1);
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 0.5);
            drawCenteredString(mc.fontRendererObj, "PVN: " + ViaLoadingBase.getProtocols().get(i).getVersion(), width, (i2 + 2) * 2 + 20, -1);
            GlStateManager.popMatrix();
        }
    }
}