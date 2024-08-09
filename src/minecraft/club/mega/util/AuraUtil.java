package club.mega.util;

import club.mega.Mega;
import club.mega.interfaces.MinecraftInterface;
import club.mega.module.impl.combat.AntiBot;
import club.mega.module.impl.combat.KillAura;
import club.mega.module.impl.misc.Friends;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.MovingObjectPosition;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class AuraUtil implements MinecraftInterface {

    private final ArrayList<EntityLivingBase> targets = new ArrayList<>();
    private static EntityLivingBase target;
    private final static KillAura killAura = Mega.INSTANCE.getModuleManager().getModule(KillAura.class);
    private static int targetIndex, finalCps = 0, blockTicks;
    private static final TimeUtil switchTimer = new TimeUtil();
    private static final TimeUtil attackTimer = new TimeUtil();
    private static boolean blocking;
    private static long randomDelay = 100L;
    private static float range, preRange;
    public static boolean leftToBackRotate = false;

    public static boolean isLeftToBackRotate() {
        return leftToBackRotate;
    }

    public static void setLeftToBackRotate(boolean leftToBackRotate) {
        AuraUtil.leftToBackRotate = leftToBackRotate;
    }

    public static void setTargets() {
        target = null;
        List<EntityLivingBase> livingEntities = getLivingEntities();

        if (killAura.priority.is("health"))
            livingEntities.sort(new HealthSorter());
        else
            livingEntities.sort(new DistanceSorter());
        if (livingEntities.size() > 0) {
            if (killAura.mode.is("single"))
                targetIndex = 0;
            else {
                if (switchTimer.hasTimePassed(killAura.switchDelay.getAsLong())) {
                    targetIndex++;
                    switchTimer.reset();
                }
                if (targetIndex >= livingEntities.size())
                    targetIndex = 0;
            }
            target = livingEntities.get(targetIndex);
        }
    }

    public static void attack() {
        if (getTarget() != null) {

            block();


            if (shouldHit(KillAura.getInstance().perfectHitChance.getAsInt())) {
                if (shouldPreSwing(range, preRange))
                    MC.thePlayer.swingItem();


                if (canAttack(range))
                    setRandomDelay();
                    switch (killAura.attackMode.getCurrent()) {

                        case "Normal":
                            MC.thePlayer.swingItem();
                            if (killAura.keepSprint.get())
                                MC.getNetHandler().addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
                            else
                                MC.playerController.attackEntity(MC.thePlayer, target);
                            break;
                        case "Click":
                            MC.clickMouse();
                            if (killAura.keepSprint.get())
                                MC.thePlayer.setSprinting(true);
                            break;

                    }

                if (killAura.randomizeRange.get())
                    range = (float) RandomUtil.getRandomNumber((killAura.range.getAsDouble() > 3.2 ? killAura.range.getAsDouble() - 0.3 : killAura.range.getAsDouble()), killAura.range.getAsDouble() + 1);

                if (killAura.randomizePreRange.get())
                    preRange = (float) RandomUtil.getRandomNumber(killAura.preRange.getAsDouble(), killAura.preRange.getAsDouble() + 1);

                attackTimer.reset();

                unBlock();
            }
        }
    }


    private static boolean shouldHit(int perfectHitChance) {
        boolean perfectHit = random.nextInt(100) < perfectHitChance;
        if (killAura.perfectHit.get() && perfectHit) {
                MovingObjectPosition objectPosition = RayCastUtil.rayCast(2.0F, RotationUtil.getRotations());
                if (objectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && MC.objectMouseOver.entityHit instanceof EntityLivingBase && MC.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
                    return false;
                }

            if (MC.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && MC.objectMouseOver.entityHit instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase)MC.objectMouseOver.entityHit;
                if (entity.hurtTime == 0 || entity.hurtTime == 1) {
                    return true;
                }
            }

            if (target.hurtTime == 4) {
                return false;

            }
        }

        return attackTimer.hasTimePassed(randomDelay) || attackTimer.hasTimePassed(1000L);
    }
    private static void setRandomDelay() {
        PCGRandom pcg = new PCGRandom(System.nanoTime(), System.nanoTime()); // Initialize with some seeds

        if (killAura.minAPS.getAsInt() == 0.0 && killAura.maxAPS.getAsInt() == 0.0) {
            randomDelay = 0L;
        } else if (Math.abs(killAura.minAPS.getAsInt() - killAura.maxAPS.getAsInt()) > 0.0) {
            randomDelay =  pcg.nextInt( killAura.minAPS.getAsInt(),  killAura.maxAPS.getAsInt());
        } else {
            randomDelay =  killAura.minAPS.getAsInt();
        }
    }


    public static void block() {
        if (MC.thePlayer != null && MC.thePlayer.getHeldItem() != null && MC.thePlayer.getHeldItem().getItem() instanceof ItemSword && killAura.autoBlock.get() && !blocking && RandomUtil.get(killAura.blockChance.getAsInt())) {
            blocking = true;
            blockTicks = 0;
            MC.gameSettings.keyBindUseItem.pressed = true;
        } else {
            forceUnblock();
        }
    }

    public static void unBlock() {
        if (MC.thePlayer != null && MC.thePlayer.getHeldItem() != null && MC.thePlayer.getHeldItem().getItem() instanceof ItemSword && killAura.autoBlock.get() && blocking && blockTicks >= Math.round(RandomUtil.getRandomNumber(killAura.minUnBlockTicks.getAsInt(), killAura.maxUnBlockTicks.getAsInt()))) {
            blocking = false;
            MC.gameSettings.keyBindUseItem.pressed = false;
        }
    }

    public static void addBlockTick() {
        if (MC.thePlayer.getDistanceToEntity(target) <= killAura.blockRange.getAsDouble()) blockTicks++;
    }
    public static void forceUnblock() {
        if (blocking && MC.thePlayer.getHeldItem().getItem() instanceof ItemSword && killAura.autoBlock.get()) {
            MC.gameSettings.keyBindUseItem.pressed = false;
            blocking = false;
        }
    }

    public static double nextDouble() {
        SecureRandom secureRandom = new SecureRandom();
        return secureRandom.nextDouble();
    }
    public static double nextInt() {
        SecureRandom secureRandom = new SecureRandom();
        return secureRandom.nextInt();
    }
    public static boolean nextBoolean() {
        SecureRandom secureRandom = new SecureRandom();
        return secureRandom.nextBoolean();
    }
    private static final Random random = new Random();
    public static double nextGaussian(double mean, double stdDev) {
        return mean + (random.nextGaussian() * stdDev);
    }
    public static boolean isValid(EntityLivingBase entityLivingBase) {
        return (killAura.attackDeath.get() || !entityLivingBase.isDead) &&
                ((!entityLivingBase.getName().toLowerCase().contains("shop") && !entityLivingBase.getName().toLowerCase().contains("upgrades")) || killAura.shopAttack.get()) &&
                (entityLivingBase.getName() != MC.thePlayer.getName()) &&
                (MC.thePlayer.getDistanceToEntity(entityLivingBase) <= range + preRange) &&
                (entityLivingBase instanceof EntityPlayer && (!FriendUtil.isFriendString(entityLivingBase.getName()) || !Mega.INSTANCE.getModuleManager().getModule(Friends.class).isToggled())) &&
                (!Mega.INSTANCE.getModuleManager().isToggled(AntiBot.class) || new AntiBot().matrixCanAttack(entityLivingBase)) &&
                (killAura.player.get() && entityLivingBase instanceof EntityPlayer) ||
                (killAura.mobs.get() && (entityLivingBase instanceof EntityMob || entityLivingBase instanceof EntityAnimal) && canAttack(entityLivingBase, KillAura.getInstance().range.getAsDouble() + KillAura.getInstance().preRange.getAsDouble())) ||
                (killAura.villagers.get() && entityLivingBase instanceof EntityVillager);
    }




    public static EntityLivingBase getTarget() {
        return target;
    }

    public static void setTarget(final EntityLivingBase target) {
        AuraUtil.target = target;
    }

    public static void setRange(final float range) {
        AuraUtil.range = range;
    }

    public static float getRange() {
        return range;
    }

    public static void setPreRange(final float preRange) {
        AuraUtil.preRange = preRange;
    }

    public static float getPreRange() {
        return preRange;
    }

    public static void resetCps() {
        finalCps = 0;
    }

    public static boolean canAttack(final float range) {

        return (target != null && MC.thePlayer.getDistanceToEntity(target) <= range);
    }
    public static boolean canAttack(Entity target, final double range) {
        return (target != null && MC.thePlayer.getDistanceToEntity(target) <= range);
    }

    private static boolean shouldPreSwing(final float realRange, final float preRange) {
        return (target != null && MC.thePlayer.getDistanceToEntity(target) > realRange && MC.thePlayer.getDistanceToEntity(target) <= (realRange + preRange) - RandomUtil.getRandomNumber(0.2, 0.6)) && killAura.preSwing.get();
    }

    private static List<EntityLivingBase> getLivingEntities() {
        final List<EntityLivingBase> entities = new ArrayList<>();
        for (final Entity entity : MC.theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase) {
                final EntityLivingBase e = (EntityLivingBase) entity;
                if (isValid((EntityLivingBase) entity) && e != MC.thePlayer){
                    entities.add(e);
                }
            }
        }
        return entities;
    }

    private static final class HealthSorter implements Comparator<EntityLivingBase> {
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return Double.compare(PlayerUtil.getEffectiveHealth(o1), PlayerUtil.getEffectiveHealth(o2));
        }
    }

    private static final class DistanceSorter implements Comparator<EntityLivingBase> {
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return Double.compare(MC.thePlayer.getDistanceToEntity(o1), MC.thePlayer.getDistanceToEntity(o2));
        }
    }


}
