package club.mega.module.impl.combat;

import club.mega.event.impl.EventPostMotion;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.AuraUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "TickBase", description = "TickBase", category = Category.COMBAT)
public class TickBase extends Module {
    public static EntityPlayer target = null;
    public final NumberSetting range = new NumberSetting("Range", this, 3, 7, 3.2, 0.1);
    private final NumberSetting ticks = new NumberSetting("Ticks", this, 1, 10, 3, 1);
    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"Past", "Future"});
    public int counter = 0;
    public boolean hasBased = false;
    public boolean freezing;

    @Handler
    public final void earlyTick(final EventTick eventTick) {
        this.setTag(ticks.getAsInt() + (ticks.getAsInt() > 1 ? " Ticks" : " Tick"));

    }
    public int getExtraTicks() {
        this.counter--;

        if (this.counter > 0) {
            return -1;
        } else {
            this.freezing = false;


            if (KillAura.getInstance().isToggled() && AuraUtil.getTarget() != null && predict() && !hasBased) {
                this.counter = this.ticks.getAsInt();
                return this.counter;
            } else {
                return 0;
            }
        }
    }

    public boolean predict() {
        if (!hasBased) {
            if (AuraUtil.getTarget() == null || !KillAura.getInstance().isToggled()) {
                return false;
            }

            int ticksAhead = ticks.getAsInt();
            float allowedRange = range.getAsFloat();
            EntityLivingBase target = AuraUtil.getTarget();

            // Aktuelle Positionen und Geschwindigkeiten
            Vector3 playerPos = new Vector3(MC.thePlayer.posX, MC.thePlayer.posY, MC.thePlayer.posZ);
            Vector3 playerSpeed = getPlayerSpeedVec();
            Vector3 targetPos = new Vector3(target.posX, target.posY, target.posZ);
            Vector3 targetSpeed = getEntitySpeedVec(target);

            // Vorhersagen der zukünftigen Positionen
            Vector3 predictedPlayerPos = playerPos.add(playerSpeed.scale(ticksAhead));
            Vector3 predictedTargetPos = targetPos.add(targetSpeed.scale(ticksAhead));

            // Berechnung der zukünftigen Entfernung
            double predictedDistance = predictedPlayerPos.distanceTo(predictedTargetPos);

            // Aktuelle Entfernung
            double actualDistance = playerPos.distanceTo(targetPos);

            // Überprüfung, ob die Vorhersage innerhalb des erlaubten Bereichs liegt

            if (Math.abs(predictedDistance) <= allowedRange) {
                return true;
            }
        }
        return false;
    }

    public Vector3 getEntitySpeedVec(EntityLivingBase entity) {
        return new Vector3(
                entity.posX - entity.lastTickPosX,
                entity.posY - entity.lastTickPosY,
                entity.posZ - entity.lastTickPosZ
        );
    }

    public Vector3 getPlayerSpeedVec() {
        return new Vector3(
                MC.thePlayer.posX - MC.thePlayer.lastTickPosX,
                MC.thePlayer.posY - MC.thePlayer.lastTickPosY,
                MC.thePlayer.posZ - MC.thePlayer.lastTickPosZ
        );
    }

    class Vector3 {
        public double x, y, z;

        public Vector3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vector3 add(Vector3 other) {
            return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
        }

        public Vector3 scale(double scalar) {
            return new Vector3(this.x * scalar, this.y * scalar, this.z * scalar);
        }

        public double distanceTo(Vector3 other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            double dz = this.z - other.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }
    @Handler
    public final void onPostTick(final EventPostMotion eventTick) {
        if (this.freezing) {
            MC.thePlayer.posX = MC.thePlayer.lastTickPosX;
            MC.thePlayer.posY = MC.thePlayer.lastTickPosY;
            MC.thePlayer.posZ = MC.thePlayer.lastTickPosZ;
        }
    }


    public double getDistanceToEntity(EntityLivingBase entity) {
        if(MC.thePlayer != null) {
            Vec3 playerVec = new Vec3(MC.thePlayer.posX, MC.thePlayer.posY + (double) MC.thePlayer.getEyeHeight(), MC.thePlayer.posZ);


            double yDiff = MC.thePlayer.posY - entity.posY;
            double targetY = yDiff > 0.0D ? entity.posY + (double) entity.getEyeHeight() : (-yDiff < (double) MC.thePlayer.getEyeHeight() ? MC.thePlayer.posY + (double) MC.thePlayer.getEyeHeight() : entity.posY);
            Vec3 targetVec = new Vec3(entity.posX, targetY, entity.posZ);
            return playerVec.distanceTo(targetVec) - 0.30000001192092896D;
        }
        return 1;
    }



    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.counter = -1;
        this.freezing = false;
    }
}
