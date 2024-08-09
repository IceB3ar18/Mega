package club.mega.module.impl.player;

import club.mega.event.impl.EventRender2D;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.module.setting.impl.RangeSetting;
import club.mega.util.RandomUtil;
import club.mega.util.TimeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import rip.hippo.lwjeb.annotation.Handler;
import org.lwjgl.opengl.GL11;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Module.ModuleInfo(name = "ChestStealer", description = "Steals items from a chest", category = Category.PLAYER)
public class ChestStealer extends Module {
    public ListSetting sorting = new ListSetting("Sorting", this, new String[]{"Normal", "Random", "Nearest"});
    public ListSetting mode = new ListSetting("Mode", this, new String[]{"Normal", "MouseSimulation"});
    private final RangeSetting delay = new RangeSetting("Delay", this, 0, 1000.0, 90, 150, 1.0, ()-> mode.is("Normal"));

    private final RangeSetting mouseSpeed = new RangeSetting("Mouse Speed", this, 1.0, 50, 5, 10, 1.0, ()-> mode.is("MouseSimulation"));
    public NumberSetting clickEmptySlotChance = new NumberSetting("Click Empty Slot Chance", this, 0, 100, 10, 1);
    public BooleanSetting freeLook = new BooleanSetting("FreeLook", this, true);
    public BooleanSetting intelligent = new BooleanSetting("Intelligent", this, true);
    public RangeSetting startDelay = new RangeSetting("Start Delay", this, 0, 1000, 100, 300, 1);
    public BooleanSetting close = new BooleanSetting("Close", this, true);
    public RangeSetting closeDelay = new RangeSetting("Close Delay", this, 0, 1000, 100, 300, 1, close::get);

    private final TimeUtil startTimer = new TimeUtil();
    private final TimeUtil closeTimer = new TimeUtil();
    private final TimeUtil timerUtil = new TimeUtil();

    private final RandomUtil randomUtil = new RandomUtil();
    private List<Point> itemPositions = new ArrayList<>();
    private int currentIndex = 0;

    private double currentMouseSpeed;
    private boolean hasStarted = false;
    private boolean waitingToClose = false;

    @Handler
    public void onUpdate(EventTick e) {
        if (MC.currentScreen instanceof GuiChest) {
            if (freeLook.get()) {
                MC.inGameHasFocus = true;
                MC.mouseHelper.grabMouseCursor();
            }
            if (!hasStarted) {
                if (startTimer.hasTimePassed(getSafeRandom((long) startDelay.getCurrentMin(), (long) startDelay.getCurrentMax()))) {
                    hasStarted = true;
                    startTimer.reset();
                } else {
                    return;
                }
            }

            final ContainerChest chest = (ContainerChest) MC.thePlayer.openContainer;

            // Collect item positions if not already done
            if (itemPositions.isEmpty()) {
                boolean hasItems = false;
                for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); ++i) {
                    final ItemStack itemStack = chest.getLowerChestInventory().getStackInSlot(i);

                    if (itemStack != null) {
                        if (this.intelligent.get()) {
                            if (this.isBestChestItem(itemStack) && this.isBestItem(itemStack)) {
                                itemPositions.add(new Point(i % 9, i / 9));
                            }
                        } else {
                            itemPositions.add(new Point(i % 9, i / 9));
                        }

                        hasItems = true;
                    } else if (randomUtil.nextInt(0, 100) < clickEmptySlotChance.getAsInt()) {
                        // Add empty slot based on probability
                        itemPositions.add(new Point(i % 9, i / 9));
                    }
                }
                // Only sort positions if there are actual items in the chest
                if (hasItems) {
                    sortPositions();
                    // Set initial mouse speed
                    currentMouseSpeed = (randomUtil.nextDouble(mouseSpeed.getCurrentMin(), mouseSpeed.getCurrentMax()));
                } else {
                    itemPositions.clear();
                }
            }

