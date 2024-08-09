package club.mega.module.impl.visual;

import club.mega.event.impl.EventPreTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.impl.combat.KillAura;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.AuraUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import rip.hippo.lwjeb.annotation.Handler;
@Module.ModuleInfo(name = "AttackEffects", description = "Adds attack effects", category = Category.VISUAL)
public class AttackEffects extends Module {
    @Handler
    public final void onRender(final EventPreTick event) {

        Entity target = AuraUtil.getTarget();
        if (target != null && target instanceof EntityLivingBase) {

            if (KillAura.getInstance().isToggled()) {
                target = AuraUtil.getTarget();
                MC.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.SPELL_MOB);
                event.setCancelled(true);

            }
        }
    }

    
}
