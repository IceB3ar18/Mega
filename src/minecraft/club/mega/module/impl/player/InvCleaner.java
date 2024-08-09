package club.mega.module.impl.player;

import club.mega.Mega;
import club.mega.event.impl.EventPreTick;
import club.mega.event.impl.EventRender2D;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.module.setting.impl.RangeSetting;
import club.mega.util.ChatUtil;
import club.mega.util.RandomUtil;
import club.mega.util.RenderUtil;
import club.mega.util.TimeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import rip.hippo.lwjeb.annotation.Handler;

import java.awt.*;
import java.util.*;

@Module.ModuleInfo(name = "InvCleaner", description = "Cleans your inventory", category = Category.PLAYER)
public class InvCleaner extends Module {
    private final TimeUtil timeHelper = new TimeUtil();
    private final TimeUtil timeHelper2 = new TimeUtil();
    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"OpenInv", "Basic"});
    public final ListSetting cleanMode = new ListSetting("CleanMode", this, new String[]{"Normal", "Clean"});
    private final BooleanSetting noMove = new BooleanSetting("NoMove", this, false);
    public final ListSetting clickMode = new ListSetting("ClickMode", this, new String[]{"Normal"});
    private final RangeSetting delay = new RangeSetting("Delay", this, 1.0, 1000, 80, 120, 1.0, () -> clickMode.is("Normal"));
    public final RangeSetting startDelay = new RangeSetting("StartDelay", this, 0.0, 1000.0, 200.0, 300.0, 1);
    private final BooleanSetting failClicks = new BooleanSetting("FailClicks", this, true);
    public final NumberSetting failChance = new NumberSetting("FailChance", this, 1.0, 10.0, 3, 1, failClicks::get);
    private final BooleanSetting sort = new BooleanSetting("Sort", this, true);
    private boolean iscleaning = false;
    private int currentItem = -1;
    private boolean blockInv;

    private int count = 0;
    public int randomNumber;

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Handler
    public final void earlyTick(final EventTick eventTick) {
        this.setTag(delay.getCurrentMin() + " - " + delay.getCurrentMax() + " ms");
    }


    @Handler
    public final void preTick(final EventPreTick event) {
        ArrayList<ItemStack> swords = new ArrayList();
        ArrayList<ItemStack> bows = new ArrayList();
        ArrayList<ItemStack> rods = new ArrayList();
        ArrayList<ItemStack> foods = new ArrayList();
        ArrayList<ItemStack> gapples = new ArrayList();
        ArrayList<ItemStack> potions = new ArrayList();
        ArrayList<ItemStack> axes = new ArrayList();
        ArrayList<ItemStack> pickAxes = new ArrayList();
        ArrayList<ItemStack> shovels = new ArrayList();
        ArrayList<Integer> allToKeep = new ArrayList();
        int swordID = 0;
        int bowID = 1;
        int rodID = 2;
        int foodID = 7;
        int gappleID = 8;
        ItemStack sword = null;
        ItemStack bow = null;
        ItemStack rod = null;
        ItemStack food = null;
        ItemStack gapple = null;
        ItemStack axe = null;
        ItemStack pickAxe = null;
        ItemStack shovel = null;
        ItemStack newSword = null;
        ItemStack newBow = null;
        ItemStack newRod = null;
        ItemStack newFood = null;
        ItemStack newGapple = null;
        ItemStack newAxe = null;
        ItemStack newPickAxe = null;
        ItemStack newShovel = null;
        long delay = 0;
        if(clickMode.is("Normal")) {
            delay = (long)Math.max((double)RandomUtil.nextLong((long)this.delay.getCurrentMin(), (long)this.delay.getCurrentMax()), 0);
        }

        long startDelay = RandomUtil.nextLong((long) this.startDelay.getCurrentMin(), (long) this.startDelay.getCurrentMax());

        boolean invCleaner = MC.currentScreen instanceof GuiInventory || !this.mode.getCurrent().equalsIgnoreCase("OpenInv") && (MC.currentScreen == null || MC.currentScreen instanceof GuiInventory) && (!this.noMove.get() || this.noMove.get() && !MC.thePlayer.isMoving());

        BlockPos playerBlock = MC.objectMouseOver.getBlockPos();
        if (playerBlock != null) {
            IBlockState blockState = MC.theWorld.getBlockState(playerBlock);
            Block block = blockState.getBlock();
            if (MC.currentScreen == null && Mega.INSTANCE.getModuleManager().getModule(AutoArmor.class).isToggled() && !(block == Blocks.chest) && !(block == Blocks.trapped_chest) && !(block == Blocks.ender_chest) && !(block == Blocks.crafting_table) && !(block == Blocks.furnace) && !(block == Blocks.lit_furnace) && !(block == Blocks.dispenser) && !(block == Blocks.anvil) && !(block == Blocks.enchanting_table)) {
                AutoArmor.getInstance().newAutoArmorHotbar();
            }
        }
        if (invCleaner && !this.blockInv) {
            int i;
            ItemStack itemStack;

            for (i = 0; i < MC.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
                itemStack = MC.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (itemStack != null) {
                    if (itemStack.getItem() instanceof ItemSword) {
                        itemStack.setSlotID(i);
                        swords.add(itemStack);
                        if (i == swordID + 36) {
                            itemStack.setSlotID(i);
                            sword = itemStack;
                        }
                    } else if (itemStack.getItem() instanceof ItemAxe) {
                        itemStack.setSlotID(i);
                        axes.add(itemStack);
                        if (i == swordID + 36) {
                            itemStack.setSlotID(i);
                            axe = itemStack;
                        }
                    } else if (itemStack.getItem() instanceof ItemPickaxe) {
                        itemStack.setSlotID(i);
                        pickAxes.add(itemStack);
                    } else if (itemStack.getItem() instanceof ItemSpade) {
                        itemStack.setSlotID(i);
                        shovels.add(itemStack);
                    } else if (itemStack.getItem() instanceof ItemBlock) {
                        if (this.itemsToDrop(itemStack)) {
                            itemStack.setSlotID(i);
                            allToKeep.add(i);
                        }
                    } else if (itemStack.getItem() instanceof ItemBow) {
                        itemStack.setSlotID(i);
                        bows.add(itemStack);
                        if (i == bowID + 36) {
                            itemStack.setSlotID(i);
                            bow = itemStack;
                        }
                    } else if (itemStack.getItem() instanceof ItemFishingRod) {
                        itemStack.setSlotID(i);
                        rods.add(itemStack);
                        if (i == rodID + 36) {
                            itemStack.setSlotID(i);
                            rod = itemStack;
                        }
                    } else if (itemStack.getItem() instanceof ItemFood) {
                        if (itemStack.getItem() == Item.getByNameOrId("golden_apple")) {
                            itemStack.setSlotID(i);
                            gapples.add(itemStack);
                            allToKeep.add(i);
                            if (i == gappleID + 36) {
                                itemStack.setSlotID(i);
                                gapple = itemStack;
                            }

                        } else if (this.itemsToDrop(itemStack)) {
                            itemStack.setSlotID(i);
                            foods.add(itemStack);

                            if (i == foodID + 36) {
                                itemStack.setSlotID(i);
                                food = itemStack;
                            }
                        }
                    } else if (itemStack.getItem() instanceof ItemPotion) {
                        ItemPotion itemPotion = (ItemPotion) itemStack.getItem();
                        if (!itemPotion.getEffects(itemStack).isEmpty()) {
                            PotionEffect potionEffect = (PotionEffect) itemPotion.getEffects(itemStack).get(0);
                            Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
                            if (!potion.isBadEffect()) {
                                itemStack.setSlotID(i);
                                potions.add(itemStack);
                                allToKeep.add(i);
                            }
                        }
                    } else if (itemStack.getItem() instanceof ItemTool) {
                        if (this.itemsToDrop(itemStack)) {
                            itemStack.setSlotID(i);
                            allToKeep.add(i);
                        }
                    } else if (this.itemsToKeep(itemStack)) {
                        itemStack.setSlotID(i);
                        allToKeep.add(i);
                    } else if (!(itemStack.getItem() instanceof ItemArmor) && this.itemsToDrop(itemStack)) {
                        itemStack.setSlotID(i);
                        allToKeep.add(i);
                    }
                }
            }
            ItemStack bestFood = null;
            for (ItemStack currentFood : foods) {
                if (currentFood.stackSize >= 8 && (bestFood == null || compareFood(currentFood, bestFood) > 0)) {
                    allToKeep.add(i);
                }
            }

            Iterator var44 = swords.iterator();

            while (var44.hasNext()) {
                itemStack = (ItemStack) var44.next();
                if (itemStack.getSlotID() != swordID + 36) {
                    if (sword == null) {
                        if (newSword != null) {
                            if (this.getDamageSword(itemStack) > this.getDamageSword(newSword)) {
                                newSword = itemStack;
                            } else if (this.getDamageSword(itemStack) == this.getDamageSword(newSword) && itemStack.getItemDamage() < newSword.getItemDamage()) {
                                newSword = itemStack;
                            }
                        } else {
                            newSword = itemStack;
                        }
                    } else if (sword.getItem() instanceof ItemSword) {
                        if (newSword != null) {
                            if (this.getDamageSword(itemStack) > this.getDamageSword(newSword)) {
                                newSword = itemStack;
                            } else if (this.getDamageSword(itemStack) == this.getDamageSword(newSword) && itemStack.getItemDamage() < newSword.getItemDamage()) {
                                newSword = itemStack;
                            }
                        } else if (this.getDamageSword(itemStack) > this.getDamageSword(sword)) {
                            newSword = itemStack;
                        } else if (this.getDamageSword(itemStack) == this.getDamageSword(sword) && itemStack.getItemDamage() < sword.getItemDamage()) {
                            newSword = itemStack;
                        }
                    }
                }
            }

            var44 = axes.iterator();

            while (var44.hasNext()) {
                itemStack = (ItemStack) var44.next();
                if (axe == null) {
                    if (newAxe != null) {
                        if (this.getToolDamage(itemStack) > this.getToolDamage(newAxe)) {
                            newAxe = itemStack;
                        } else if (this.getToolDamage(itemStack) == this.getToolDamage(newAxe) && itemStack.getItemDamage() < newAxe.getItemDamage()) {
                            newAxe = itemStack;
                        }
                    } else {
                        newAxe = itemStack;
                    }
                } else if (axe.getItem() instanceof ItemAxe) {
                    if (newAxe != null) {
                        if (this.getToolDamage(itemStack) > this.getToolDamage(newAxe)) {
                            newAxe = itemStack;
                        } else if (this.getToolDamage(itemStack) == this.getToolDamage(newAxe) && itemStack.getItemDamage() < newAxe.getItemDamage()) {
                            newAxe = itemStack;
                        }
                    } else if (this.getToolDamage(itemStack) > this.getToolDamage(axe)) {
                        newAxe = itemStack;
                    } else if (this.getToolDamage(itemStack) == this.getToolDamage(axe) && itemStack.getItemDamage() < axe.getItemDamage()) {
                        newAxe = itemStack;
                    }
                }
            }

            var44 = pickAxes.iterator();

            while (var44.hasNext()) {
                itemStack = (ItemStack) var44.next();
                if (pickAxe == null) {
                    if (newPickAxe != null) {
                        if (this.getToolSpeed(itemStack) > this.getToolSpeed(newPickAxe)) {
                            newPickAxe = itemStack;
                        } else if (this.getToolSpeed(itemStack) == this.getToolSpeed(newPickAxe) && itemStack.getItemDamage() < newPickAxe.getItemDamage()) {
                            newPickAxe = itemStack;
                        }
                    } else {
                        newPickAxe = itemStack;
                    }
                } else if (((ItemStack) pickAxe).getItem() instanceof ItemPickaxe) {
                    if (newPickAxe != null) {
                        if (this.getToolSpeed(itemStack) > this.getToolSpeed(newPickAxe)) {
                            newPickAxe = itemStack;
                        } else if (this.getToolSpeed(itemStack) == this.getToolSpeed(newPickAxe) && itemStack.getItemDamage() < newPickAxe.getItemDamage()) {
                            newPickAxe = itemStack;
                        }
                    } else if (this.getToolSpeed(itemStack) > this.getToolSpeed((ItemStack) pickAxe)) {
                        newPickAxe = itemStack;
                    } else if (this.getToolSpeed(itemStack) == this.getToolSpeed((ItemStack) pickAxe) && itemStack.getItemDamage() < ((ItemStack) pickAxe).getItemDamage()) {
                        newPickAxe = itemStack;
                    }
                }
            }

            var44 = shovels.iterator();

            while (var44.hasNext()) {
                itemStack = (ItemStack) var44.next();
                if (shovel == null) {
                    if (newShovel != null) {
                        if (this.getToolSpeed(itemStack) > this.getToolSpeed(newShovel)) {
                            newShovel = itemStack;
                        } else if (this.getToolSpeed(itemStack) == this.getToolSpeed(newShovel) && itemStack.getItemDamage() < newShovel.getItemDamage()) {
                            newShovel = itemStack;
                        }
                    } else {
                        newShovel = itemStack;
                    }
                } else if (((ItemStack) shovel).getItem() instanceof ItemSpade) {
                    if (newShovel != null) {
                        if (this.getToolSpeed(itemStack) > this.getToolSpeed(newShovel)) {
                            newShovel = itemStack;
                        } else if (this.getToolSpeed(itemStack) == this.getToolSpeed(newShovel) && itemStack.getItemDamage() < newShovel.getItemDamage()) {
                            newShovel = itemStack;
                        }
                    } else if (this.getToolSpeed(itemStack) > this.getToolSpeed((ItemStack) shovel)) {
                        newShovel = itemStack;
                    } else if (this.getToolSpeed(itemStack) == this.getToolSpeed((ItemStack) shovel) && itemStack.getItemDamage() < ((ItemStack) shovel).getItemDamage()) {
                        newShovel = itemStack;
                    }
                }
            }

            var44 = bows.iterator();

            while (var44.hasNext()) {
                itemStack = (ItemStack) var44.next();
                if (itemStack.getSlotID() != bowID + 36) {
                }

                if (bow == null) {
                    if (newBow != null) {
                        if (itemStack.getItemDamage() < newBow.getItemDamage()) {
                            newBow = itemStack;
                        }
                    } else {
                        newBow = itemStack;
                    }
                } else if (bow.getItem() instanceof ItemBow) {
                    if (newBow != null) {
                        if (itemStack.getItemDamage() < newBow.getItemDamage()) {
                            newBow = itemStack;
                        }
                    } else if (itemStack.getItemDamage() < bow.getItemDamage()) {
                        newBow = itemStack;
                    }
                }
            }

            var44 = rods.iterator();

            while (var44.hasNext()) {
                itemStack = (ItemStack) var44.next();
                if (itemStack.getSlotID() != rodID + 36) {
                    if (rod == null) {
                        if (newRod != null) {
                            if (itemStack.getItemDamage() < newRod.getItemDamage()) {
                                newRod = itemStack;
                            }
                        } else {
                            newRod = itemStack;
                        }
                    } else if (rod.getItem() instanceof ItemFishingRod) {
                        if (newRod != null) {
                            if (itemStack.getItemDamage() < newRod.getItemDamage()) {
                                newRod = itemStack;
                            }
                        } else if (itemStack.getItemDamage() < rod.getItemDamage()) {
                            newRod = itemStack;
                        }
                    }
                }
            }

            var44 = foods.iterator();

            while (var44.hasNext()) {
                itemStack = (ItemStack) var44.next();
                if (itemStack.getSlotID() != foodID + 36) {
                    if (food == null) {
                        if (newFood != null) {
                            if (itemStack.stackSize > newFood.stackSize) {
                                newFood = itemStack;
                            }
                        } else {
                            newFood = itemStack;
                        }
                    } else if (food.getItem() instanceof ItemFood) {
                        if (newFood != null) {
                            if (itemStack.stackSize > newFood.stackSize) {
                                newFood = itemStack;
                            }
                        } else if (itemStack.stackSize > food.stackSize) {
                            newFood = itemStack;
                        }
                    }
                }
            }

            var44 = gapples.iterator();

            while (var44.hasNext()) {
                itemStack = (ItemStack) var44.next();
                if (itemStack.getSlotID() != gappleID + 36) {
                    if (gapple == null) {
                        if (newGapple != null) {
                            if (itemStack.stackSize > newGapple.stackSize) {
                                newGapple = itemStack;
                            }
                        } else {
                            newGapple = itemStack;
                        }
                    } else if (gapple.getItem() instanceof ItemFood) {
                        if (newGapple != null) {
                            if (itemStack.stackSize > newGapple.stackSize) {
                                newGapple = itemStack;
                            }
                        } else if (itemStack.stackSize > gapple.stackSize) {
                            newGapple = itemStack;
                        }
                    }
                }
            }

            if (newSword != null) {
                allToKeep.add(newSword.getSlotID());
            }

            if (sword != null) {
                allToKeep.add(sword.getSlotID());
            }

            if (newBow != null) {
                allToKeep.add(newBow.getSlotID());
            }

            if (bow != null) {
                allToKeep.add(bow.getSlotID());
            }

            if (newRod != null) {
                allToKeep.add(newRod.getSlotID());
            }

            if (rod != null) {
                allToKeep.add(rod.getSlotID());
            }

            if (newFood != null) {
                allToKeep.add(newFood.getSlotID());
            }

            if (food != null) {
                allToKeep.add(food.getSlotID());
            }

            if (newGapple != null) {
                allToKeep.add(newGapple.getSlotID());
            }

            if (gapple != null) {
                allToKeep.add(gapple.getSlotID());
            }

            if (axe != null) {
                allToKeep.add(axe.getSlotID());
            }

            if (newAxe != null) {
                allToKeep.add(newAxe.getSlotID());
            }

            if (pickAxe != null) {
                allToKeep.add(((ItemStack) pickAxe).getSlotID());
            }

            if (newPickAxe != null) {
                allToKeep.add(newPickAxe.getSlotID());
            }

            if (shovel != null) {
                allToKeep.add(((ItemStack) shovel).getSlotID());
            }

            if (newShovel != null) {
                allToKeep.add(newShovel.getSlotID());
            }

            if (this.timeHelper2.hasTimePassed(startDelay)) {
                if (this.timeHelper.hasTimePassed(delay) && this.sort.get()) {
                    if (newSword != null) {
                        this.switchItems(newSword, swordID);
                        this.timeHelper.reset();
                        if (this.delay.getCurrentMin() != 0.0) {
                            return;
                        }
                    }

                    if (sword == null && axe == null && newAxe != null) {
                        this.switchItems(newAxe, swordID);
                        this.timeHelper.reset();
                        if (this.delay.getCurrentMin() != 0.0) {
                            return;
                        }
                    }

                    if (newBow != null) {
                        this.switchItems(newBow, bowID);
                        this.timeHelper.reset();
                        if (this.delay.getCurrentMin() != 0.0) {
                            return;
                        }
                    }

                    if (newRod != null) {
                        this.switchItems(newRod, rodID);
                        this.timeHelper.reset();
                        if (this.delay.getCurrentMin() != 0.0) {
                            return;
                        }
                    }

                    if (newFood != null) {
                        this.switchItems(newFood, foodID);
                        this.timeHelper.reset();
                        if (this.delay.getCurrentMin() != 0.0) {
                            return;
                        }
                    }

                    if (newGapple != null) {
                        this.switchItems(newGapple, gappleID);
                        this.timeHelper.reset();
                        if (this.delay.getCurrentMin() != 0.0) {
                            return;
                        }
                    }
                }

                if (Mega.INSTANCE.getModuleManager().getModule(AutoArmor.class).isToggled()) {

                    AutoArmor.getInstance().newAutoArmor(false);
                }


                if (this.timeHelper.hasTimePassed(delay) && AutoArmor.getInstance().isInvManager()) {
                    for (i = 9; i < MC.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
                        ArrayList<Integer> emptys = new ArrayList();
                        for (int j = 9; j < MC.thePlayer.inventoryContainer.inventorySlots.size(); ++j) {
                            itemStack = MC.thePlayer.inventoryContainer.getSlot(j).getStack();
                            if (itemStack == null) {
                                emptys.add(j);
                            }
                        }
                        itemStack = MC.thePlayer.inventoryContainer.getSlot(i).getStack();
                        if (itemStack != null) {
                            boolean notDrop = false;


                            for (int j = 0; j < allToKeep.size(); ++j) {
                                int slot = allToKeep.get(j);
                                if (i == slot) {
                                    notDrop = true;
                                    break;
                                }
                            }


                            if (!notDrop) {
                                if (shouldFail()) {
                                    int index = (int) RandomUtil.getRandomNumber(0, emptys.size());
                                    int failSlot = emptys.get(index);
                                    currentItem = failSlot;
                                    iscleaning = true;
                                    this.dropItems(failSlot);
                                    emptys.remove(index);
                                    break;
                                }
                                currentItem = i;
                                iscleaning = true;
                                this.dropItems(i);
                                this.timeHelper.reset();
                                if (this.delay.getCurrentMin() != 0.0) {
                                    return;
                                }
                            }
                        }
                    }
                }

            }
        } else {
            this.timeHelper2.reset();
        }

    }


    private boolean allItemsEqual(String[] chars) {
        String firstElement = chars[0];
        for(int i = 1; i < chars.length; i++) { //0 skip
            if(chars[i].equals(firstElement)) {
                return false;
            }
        }
        return true;
    }

    private int compareFood(ItemStack food1, ItemStack food2) {
        ItemFood itemFood1 = (ItemFood) food1.getItem();
        ItemFood itemFood2 = (ItemFood) food2.getItem();
        if (itemFood1.getHealAmount(food1) != itemFood2.getHealAmount(food2)) {
            return itemFood1.getHealAmount(food1) - itemFood2.getHealAmount(food2);
        }
        return (int) (itemFood1.getSaturationModifier(food1) - itemFood2.getSaturationModifier(food2));
    }
    private boolean shouldFail() {
        int random = (int) RandomUtil.getRandomNumber(0, failChance.getAsInt() + 1);
        return random == 1;
    }

    @Handler
    public final void render2D(final EventRender2D event) {
        if (MC.currentScreen instanceof GuiInventory) {
            GuiInventory inv = (GuiInventory) MC.currentScreen;
            if (iscleaning) {
                int i = inv.guiLeft;
                int j = inv.guiTop;
                Slot slot = inv.inventorySlots.inventorySlots.get(currentItem);
                RenderUtil.drawRect(slot.xDisplayPosition + i, slot.yDisplayPosition + j, 17, 17, new Color(255, 255, 255));

            }

        }
    }

    private void switchItems(ItemStack itemStack, int hotBarSlot) {
        count++;
        MC.playerController.windowClick(MC.thePlayer.inventoryContainer.windowId, itemStack.getSlotID(), hotBarSlot, 2, MC.thePlayer);
    }

    private void dropItems(int slotID) {
        count++;
        MC.playerController.windowClick(MC.thePlayer.inventoryContainer.windowId, slotID, 1, 4, MC.thePlayer);

    }

    private double getDamageSword(ItemStack itemStack) {
        double damage = 0.0;
        if (itemStack.getItem() instanceof ItemSword) {
            damage += (double) (((ItemSword) itemStack.getItem()).getAttackDamage() + (float) EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) * 1.25F);
            damage += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStack) / 11.0;
            damage += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, itemStack) / 11.0;
            damage += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack) / 11.0;
        }

        return damage;
    }

    private double getToolDamage(ItemStack itemStack) {
        double damage = 0.0;
        if (itemStack.getItem() instanceof ItemAxe) {
            damage += (double) itemStack.getItem().getStrVsBlock(itemStack, new Block(Material.wood, MapColor.woodColor)) + (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack) / 11.0;
        } else if (itemStack.getItem() instanceof ItemPickaxe) {
            damage += (double) itemStack.getItem().getStrVsBlock(itemStack, new Block(Material.rock, MapColor.stoneColor)) + (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack) / 11.0;
        } else if (itemStack.getItem() instanceof ItemSpade) {
            damage += (double) itemStack.getItem().getStrVsBlock(itemStack, new Block(Material.sand, MapColor.sandColor)) + (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack) / 11.0;
        }

        damage += (double) ((float) itemStack.getItem().getMaxDamage() + (float) EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) * 1.25F);
        damage += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStack) / 11.0;
        damage += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, itemStack) / 11.0;
        damage += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack) / 11.0;
        return damage;
    }

    private boolean itemsToKeep(ItemStack itemStack) {
        Item i = itemStack.getItem();
        return i.equals(Item.getItemById(288)) || i.equals(Item.getItemById(289));
    }

    private boolean itemsToDrop(ItemStack itemStack) {
        Item i = itemStack.getItem();

        // Items to exclude
        Set<Integer> excludeIds = new HashSet<>(Arrays.asList(
                39, 40, 288, 289, 53, 65, 66, 67, 70, 72, 77, 81, 85, 96, 101, 102, 106, 107, 108, 109, 111, 113, 114,
                128, 131, 134, 135, 136, 143, 136, 147, 148, 151, 154, 156, 157, 163, 164, 167, 180, 287, 318, 321, 334,
                337, 338, 344, 348, 352, 353, 354, 356, 361, 362, 367, 370, 371, 372, 373, 12, 88, 332
        ));
        if (cleanMode.is("Clean")) {
            excludeIds.addAll(Arrays.asList(259, 327, 326, 351, 46, 384));
        }
        // Items to exclude by name
        Set<String> excludeNames = new HashSet<>(Arrays.asList(
                "tallgrass", "deadbush", "red_flower", "stone_slab", "snow_layer", "wooden_slab", "cobblestone_wall",
                "anvil", "stained_glass_pane", "carpet", "double_plant", "stone_slab2", "sapling"
        ));
        // Check if the item is in the exclusion sets
        if (excludeIds.contains(Item.getIdFromItem(i)) || excludeNames.contains(i.getUnlocalizedName())) {
            return false;
        }

        // Check specific ID ranges
        if ((183 <= Item.getIdFromItem(i) && Item.getIdFromItem(i) < 192) ||
                (290 <= Item.getIdFromItem(i) && Item.getIdFromItem(i) < 295)) {
            return false;
        }

        return true;
    }

    private double getToolSpeed(ItemStack itemStack) {
        double damage = 0.0;
        if (itemStack.getItem() instanceof ItemTool) {
            if (itemStack.getItem() instanceof ItemAxe) {
                damage += (double) (itemStack.getItem().getStrVsBlock(itemStack, new Block(Material.wood, MapColor.woodColor)) + (float) EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack));
            } else if (itemStack.getItem() instanceof ItemPickaxe) {
                damage += (double) (itemStack.getItem().getStrVsBlock(itemStack, new Block(Material.rock, MapColor.stoneColor)) + (float) EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack));
            } else if (itemStack.getItem() instanceof ItemSpade) {
                damage += (double) (itemStack.getItem().getStrVsBlock(itemStack, new Block(Material.sand, MapColor.sandColor)) + (float) EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack));
            }

            damage += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack) / 11.0;
            damage += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, itemStack) / 11.0;
            damage += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) / 33.0;
        }

        return damage;
    }

    public static InvCleaner getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(InvCleaner.class);
    }

}