            // Check if there are no best items left when intelligent mode is enabled
            if (this.intelligent.get() && !containsBestItems(chest)) {
                hasStarted = false;
                if (close.get() && !waitingToClose) {
                    closeTimer.reset();
                    waitingToClose = true;
                }
            } else if (!itemPositions.isEmpty() && timerUtil.hasTimePassed(getDelayToNextPosition())) {
                Point pos = itemPositions.get(currentIndex);
                int slotIndex = pos.y * 9 + pos.x;

                MC.playerController.windowClick(chest.windowId, slotIndex, 0, 1, MC.thePlayer);

                currentIndex++;
                // Update mouse speed smoothly
                currentMouseSpeed = randomUtil.smooth(mouseSpeed.getCurrentMax(), mouseSpeed.getCurrentMin(), 0.1, true, 0.05);
                timerUtil.reset();
            }

            if (currentIndex >= itemPositions.size()) {
                itemPositions.clear();
                currentIndex = 0;
                hasStarted = false;
                if (containerEmpty(chest) && close.get() && !waitingToClose) {
                    closeTimer.reset();
                    waitingToClose = true;
                }
            }

            if (waitingToClose && closeTimer.hasTimePassed(getSafeRandom((long) closeDelay.getCurrentMin(), (long) closeDelay.getCurrentMax()))) {
                MC.thePlayer.closeScreen();
                startTimer.reset();
                waitingToClose = false;
            }
        } else {
            // Clear item positions and reset index when the screen is not a chest
            itemPositions.clear();
            currentIndex = 0;
            hasStarted = false;
            waitingToClose = false;
            startTimer.reset();
        }
    }

    private void sortPositions() {
        if (itemPositions.isEmpty()) {
            return;
        }

        String sortMode = sorting.getCurrent();
        switch (sortMode) {
            case "Nearest":
                sortPositionsByNearest();
                break;
            case "Random":
                Collections.shuffle(itemPositions);
                break;
            case "Normal":
            default:
                // No need to sort, just keep the order as it is
                break;
        }
    }
    public static long getSafeRandom(long min, long max) {
        double randomPercent = ThreadLocalRandom.current().nextDouble(0.7, 1.3);
        long delay = (long)(randomPercent * (double)ThreadLocalRandom.current().nextLong(min, max + 1L));
        return delay;
    }
    private void sortPositionsByNearest() {
        List<Point> sortedPositions = new ArrayList<>();
        Point current = itemPositions.get(0);
        sortedPositions.add(current);
        itemPositions.remove(0);

        while (!itemPositions.isEmpty()) {
            final Point finalCurrent = current;
            Point nearest = Collections.min(itemPositions, Comparator.comparingDouble(p -> finalCurrent.distance(p)));
            sortedPositions.add(nearest);
            itemPositions.remove(nearest);
            current = nearest;
        }

        itemPositions = sortedPositions;
    }

    private long getDelayToNextPosition() {
        if (mode.is("MouseSimulation")) {
            if (currentIndex == 0 || currentIndex >= itemPositions.size()) {
                return 0;
            }

            Point current = itemPositions.get(currentIndex - 1);
            Point next = itemPositions.get(currentIndex);
            double distance = current.distance(next);

            // Delay is inversely proportional to currentMouseSpeed
            return (long) (distance / currentMouseSpeed * 300); // Multiply by 100 to get a reasonable delay value
        } else {
            // Return a random delay between the min and max delay values for "Normal" mode
            return getSafeRandom((long) delay.getCurrentMin(), (long) delay.getCurrentMax());
        }
    }


    private boolean containerEmpty(ContainerChest container) {
        for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); ++i) {
            if (container.getLowerChestInventory().getStackInSlot(i) != null) {
                return false;
            }
        }
        return true;
    }

    private boolean containsBestItems(ContainerChest chest) {
        for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); ++i) {
            ItemStack itemStack = chest.getLowerChestInventory().getStackInSlot(i);
            if (itemStack != null && isBestChestItem(itemStack) && isBestItem(itemStack)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBestChestItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemSword || itemStack.getItem() instanceof ItemBow || itemStack.getItem() instanceof ItemArmor || itemStack.getItem() instanceof ItemAxe || itemStack.getItem() instanceof ItemPickaxe || itemStack.getItem() instanceof ItemSpade || itemStack.getItem() instanceof ItemFishingRod) {
            ItemStack bestItem = null;
            GuiChest chest = (GuiChest)MC.currentScreen;

            for(int i = 0; i < chest.inventorySlots.inventorySlots.size() - 36; ++i) {
                ItemStack chestItem = chest.inventorySlots.getSlot(i).getStack();
                if (chestItem != null) {
                    if (itemStack.getItem() instanceof ItemSword && chestItem.getItem() instanceof ItemSword) {
                        if (this.getDamageSword(itemStack) < this.getDamageSword(chestItem)) {
                            return false;
                        }
                    } else if (itemStack.getItem() instanceof ItemBow && chestItem.getItem() instanceof ItemBow) {
                        if (this.getDamageBow(itemStack) < this.getDamageBow(chestItem)) {
                            return false;
                        }
                    } else if (itemStack.getItem() instanceof ItemArmor && chestItem.getItem() instanceof ItemArmor) {
                        if (((ItemArmor)itemStack.getItem()).armorType == ((ItemArmor)chestItem.getItem()).armorType && this.getDamageReduceAmount(itemStack) < this.getDamageReduceAmount(chestItem)) {
                            return false;
                        }
                    } else if (itemStack.getItem() instanceof ItemFishingRod && chestItem.getItem() instanceof ItemFishingRod) {
                        if (this.getBestRod(itemStack) < this.getBestRod(chestItem)) {
                            return false;
                        }
                    } else if (itemStack.getItem() instanceof ItemTool && chestItem.getItem() instanceof ItemTool && this.getToolSpeed(itemStack) < this.getToolSpeed(chestItem)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean isBestItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemSword || itemStack.getItem() instanceof ItemBow || itemStack.getItem() instanceof ItemArmor || itemStack.getItem() instanceof ItemAxe || itemStack.getItem() instanceof ItemPickaxe || itemStack.getItem() instanceof ItemSpade || itemStack.getItem() instanceof ItemFishingRod) {
            for(int i = 0; i < MC.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
                ItemStack inventoryStack = MC.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (inventoryStack != null) {
                    if (itemStack.getItem() instanceof ItemSword && inventoryStack.getItem() instanceof ItemSword) {
                        if (this.getDamageSword(itemStack) <= this.getDamageSword(inventoryStack)) {
                            return false;
                        }
                    } else if (itemStack.getItem() instanceof ItemBow && inventoryStack.getItem() instanceof ItemBow) {
                        if (this.getDamageBow(itemStack) <= this.getDamageBow(inventoryStack)) {
                            return false;
                        }
                    } else if (itemStack.getItem() instanceof ItemArmor && inventoryStack.getItem() instanceof ItemArmor) {
                        if (((ItemArmor)itemStack.getItem()).armorType == ((ItemArmor)inventoryStack.getItem()).armorType && this.getDamageReduceAmount(itemStack) <= this.getDamageReduceAmount(inventoryStack)) {
                            return false;
                        }
                    } else if (itemStack.getItem() instanceof ItemFishingRod && inventoryStack.getItem() instanceof ItemFishingRod) {
                        if (this.getBestRod(itemStack) <= this.getBestRod(inventoryStack)) {
                            return false;
                        }
                    } else if (itemStack.getItem() instanceof ItemTool && inventoryStack.getItem() instanceof ItemTool && this.getToolSpeed(itemStack) <= this.getToolSpeed(inventoryStack)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private double getDamageSword(ItemStack itemStack) {
        double damage = 0.0;
        if (itemStack.getItem() instanceof ItemSword) {
            damage += (double)(((ItemSword)itemStack.getItem()).getAttackDamage() + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) * 1.25F);
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, itemStack) / 11.0;
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, itemStack) / 11.0;
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack) / 11.0;
            damage -= (double)itemStack.getItemDamage() / 10000.0;
        }

        return damage;
    }

    private double getDamageBow(ItemStack itemStack) {
        double damage = 0.0;
        if (itemStack.getItem() instanceof ItemBow) {
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemStack) / 11.0;
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemStack) / 8.0;
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemStack) / 8.0;
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, itemStack) / 11.0;
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack) / 11.0;
            damage -= (double)itemStack.getItemDamage() / 10000.0;
        }

        return damage;
    }

    private double getToolSpeed(ItemStack itemStack) {
        double damage = 0.0;
        if (itemStack.getItem() instanceof ItemTool) {
            if (itemStack.getItem() instanceof ItemAxe) {
                damage += (double)(itemStack.getItem().getStrVsBlock(itemStack, new Block(Material.wood, MapColor.woodColor)) + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack));
            } else if (itemStack.getItem() instanceof ItemPickaxe) {
                damage += (double)(itemStack.getItem().getStrVsBlock(itemStack, new Block(Material.rock, MapColor.stoneColor)) + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack));
            } else if (itemStack.getItem() instanceof ItemSpade) {
                damage += (double)(itemStack.getItem().getStrVsBlock(itemStack, new Block(Material.sand, MapColor.sandColor)) + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack));
            }

            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack) / 11.0;
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, itemStack) / 11.0;
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) / 33.0;
            damage -= (double)itemStack.getItemDamage() / 10000.0;
        }

        return damage;
    }

    private double getDamageReduceAmount(ItemStack itemStack) {
        double damageReduceAmount = 0.0;
        if (itemStack.getItem() instanceof ItemArmor) {
            damageReduceAmount += (double)((float)((ItemArmor)itemStack.getItem()).damageReduceAmount + (float)(6 + EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, itemStack) * EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, itemStack)) / 3.0F);
            damageReduceAmount += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, itemStack) / 11.0;
            damageReduceAmount += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, itemStack) / 11.0;
            damageReduceAmount += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, itemStack) / 11.0;
            damageReduceAmount += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack) / 11.0;
            damageReduceAmount += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, itemStack) / 11.0;
            damageReduceAmount += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.featherFalling.effectId, itemStack) / 11.0;
            if (((ItemArmor)itemStack.getItem()).armorType == 0 && ((ItemArmor)itemStack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.GOLD) {
                damageReduceAmount -= 0.01;
            }

            damageReduceAmount -= (double)itemStack.getItemDamage() / 10000.0;
        }

        return damageReduceAmount;
    }

    private double getBestRod(ItemStack itemStack) {
        double damage = 0.0;
        if (itemStack.getItem() instanceof ItemFishingRod) {
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.lure.effectId, itemStack) / 11.0;
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack) / 11.0;
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.luckOfTheSea.effectId, itemStack) / 33.0;
            damage -= (double)itemStack.getItemDamage() / 10000.0;
        }

        return damage;
    }
    @Handler
    public final void render2D(final EventRender2D event) {
        if (MC.currentScreen instanceof GuiChest) {
            GuiChest guiChest = (GuiChest) MC.currentScreen;

            final int slotSize = 18; // Each slot is 18x18 pixels
            final int startX = guiChest.guiLeft + 8;
            final int startY = guiChest.guiTop + 17;

            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            for (int i = 0; i < itemPositions.size(); i++) {
                Point pos = itemPositions.get(i);
                int x = startX + pos.x * slotSize;
                int y = startY + pos.y * slotSize;

                if (i < currentIndex) {
                    // Already processed slots (green)
                    drawRect(x, y, x + slotSize, y + slotSize, 0x6000FF00);
                } else {
                    // Slots yet to be processed (red)
                    drawRect(x, y, x + slotSize, y + slotSize, 0x60FF0000);
                }
            }

            if (currentIndex < itemPositions.size()) {
                Point currentPos = itemPositions.get(currentIndex);
                int currentX = startX + currentPos.x * slotSize;
                int currentY = startY + currentPos.y * slotSize;

                // Current slot being processed (blue)
                drawRect(currentX, currentY, currentX + slotSize, currentY + slotSize, 0x600000FF);
            }

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        GL11.glColor4f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >> 24 & 255) / 255.0F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(left, top);
        GL11.glVertex2f(left, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);
        GL11.glEnd();
    }
}
