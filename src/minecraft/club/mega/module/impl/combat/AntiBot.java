package club.mega.module.impl.combat;

import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.util.ChatUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import rip.hippo.lwjeb.annotation.Handler;

import java.util.ArrayList;
import java.util.List;

@Module.ModuleInfo(name = "AntiBot", description = "AntiBot", category = Category.COMBAT)
public class AntiBot extends Module {
    private int matrixTicks = 0;
    public static List<EntityPlayer> bots = new ArrayList<>();
    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"Matrix"});



    @Handler
    public final void onTick(final EventTick eventTick) {
    }

    public boolean canAttack(EntityLivingBase entity) {
        boolean canattack = false;
       if (this.mode.is("Matrix")) {
            if (this.matrixCanAttack(entity)) {
                canattack = true;
            } else {
                canattack = false;
                ChatUtil.sendMessage("Matrix Antibot: Detected bot [" + entity + "]");

            }
        }

        return canattack;
    }


    public boolean matrixCanAttack(EntityLivingBase i) {
        boolean matrixcanattack = false;
        ++this.matrixTicks;
        double oldPosX = i.posX;
        double oldPosZ = i.posZ;
        if (this.matrixTicks > 15) {
            double xDiff = oldPosX - i.posX;
            double zDiff = oldPosZ - i.posZ;
            if (Math.sqrt(xDiff * xDiff + zDiff * zDiff) > 4.7D && i.posY > MC.thePlayer.posY - 1.5D && i.posY < MC.thePlayer.posY + 1.5D && MC.thePlayer.getDistanceToEntity(i) < 6.0F && i != MC.thePlayer) {
                matrixcanattack = false;
            }
        } else {
            matrixcanattack = true;
        }

        return matrixcanattack;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.matrixTicks = 0;
    }
}
