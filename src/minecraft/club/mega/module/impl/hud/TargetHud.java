package club.mega.module.impl.hud;

import club.mega.Mega;
import club.mega.event.impl.*;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.combat.KillAura;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ColorSetting;
import club.mega.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;
import java.util.List;

@Module.ModuleInfo(name = "TargetHud", description = "Displays target information", category = Category.HUD)
public class TargetHud extends Module {

    private double x, y, dragX, dragY, currentScale, healthBarWidth;
    private boolean dragging;
    private boolean hasNoArmor = false;

    public final BooleanSetting blur = new BooleanSetting("Blur", this, true);
    public final ColorSetting backgroundColor = new ColorSetting("Background Color", this, new Color(0, 0, 0, 107));
    public final ColorSetting healthColor = new ColorSetting("Health Color", this, new Color(27, 202, 255));

    @Override
    public void onEnable() {
        super.onEnable();
        resetPosition();
    }

    private void resetPosition() {
        currentScale = 0.7;
        x = RenderUtil.getScaledResolution().getScaledWidth() / 2D;
        y = RenderUtil.getScaledResolution().getScaledHeight() / 2D;
    }

    @Handler
    public void onRender2D(EventRender2D event) {
        EntityLivingBase entity = getTargetEntity();
        if (entity == null) {
            currentScale = 0.7;
            return;
        }

        currentScale = 1;
        healthBarWidth = Math.min(111, (entity.getHealth() / entity.getMaxHealth()) * 109.8);

        GL11.glPushMatrix();
        GL11.glScaled(currentScale, currentScale, currentScale);
        renderTargetHud(entity);
        GL11.glPopMatrix();
    }

    private EntityLivingBase getTargetEntity() {
        if (MC.currentScreen instanceof GuiChat) {
            return MC.thePlayer;
        }
        return (EntityLivingBase) AuraUtil.getTarget();
    }

    private void renderTargetHud(EntityLivingBase entity) {
        renderBackground();
        renderHealthBar(entity);
        renderEntityInfo(entity);
        renderArmorValues((int) (x + 50), (int) (y - 2));
    }

    private void renderBackground() {
        RenderUtil.drawRoundedRect(x + 10, y - 20, 117, 48, 4, backgroundColor.getColor());
    }

    private void renderHealthBar(EntityLivingBase entity) {
        RenderUtil.drawRoundedRect(x + 13, y + 16, healthBarWidth, 8, 2, healthColor.getColor());
    }

    private void renderEntityInfo(EntityLivingBase entity) {
        Mega.INSTANCE.getFontManager().getFont("Roboto medium 20").drawString(entity.getName(), x + 53, y - 14, -1);
        renderEntitySkin(entity);
        if (hasNoArmor) {
            Mega.INSTANCE.getFontManager().getFont("Arial 20").drawString("Health: " + entity.getHealth(), x + 53, y, -1);
        }
    }

    private void renderEntitySkin(EntityLivingBase entity) {
        List<NetworkPlayerInfo> networkPlayers = GuiPlayerTabOverlay.field_175252_a.sortedCopy(MC.thePlayer.sendQueue.getPlayerInfoMap());
        for (NetworkPlayerInfo playerInfo : networkPlayers) {
            if (MC.theWorld.getPlayerEntityByUUID(playerInfo.getGameProfile().getId()) == entity) {
                GlStateManager.enableCull();
                MC.getTextureManager().bindTexture(playerInfo.getLocationSkin());
                Gui.drawScaledCustomSizeModalRect((int) x + 17, (int) y - 15, 8.0F, 8.0F, 8, 8, 26, 26, 64.0F, 66.0F);
                break;
            }
        }
    }

    private void renderArmorValues(int x, int y) {
        int noArmorCount = 0;
        int armorSlots = 3;
        EntityLivingBase target = AuraUtil.getTarget();
        if (target instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) target;
            for (int i = 0; i < MC.thePlayer.inventory.armorInventory.length; i++) {
                ItemStack itemStack = player.getCurrentArmor(i);
                if (itemStack == null) {
                    noArmorCount++;
                }
                renderArmorSlot(armorSlots - i, itemStack, x, y, 16);
            }
        }
        hasNoArmor = (noArmorCount == 4);
    }

    private void renderArmorSlot(int slotIndex, ItemStack itemStack, int x, int y, int offset) {
        if (itemStack != null) {
            RenderHelper.enableGUIStandardItemLighting();
            MC.getRenderItem().renderItemAndEffectIntoGUI(itemStack, x + (slotIndex * offset), y);
        }
    }

    @Handler
    public void onBlur(EventBlur event) {
        EntityLivingBase entity = getTargetEntity();
        if (entity != null && blur.get()) {
            renderBackground();
        }
    }

    @Handler
    public void onMouseClicked(EventMouseClicked event) {
        if (!(MC.currentScreen instanceof GuiChat)) return;

        if (MouseUtil.isInside(event.getMouseX(), event.getMouseY(), x + 10, y - 20, 117, 41) && event.getMouseButton() == 0) {
            startDragging(event);
        }
    }

    private void startDragging(EventMouseClicked event) {
        dragging = true;
        dragX = event.getMouseX() - x;
        dragY = event.getMouseY() - y;
    }

    @Handler
    public void onMouseReleased(EventMouseReleased event) {
        if (MC.currentScreen instanceof GuiChat && event.getMouseButton() == 0) {
            dragging = false;
        }
    }

    @Handler
    public void onDrawGuiScreen(EventDrawGuiScreen event) {
        if (MC.currentScreen instanceof GuiChat && dragging) {
            updatePosition(event);
        }
    }

    private void updatePosition(EventDrawGuiScreen event) {
        x = event.getMouseX() - dragX;
        y = event.getMouseY() - dragY;
    }
}
