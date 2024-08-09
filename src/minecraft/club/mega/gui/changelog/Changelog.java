package club.mega.gui.changelog;

import club.mega.Mega;
import club.mega.util.animation.AnimationUtil;
import club.mega.util.ColorUtil;
import club.mega.util.MouseUtil;
import club.mega.util.RenderUtil;
import club.mega.util.blur.BloomUtil;
import club.mega.util.blur.GaussianBlur;
import club.mega.util.blur.KawaseBlur;
import club.mega.util.shader.StencilUtil;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Changelog {

    private final ArrayList<Change> changes = new ArrayList<>();
    private double scrollOffset;
    private double current;
    private double x = RenderUtil.getScaledResolution().getScaledWidth() - 300;
    private double y = RenderUtil.getScaledResolution().getScaledHeight() / 2D - 180;
    private final double width = 200;
    private double dragX;
    private double dragY;
    private boolean dragging;

    public Changelog() {
        current = 0;
        scrollOffset = 0;
        changes.clear();
        add(
                //1.7.0
                new Change("1.7.0"),
                new Change("Main Menu", Type.UPDATED),

                new Change(),
                new Change("DiscordRPC", Type.ADDED),
                new Change("Client Manager", Type.ADDED),

                // 1.6.0
                new Change("1.6.0"),
                new Change("ESP", Type.UPDATED),
                new Change("Command System", Type.UPDATED),
                new Change("Module List", Type.UPDATED),
                new Change("KillAura", Type.UPDATED),
                new Change("TickBase Prediction", Type.UPDATED),
                new Change("Glow ESP Modes", Type.UPDATED),
                new Change("AltManager", Type.UPDATED),

                new Change(),
                new Change("WTap", Type.ADDED),
                new Change("Boxy-ESP Modes", Type.ADDED),
                new Change("AntiCheat", Type.ADDED),
                new Change("TickBase", Type.ADDED),
                new Change("Shadows", Type.ADDED),
                new Change(),
                new Change("2D ESP", Type.REMOVED),

                // 1.5.0
                new Change("1.5.0"),
                new Change("Click GUI", Type.UPDATED),
                new Change("FileManager issues", Type.UPDATED),
                new Change("Chest ESP", Type.UPDATED),
                new Change(),
                new Change("Color Wheels", Type.ADDED),
                new Change("NameTags", Type.ADDED),
                new Change("AntiBot", Type.ADDED),
                new Change("TNTRange", Type.ADDED),

                // 1.4.0
                new Change("1.4.0"),
                new Change("Fixed Color issues ", Type.UPDATED),
                new Change("Scaffold", Type.UPDATED),
                new Change("Main Menu", Type.UPDATED),
                new Change("ModuleList", Type.UPDATED),
                new Change(),
                new Change("AntiVoid", Type.ADDED),
                new Change("Regen", Type.ADDED),
                new Change("VClip", Type.ADDED),
                new Change("MCF", Type.ADDED),
                new Change("FastPlace", Type.ADDED),
                new Change("Timer", Type.ADDED),
                new Change("BedFucker", Type.ADDED),
                new Change("Shader ESP", Type.ADDED),
                new Change("Chest ESP", Type.ADDED),
                new Change("TickBase", Type.ADDED),
                new Change("ClientGlow", Type.ADDED),
                new Change("TerrainSpeed", Type.ADDED),
                new Change("AutoTool", Type.ADDED),

                // 1.3.0
                new Change("1.3.0"),
                new Change("HUD & GUI", Type.UPDATED),
                new Change("Scaffold", Type.UPDATED),
                new Change("Fonts", Type.UPDATED),
                new Change("ModuleList", Type.UPDATED),
                new Change("Intelligent Modes", Type.UPDATED),
                new Change("NoBob", Type.UPDATED),

                new Change(),

                new Change("FriendSystem", Type.ADDED),
                new Change("TargetESP", Type.ADDED),
                new Change("Auto Register", Type.ADDED),
                new Change("SuperKnockback", Type.ADDED),
                new Change("NameProtect", Type.ADDED),
                new Change("Inv Manager", Type.ADDED),
                new Change("AutoArmor", Type.ADDED),
                new Change("ChestStealer", Type.ADDED),
                // 1.2.0
                new Change("1.2.0"),
                new Change("Config system", Type.UPDATED),
                new Change("ClickGUI", Type.UPDATED),
                new Change("ESP", Type.UPDATED),
                new Change("ModuleList", Type.UPDATED),

                new Change(),


                new Change("Intave autoblock", Type.ADDED),
                new Change("Intave velocity", Type.ADDED),
                new Change("2D ESP", Type.ADDED),
                new Change("Toggle sounds", Type.ADDED),

                // 1.1.0
                new Change("1.1.0"),
                new Change("ChangeLog", Type.UPDATED),
                new Change("Scaffold", Type.UPDATED),
                new Change("ClickGui", Type.UPDATED),
                new Change("Scaffold", Type.UPDATED),
                new Change("KillAura", Type.UPDATED),
                new Change("Design", Type.UPDATED),

                new Change(),

                new Change("STap", Type.ADDED),
                new Change("NoBob", Type.ADDED),
                new Change("TargetHud", Type.ADDED),
                new Change("Login", Type.ADDED),
                new Change("Animations", Type.ADDED),
                new Change("ConfigSystem", Type.ADDED),
                new Change("Velocity", Type.ADDED),
                new Change("InvMove", Type.ADDED),
                new Change("Step", Type.ADDED),
                new Change("Animations", Type.ADDED),

                // 1.0.0
                new Change("1.0.0"),
                new Change("Changelog", Type.ADDED),
                new Change("TabGui", Type.ADDED),
                new Change("Basic modules", Type.ADDED)
        );
    }

    private void add(final Change... modules) {
        this.changes.addAll(Arrays.asList(modules));
    }

    private void handleScrolling(final int mouseX, final int mouseY, final double x, final double y, final double width, final double height) {
        if (Mouse.hasWheel() && MouseUtil.isInside(mouseX, mouseY, x, y, width, height)) {
            int wheel = Mouse.getDWheel();
            if (wheel < 0) {
                current += 10;
                if (this.current < 0) {
                    this.current = 0;
                }
            } else if (wheel > 0) {
                this.current -= 10;
                if (this.current < 0) {
                    this.current = 0;
                }
            }
            this.scrollOffset = AnimationUtil.animate(scrollOffset, current, 1);
        }
    }

    private Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);

    public final void render(final int mouseX, final int mouseY, final double aDouble) {
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }

        double offset = y + 1 - scrollOffset;
        double height = 300;
        handleScrolling(mouseX, mouseY, x, y, width, height);

        StencilUtil.readStencilBuffer(1);

        String currentMode = "Kawase";
        float radius = 17;
        int iterations = 15;
        int offsetBlur = 2;
        int shadowRadius = 6;
        int shadowOffset = 2;
        switch (currentMode) {
            case "Gaussian":
                GaussianBlur.renderBlur(radius);
                break;
            case "Kawase":
                KawaseBlur.renderBlur(iterations, offsetBlur);
                break;
        }

        StencilUtil.uninitStencilBuffer();

        bloomFramebuffer = RenderUtil.createFrameBuffer(bloomFramebuffer);

        bloomFramebuffer.framebufferClear();
        bloomFramebuffer.bindFramebuffer(true);
        RenderUtil.drawRoundedRect(x, y, width, height, 3, new Color(22,22,22, 160));
        RenderUtil.drawRoundedRect(x + width / 2 - 29, y - 16, 60, 14, 3, ColorUtil.getMainColor());
        RenderUtil.drawRoundedRect(x + width / 2 - 30, y - 17, 60, 14, 3, new Color(22,22,2));

        bloomFramebuffer.unbindFramebuffer();

        BloomUtil.renderBlur(bloomFramebuffer.framebufferTexture, shadowRadius, shadowOffset);
        RenderUtil.drawRoundedRect(x, y, width, height, 3, new Color(22,22,22, 160));


        RenderUtil.drawRoundedRect(x + width / 2 - 29, y - 16, 60, 14, 3, ColorUtil.getMainColor());
        RenderUtil.drawRoundedRect(x + width / 2 - 30, y - 17, 60, 14, 3, new Color(22,22,2));
        Mega.INSTANCE.getFontManager().getFont("Roboto bold 20").drawCenteredString("Changelog", x + width / 2,y - 15,-1);

        GL11.glPushMatrix();
        RenderUtil.prepareScissorBox(0, y + 2, RenderUtil.getScaledResolution().getScaledWidth(), height - 4);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        for (final Change change : changes)
        {
            switch (change.getType())
            {
                case EMPTY:
                    //RenderUtil.drawRect(x + 1, offset + 1, width - 2, 12, new Color(14, 14, 14, 160));
                    offset += aDouble;
                    break;
                case VERSION:
                    if (changes.get(0) != change) {
                        //RenderUtil.drawRect(x + 1, offset + 1, width - 2, 12 / 2, new Color(14, 14, 14, 160));
                        offset += aDouble - 1;
                    }
                    RenderUtil.drawRect(x + 1, offset + 1, width - 2, 12, new Color(14, 14, 14, 160));
                    Mega.INSTANCE.getFontManager().getFont("Roboto bold 20").drawCenteredString("- " + change.getChange() + " -", x + width / 2D, offset + 2, change.getType().getColor());
                    offset += aDouble;
                    break;
                default:
                    RenderUtil.drawCircle(x + 8, offset + 8, 2, change.getType().getColor().getRGB());
                    Mega.INSTANCE.getFontManager().getFont("Arial 19").drawString(change.getType().name().toLowerCase() + ":", x + 13, offset + 3, change.getType().getColor());
                    Mega.INSTANCE.getFontManager().getFont("Arial 19").drawCenteredString(change.getChange(), x + width / 2D, offset + 3, -1);
                    //RenderUtil.drawRect(x + 13, offset + 13, width - 13, 2, new Color(14, 14, 14, 160));
                    offset += aDouble;
                    break;
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }

    public final void mouseClicked(final int mouseX, final int mouseY, final int button) {
        if (MouseUtil.isInside(mouseX, mouseY, x + width / 2 - 30, y - 17, 60, 14) && button == 0) {
            dragging = true;
            dragX = mouseX - x;
            dragY = mouseY - y;
        }
    }

    public void mouseReleased(final int button) {
        if (button == 0 && dragging)
            dragging = false;
    }


    public enum Type {

        ADDED(Color.GREEN), REMOVED(Color.RED), UPDATED(Color.ORANGE), VERSION(Color.WHITE), EMPTY(Color.WHITE);

        private final Color color;

        Type(final Color color) {
            this.color = color;
        }

        public final Color getColor() {
            return color;
        }

    }

}
