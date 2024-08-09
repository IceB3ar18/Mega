package club.mega.util;

import club.mega.interfaces.MinecraftInterface;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;
import net.optifine.reflect.Reflector;

import java.util.List;

public final class RayCastUtil implements MinecraftInterface {

    public static MovingObjectPosition getHitVec(BlockPos blockPos, float yaw, float pitch, double range) {
        Vec3 vec31 = MC.thePlayer.getVectorForRotation(pitch, yaw);
        Vec3 vec32 = MC.thePlayer.getPositionEyes(1.0F).addVector(vec31.xCoord * range, vec31.yCoord * range, vec31.zCoord * range);
        Block block = MC.theWorld.getBlockState(blockPos).getBlock();
        AxisAlignedBB axisalignedbb = new AxisAlignedBB((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), (double)blockPos.getX() + block.getBlockBoundsMaxX(), (double)blockPos.getY() + block.getBlockBoundsMaxY(), (double)blockPos.getZ() + block.getBlockBoundsMaxZ());
        return axisalignedbb.calculateIntercept(MC.thePlayer.getPositionEyes(1.0F), vec32);
    }


    public static MovingObjectPosition rayCast(float partialTicks, float[] rots) {
        MovingObjectPosition objectMouseOver = null;
        Entity entity = MC.getRenderViewEntity();
        if (entity != null && MC.theWorld != null) {
            MC.mcProfiler.startSection("pick");
            MC.pointedEntity = null;
            double d0 = (double)MC.playerController.getBlockReachDistance();
            objectMouseOver = entity.customRayTrace(d0, partialTicks, rots[0], rots[1]);
            double d1 = d0;
            Vec3 vec3 = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            boolean flag1 = true;
            if (MC.playerController.extendedReach()) {
                d0 = 6.0;
                d1 = 6.0;
            } else {
                if (d0 > 3.0) {
                    flag = true;
                }

                d0 = d0;
            }

            if (objectMouseOver != null) {
                d1 = objectMouseOver.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = entity.getCustomLook(partialTicks, rots[0], rots[1]);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            Entity pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1.0F;
            List list = MC.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand((double)f, (double)f, (double)f), Predicates.and(new Predicate[]{EntitySelectors.NOT_SPECTATING}));
            double d2 = d1;
            AxisAlignedBB realBB = null;

            for(int i = 0; i < list.size(); ++i) {
                Entity entity1 = (Entity)list.get(i);
                float f1 = entity1.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)f1, (double)f1, (double)f1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0;
                    }
                } else if (movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                    if (d3 < d2 || d2 == 0.0) {
                        boolean flag2 = false;
                        if (Reflector.ForgeEntity_canRiderInteract.exists()) {
                            flag2 = Reflector.callBoolean(entity1, Reflector.ForgeEntity_canRiderInteract, new Object[0]);
                        }

                        if (entity1 == entity.ridingEntity && !flag2) {
                            if (d2 == 0.0) {
                                pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                            }
                        } else {
                            pointedEntity = entity1;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > 3.0) {
                pointedEntity = null;
                objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, (EnumFacing)null, new BlockPos(vec33));
            }

            if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
                objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);
                if (!(pointedEntity instanceof EntityLivingBase) && pointedEntity instanceof EntityItemFrame) {
                }
            }
        }

        return objectMouseOver;
    }


    public static final Entity raycastEntity(double range, float[] rotations) {
        final Entity player = MC.getRenderViewEntity();

        if (player != null && MC.theWorld != null) {
            final Vec3 eyeHeight = player.getPositionEyes(MC.timer.renderPartialTicks);

            final Vec3 looks = getVectorForRotation(rotations[0], rotations[1]);
            final Vec3 vec = eyeHeight.addVector(looks.xCoord * range, looks.yCoord * range, looks.zCoord * range);
            final List<Entity> list = MC.theWorld.getEntitiesInAABBexcluding(player, player.getEntityBoundingBox().addCoord(looks.xCoord * range, looks.yCoord * range, looks.zCoord * range).expand(1, 1, 1), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));

            Entity raycastedEntity = null;

            for (Entity entity : list) {
                if (!(entity instanceof EntityLivingBase)) continue;

                final float borderSize = entity.getCollisionBorderSize();
                final AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);
                final MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(eyeHeight, vec);

                if (axisalignedbb.isVecInside(eyeHeight)) {
                    if (range >= 0.0D) {
                        raycastedEntity = entity;
                        range = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    double distance = eyeHeight.distanceTo(movingobjectposition.hitVec);

                    if (distance < range || range == 0.0D) {

                        if (entity == player.ridingEntity) {
                            if (range == 0.0D) {
                                raycastedEntity = entity;
                            }
                        } else {
                            raycastedEntity = entity;
                            range = distance;
                        }
                    }
                }
            }
            return raycastedEntity;
        }
        return null;
    }

    public static final Vec3 getVectorForRotation(float yaw, float pitch) {
        final double f = Math.cos(Math.toRadians(-yaw) - Math.PI);
        final double f1 = Math.sin(Math.toRadians(-yaw) - Math.PI);
        final double f2 = -Math.cos(Math.toRadians(-pitch));
        final double f3 = Math.sin(Math.toRadians(-pitch));
        return new Vec3((double) (f1 * f2), (double) f3, (double) (f * f2));
    }



    public static MovingObjectPosition rayCastAura(float partialTicks, float[] rots) {
        MovingObjectPosition objectMouseOver = null;
        Entity entity = MC.getRenderViewEntity();
        if (entity != null && MC.theWorld != null) {
            MC.mcProfiler.startSection("pick");
            MC.pointedEntity = null;
            double d0 = (double)MC.playerController.getBlockReachDistance();
            objectMouseOver = entity.customRayTrace(d0, partialTicks, rots[0], rots[1]);
            double d2 = d0;
            Vec3 vec3 = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            boolean flag2 = true;
            if (MC.playerController.extendedReach()) {
                d0 = 6.0;
                d2 = 6.0;
            } else {
                if (d0 > 3.0) {
                    flag = true;
                }

                d0 = d0;
            }

            if (objectMouseOver != null) {
                d2 = objectMouseOver.hitVec.distanceTo(vec3);
            }

            Vec3 vec4 = entity.getCustomLook(partialTicks, rots[0], rots[1]);
            Vec3 vec5 = vec3.addVector(vec4.xCoord * d0, vec4.yCoord * d0, vec4.zCoord * d0);
            Entity pointedEntity = null;
            Vec3 vec6 = null;
            float f = 1.0F;
            List list = MC.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec4.xCoord * d0, vec4.yCoord * d0, vec4.zCoord * d0).expand(1.0, 1.0, 1.0), Predicates.and(new Predicate[]{EntitySelectors.NOT_SPECTATING}));
            double d3 = d2;
            AxisAlignedBB realBB = null;

            for(int i = 0; i < list.size(); ++i) {
                Entity entity2 = (Entity)list.get(i);
                float f2 = entity2.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity2.getEntityBoundingBox().expand((double)f2, (double)f2, (double)f2);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec5);
                if (axisalignedbb.isVecInside(vec3)) {
                    if (d3 >= 0.0) {
                        pointedEntity = entity2;
                        vec6 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d3 = 0.0;
                    }
                } else if (movingobjectposition != null) {
                    double d4 = vec3.distanceTo(movingobjectposition.hitVec);
                    if (d4 < d3 || d3 == 0.0) {
                        boolean flag3 = false;
                        if (Reflector.ForgeEntity_canRiderInteract.exists()) {
                            flag3 = Reflector.callBoolean(entity2, Reflector.ForgeEntity_canRiderInteract, new Object[0]);
                        }

                        if (entity2 == entity.ridingEntity && !flag3) {
                            if (d3 == 0.0) {
                                pointedEntity = entity2;
                                vec6 = movingobjectposition.hitVec;
                            }
                        } else {
                            pointedEntity = entity2;
                            vec6 = movingobjectposition.hitVec;
                            d3 = d4;
                        }
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec6) > 3.0) {
                pointedEntity = null;
                objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec6, (EnumFacing)null, new BlockPos(vec6));
            }

            if (pointedEntity != null && (d3 < d2 || objectMouseOver == null)) {
                objectMouseOver = new MovingObjectPosition(pointedEntity, vec6);
                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    ;
                }
            }
        }

        return objectMouseOver;
    }

    public static MovingObjectPosition rayCast(float partialTicks, float[] rots, double range, double hitBoxExpand) {
        MovingObjectPosition objectMouseOver = null;
        Entity entity = MC.getRenderViewEntity();
        if (entity != null && MC.theWorld != null) {
            MC.mcProfiler.startSection("pick");
            MC.pointedEntity = null;
            objectMouseOver = entity.customRayTrace(range, partialTicks, rots[0], rots[1]);
            double d2 = range;
            Vec3 vec3 = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            boolean flag2 = true;
            double d0;
            if (MC.playerController.extendedReach()) {
                d0 = 6.0;
                d2 = 6.0;
            } else {
                if (range > 3.0) {
                    flag = true;
                }

                d0 = range;
            }

            if (objectMouseOver != null) {
                d2 = objectMouseOver.hitVec.distanceTo(vec3);
            }

            Vec3 vec4 = entity.getCustomLook(partialTicks, rots[0], rots[1]);
            Vec3 vec5 = vec3.addVector(vec4.xCoord * d0, vec4.yCoord * d0, vec4.zCoord * d0);
            Entity pointedEntity = null;
            Vec3 vec6 = null;
            float f = 1.0F;
            List list = MC.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec4.xCoord * d0, vec4.yCoord * d0, vec4.zCoord * d0).expand(1.0, 1.0, 1.0), Predicates.and(new Predicate[]{EntitySelectors.NOT_SPECTATING}));
            double d3 = d2;
            AxisAlignedBB realBB = null;

            for(int i = 0; i < list.size(); ++i) {
                Entity entity2 = (Entity)list.get(i);
                float f2 = (float)((double)entity2.getCollisionBorderSize() + hitBoxExpand);
                AxisAlignedBB axisalignedbb = entity2.getEntityBoundingBox().expand((double)f2, (double)f2, (double)f2);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec5);
                if (axisalignedbb.isVecInside(vec3)) {
                    if (d3 >= 0.0) {
                        pointedEntity = entity2;
                        vec6 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d3 = 0.0;
                    }
                } else if (movingobjectposition != null) {
                    double d4 = vec3.distanceTo(movingobjectposition.hitVec);
                    if (d4 < d3 || d3 == 0.0) {
                        boolean flag3 = false;
                        if (Reflector.ForgeEntity_canRiderInteract.exists()) {
                            flag3 = Reflector.callBoolean(entity2, Reflector.ForgeEntity_canRiderInteract, new Object[0]);
                        }

                        if (entity2 == entity.ridingEntity && !flag3) {
                            if (d3 == 0.0) {
                                pointedEntity = entity2;
                                vec6 = movingobjectposition.hitVec;
                            }
                        } else {
                            pointedEntity = entity2;
                            vec6 = movingobjectposition.hitVec;
                            d3 = d4;
                        }
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec6) > range) {
                pointedEntity = null;
                objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec6, (EnumFacing)null, new BlockPos(vec6));
            }

            if (pointedEntity != null && (d3 < d2 || objectMouseOver == null)) {
                objectMouseOver = new MovingObjectPosition(pointedEntity, vec6);
                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    ;
                }
            }
        }

        return objectMouseOver;
    }
}
