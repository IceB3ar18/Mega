package club.mega.module.impl.visual;

import club.mega.event.impl.EventItemRenderer;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.NumberSetting;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.Color;
@Module.ModuleInfo(name = "CustomItemPos", description = "Changes the item position", category = Category.VISUAL)

public class CustomItemPos extends Module {
    public final NumberSetting posX = new NumberSetting("X", this, -1, 1, 0, 0.01);
    public final NumberSetting posY = new NumberSetting("Y", this, -1, 1, 0, 0.01);
    public final NumberSetting posZ = new NumberSetting("Z", this, -1, 1, 0, 0.01);

    public final NumberSetting blockPosX = new NumberSetting("BlockX", this, -1, 1, 0, 0.01);
    public final NumberSetting blockPosY = new NumberSetting("BlockY", this, -1, 1, 0, 0.01);
    public final NumberSetting blockPosZ = new NumberSetting("BlockZ", this, -1, 1, 0.0, 0.01);
    public final NumberSetting scale = new NumberSetting("Scale", this, 0.0, 2.0, 1, 0.01);



    @Handler
    public void onEventItemRenderer(EventItemRenderer eventItemRenderer) {
        eventItemRenderer.setX(this.posX.getAsDouble());
        eventItemRenderer.setY(this.posY.getAsDouble());
        eventItemRenderer.setZ(this.posZ.getAsDouble());
        eventItemRenderer.setBlockX(this.blockPosX.getAsDouble());
        eventItemRenderer.setBlockY(this.blockPosY.getAsDouble());
        eventItemRenderer.setBlockZ(this.blockPosZ.getAsDouble());
        eventItemRenderer.setScale(this.scale.getAsDouble());
    }
}
