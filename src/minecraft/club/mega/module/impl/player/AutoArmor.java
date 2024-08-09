package club.mega.module.impl.player;

import club.mega.Mega;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.module.setting.impl.RangeSetting;
import club.mega.util.ChatUtil;
import club.mega.util.RandomUtil;
import club.mega.util.TimeUtil;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import rip.hippo.lwjeb.annotation.Handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

@Module.ModuleInfo(name = "AutoArmor", description = "Equips armor automaticly", category = Category.PLAYER)
public class AutoArmor extends Module {

    private ArrayList<ItemStack> chestPlates;
    private ArrayList<ItemStack> helmets;
    private ArrayList<ItemStack> boots;
    private ArrayList<ItemStack> trousers;
    private boolean blockInv;
    private int oldSlotID;
    private boolean b1 = true;
    private boolean invManager = true;
    private final TimeUtil timeHelper = new TimeUtil();
    private final TimeUtil timeHelper2 = new TimeUtil();
    private final TimeUtil timeHelper3 = new TimeUtil();
    private final TimeUtil timeHelper4 = new TimeUtil();

    public final ListSetting mode = new ListSetting("Mode", this, new String[]{"OpenInv", "Basic"});
    public final NumberSetting startDelay = new NumberSetting("StartDelay", this, 0.0, 1000.0, 200.0, 1);
    private final RangeSetting delay = new RangeSetting("Delay", this, 1.0, 400, 90, 120, 1.0);
    private final BooleanSetting hotbar = new BooleanSetting("Hotbar", this, true);
    private final BooleanSetting gommeQSG = new BooleanSetting("GommeQSG", this, true);
    private final BooleanSetting noMove = new BooleanSetting("NoMove", this, false);
    public final NumberSetting hotbarStartDelay = new NumberSetting("HStartDelay", this, 0.0, 1000.0, 200.0, 1);
    public final NumberSetting hotbarDelay = new NumberSetting("HotbarDelay", this, 0.0, 400.0, 100.0, 1);

    public void onEnable() {
        super.onEnable();

    }

    public static AutoArmor getInstance() {
        return Mega.INSTANCE.getModuleManager().getModule(AutoArmor.class);
    }

    @Handler
    public final void earlyTick(final EventTick eventTick) {
        this.setTag(delay.getCurrentMin() + " - " + delay.getCurrentMax() + " ms");
    }


    public void newAutoArmorHotbar() {
        //ChatUtil.sendMessage(MC.thePlayer.inventoryContainer.getSlot(102).getStack() + "");
        this.chestPlates = new ArrayList();
        this.helmets = new ArrayList();
        this.trousers = new ArrayList();
        this.boots = new ArrayList();
        long random2 = RandomUtil.nextLong(-35L, 35L);
        long random3 = RandomUtil.nextLong(-35L, 35L);
        long delay2 = (long) (this.hotbarDelay.getAsLong() + (double) random2);
        long delay3 = (long) (this.hotbarStartDelay.getAsLong() + (double) random3);
        ItemStack helm = null;
        ItemStack boot = null;
        ItemStack chestPlate = null;
        ItemStack trouser = null;
        ItemStack newHelm = null;
        ItemStack newBoot = null;
        ItemStack newChestPlate = null;
        ItemStack newTrouser = null;
        if (this.hotbar.get() && MC.currentScreen == null) {
            boolean qsg = this.gommeQSG.get();

            int i;
            ItemStack itemStack;
            for (i = 36; i < MC.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
                itemStack = MC.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (itemStack != null && itemStack.getItem() instanceof ItemArmor) {
                    if (((ItemArmor) itemStack.getItem()).armorType == 0) {
                        itemStack.setSlotID(i);
                        this.helmets.add(itemStack);
                    } else if (((ItemArmor) itemStack.getItem()).armorType == 1) {
                        itemStack.setSlotID(i);
                        this.chestPlates.add(itemStack);
                    } else if (((ItemArmor) itemStack.getItem()).armorType == 2) {
                        itemStack.setSlotID(i);
                        this.trousers.add(itemStack);
                    } else if (((ItemArmor) itemStack.getItem()).armorType == 3) {
                        itemStack.setSlotID(i);
                        this.boots.add(itemStack);
                    }
                }
            }

            for (i = 0; i < MC.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
                itemStack = MC.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (itemStack != null && itemStack.getItem() instanceof ItemArmor) {
                    if (i == 5) {
                        itemStack.setSlotID(i);
                        helm = itemStack;
                    }

                    if (i == 6) {
                        itemStack.setSlotID(i);
                        chestPlate = itemStack;
                    }

                    if (i == 7) {
                        itemStack.setSlotID(i);
                        trouser = itemStack;
                    }

                    if (i == 8) {
                        itemStack.setSlotID(i);
                        boot = itemStack;
                    }
                }
            }

            Iterator var20 = this.helmets.iterator();

            while (var20.hasNext()) {
                itemStack = (ItemStack) var20.next();
                if (helm == null) {
                    if (newHelm != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newHelm)) {
                            newHelm = itemStack;
                        }
                    } else {
                        newHelm = itemStack;
                    }
                } else if (helm.getItem() instanceof ItemArmor && qsg) {
                    if (newHelm != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newHelm)) {
                            newHelm = itemStack;
                        }
                    } else if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(helm)) {
                        newHelm = itemStack;
                    }
                }
            }

            var20 = this.chestPlates.iterator();

            while (var20.hasNext()) {
                itemStack = (ItemStack) var20.next();
                if (chestPlate == null) {
                    if (newChestPlate != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newChestPlate)) {
                            newChestPlate = itemStack;
                        }
                    } else {
                        newChestPlate = itemStack;
                    }
                } else if (chestPlate.getItem() instanceof ItemArmor && qsg) {
                    if (newChestPlate != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newChestPlate)) {
                            newChestPlate = itemStack;
                        }
                    } else if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(chestPlate)) {
                        newChestPlate = itemStack;
                    }
                }
            }

            var20 = this.trousers.iterator();

            while (var20.hasNext()) {
                itemStack = (ItemStack) var20.next();
                if (trouser == null) {
                    if (newTrouser != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newTrouser)) {
                            newTrouser = itemStack;
                        }
                    } else {
                        newTrouser = itemStack;
                    }
                } else if (trouser.getItem() instanceof ItemArmor && qsg) {
                    if (newTrouser != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newTrouser)) {
                            newTrouser = itemStack;
                        }
                    } else if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(trouser)) {
                        newTrouser = itemStack;
                    }
                }
            }

            var20 = this.boots.iterator();

            while (var20.hasNext()) {
                itemStack = (ItemStack) var20.next();
                if (boot == null) {
                    if (newBoot != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newBoot)) {
                            newBoot = itemStack;
                        }
                    } else {
                        newBoot = itemStack;
                    }
                } else if (boot.getItem() instanceof ItemArmor && qsg) {
                    if (newBoot != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newBoot)) {
                            newBoot = itemStack;
                        }
                    } else if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(boot)) {
                        newBoot = itemStack;
                    }
                }
            }

            if (this.timeHelper3.hasTimePassed(delay3) && this.timeHelper2.hasTimePassed(delay2)) {

                if (newBoot == null && newTrouser == null && newChestPlate == null && newHelm == null) {
                    this.timeHelper3.reset();
                    if (this.b1) {
                        MC.thePlayer.inventory.currentItem = this.oldSlotID;
                    }

                    this.oldSlotID = MC.thePlayer.inventory.currentItem;
                    this.b1 = false;
                } else {
                    if (newChestPlate != null && !hasChestplate()) {
                        if (chestPlate == null) {
                            this.rightClick(newChestPlate.getSlotID());
                            this.b1 = true;

                        } else if (qsg) {
                            this.rightClick(newChestPlate.getSlotID());
                            this.b1 = true;

                        }

                        this.timeHelper2.reset();
                        return;
                    }

                    if (newTrouser != null && !hasLeggings()) {
                        if (trouser == null) {
                            this.rightClick(newTrouser.getSlotID());
                            this.b1 = true;
                        } else if (qsg) {
                            this.rightClick(newTrouser.getSlotID());
                            this.b1 = true;
                        }

                        this.timeHelper2.reset();
                        return;
                    }

                    if (newHelm != null && !hasHelmet()) {
                        if (helm == null) {
                            this.rightClick(newHelm.getSlotID());
                            this.b1 = true;
                        } else if (qsg) {
                            this.rightClick(newHelm.getSlotID());
                            this.b1 = true;
                        }

                        this.timeHelper2.reset();
                        return;
                    }

                    if (newBoot != null && !hasBoots()) {
                        if (boot == null) {
                            this.rightClick(newBoot.getSlotID());
                            this.b1 = true;
                        } else if (qsg) {
                            this.rightClick(newBoot.getSlotID());
                            this.b1 = true;
                        }

                        this.timeHelper2.reset();
                        return;
                    }
                }
            }
        }

    }

    public static boolean hasHelmet() {
        ItemStack helmetItemStack = MC.thePlayer.getCurrentArmor(3);
        return helmetItemStack != null && helmetItemStack.getItem() instanceof net.minecraft.item.ItemArmor && ((net.minecraft.item.ItemArmor) helmetItemStack.getItem()).armorType == 0;
    }

    public static boolean hasChestplate() {
        ItemStack chestplateItemStack = MC.thePlayer.getCurrentArmor(2);
        return chestplateItemStack != null && chestplateItemStack.getItem() instanceof net.minecraft.item.ItemArmor && ((net.minecraft.item.ItemArmor) chestplateItemStack.getItem()).armorType == 1;
    }

    public static boolean hasLeggings() {
        ItemStack leggingsItemStack = MC.thePlayer.getCurrentArmor(1);
        return leggingsItemStack != null && leggingsItemStack.getItem() instanceof net.minecraft.item.ItemArmor && ((net.minecraft.item.ItemArmor) leggingsItemStack.getItem()).armorType == 2;
    }

    public static boolean hasBoots() {
        ItemStack bootsItemStack = MC.thePlayer.getCurrentArmor(0);
        return bootsItemStack != null && bootsItemStack.getItem() instanceof net.minecraft.item.ItemArmor && ((net.minecraft.item.ItemArmor) bootsItemStack.getItem()).armorType == 3;
    }

    public void newAutoArmor(boolean startDelay) {
        this.chestPlates = new ArrayList();
        this.helmets = new ArrayList();
        this.trousers = new ArrayList();
        this.boots = new ArrayList();
        long random = RandomUtil.nextLong(-25L, 25L);
        long random2 = RandomUtil.nextLong(-25L, 25L);
        long random3 = RandomUtil.nextLong(-25L, 25L);
        long random4 = RandomUtil.nextLong(-25L, 25L);
        long delay = (long) (this.delay.getCurrentMin() + (double) random);
        long var10000 = (long) (this.hotbarDelay.getAsLong() + (double) random2);
        long delay4 = 0L;
        var10000 = (long) (this.hotbarStartDelay.getAsLong() + (double) random3);
        if (startDelay) {
            delay4 = (long) (this.startDelay.getAsLong() + (double) random4);
        }

        ItemStack helm = null;
        ItemStack boot = null;
        ItemStack chestPlate = null;
        ItemStack trouser = null;
        ItemStack newHelm = null;
        ItemStack newBoot = null;
        ItemStack newChestPlate = null;
        ItemStack newTrouser = null;
        boolean autoArmor = MC.currentScreen instanceof GuiInventory || !this.mode.getCurrent().equalsIgnoreCase("OpenInv") && (MC.currentScreen == null || MC.currentScreen instanceof GuiInventory) && (!this.noMove.get() || this.noMove.get() && !MC.thePlayer.isMoving());
        if (autoArmor && !this.blockInv) {
            ItemStack itemStack;
            for (int i = 0; i < MC.thePlayer.inventoryContainer.inventorySlots.size(); ++i) {
                itemStack = MC.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (itemStack != null && itemStack.getItem() instanceof ItemArmor) {
                    if (((ItemArmor) itemStack.getItem()).armorType == 0) {
                        itemStack.setSlotID(i);
                        this.helmets.add(itemStack);
                    } else if (((ItemArmor) itemStack.getItem()).armorType == 1) {
                        itemStack.setSlotID(i);
                        this.chestPlates.add(itemStack);
                    } else if (((ItemArmor) itemStack.getItem()).armorType == 2) {
                        itemStack.setSlotID(i);
                        this.trousers.add(itemStack);
                    } else if (((ItemArmor) itemStack.getItem()).armorType == 3) {
                        itemStack.setSlotID(i);
                        this.boots.add(itemStack);
                    }

                    if (itemStack.getSlotID() == 5) {
                        itemStack.setSlotID(i);
                        helm = itemStack;
                    }

                    if (itemStack.getSlotID() == 6) {
                        itemStack.setSlotID(i);
                        chestPlate = itemStack;
                    }

                    if (itemStack.getSlotID() == 7) {
                        itemStack.setSlotID(i);
                        trouser = itemStack;
                    }

                    if (itemStack.getSlotID() == 8) {
                        itemStack.setSlotID(i);
                        boot = itemStack;
                    }
                }
            }

            Iterator var29 = this.helmets.iterator();

            while (var29.hasNext()) {
                itemStack = (ItemStack) var29.next();
                if (helm == null) {
                    if (newHelm != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newHelm)) {
                            newHelm = itemStack;
                        } else if (this.getDamageReduceAmount(itemStack) == this.getDamageReduceAmount(newHelm) && itemStack.getItemDamage() < newHelm.getItemDamage()) {
                            newHelm = itemStack;
                        }
                    } else {
                        newHelm = itemStack;
                    }
                } else if (helm.getItem() instanceof ItemArmor) {
                    if (newHelm != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newHelm)) {
                            newHelm = itemStack;
                        } else if (this.getDamageReduceAmount(itemStack) == this.getDamageReduceAmount(newHelm) && itemStack.getItemDamage() < newHelm.getItemDamage()) {
                            newHelm = itemStack;
                        }
                    } else if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(helm)) {
                        newHelm = itemStack;
                    } else if (this.getDamageReduceAmount(itemStack) == this.getDamageReduceAmount(helm) && itemStack.getItemDamage() < helm.getItemDamage()) {
                        newHelm = itemStack;
                    }
                }
            }

            var29 = this.chestPlates.iterator();

            while (var29.hasNext()) {
                itemStack = (ItemStack) var29.next();
                if (chestPlate == null) {
                    if (newChestPlate != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newChestPlate)) {
                            newChestPlate = itemStack;
                        } else if (this.getDamageReduceAmount(itemStack) == this.getDamageReduceAmount(newChestPlate) && itemStack.getItemDamage() < newChestPlate.getItemDamage()) {
                            newChestPlate = itemStack;
                        }
                    } else {
                        newChestPlate = itemStack;
                    }
                } else if (chestPlate.getItem() instanceof ItemArmor) {
                    if (newChestPlate != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newChestPlate)) {
                            newChestPlate = itemStack;
                        } else if (this.getDamageReduceAmount(itemStack) == this.getDamageReduceAmount(newChestPlate) && itemStack.getItemDamage() < newChestPlate.getItemDamage()) {
                            newChestPlate = itemStack;
                        }
                    } else if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(chestPlate)) {
                        newChestPlate = itemStack;
                    } else if (this.getDamageReduceAmount(itemStack) == this.getDamageReduceAmount(chestPlate) && itemStack.getItemDamage() < chestPlate.getItemDamage()) {
                        newChestPlate = itemStack;
                    }
                }
            }

            var29 = this.trousers.iterator();

            while (var29.hasNext()) {
                itemStack = (ItemStack) var29.next();
                if (trouser == null) {
                    if (newTrouser != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newTrouser)) {
                            newTrouser = itemStack;
                        } else if (this.getDamageReduceAmount(itemStack) == this.getDamageReduceAmount(newTrouser) && itemStack.getItemDamage() < newTrouser.getItemDamage()) {
                            newTrouser = itemStack;
                        }
                    } else {
                        newTrouser = itemStack;
                    }
                } else if (trouser.getItem() instanceof ItemArmor) {
                    if (newTrouser != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newTrouser)) {
                            newTrouser = itemStack;
                        } else if (this.getDamageReduceAmount(itemStack) == this.getDamageReduceAmount(newTrouser) && itemStack.getItemDamage() < newTrouser.getItemDamage()) {
                            newTrouser = itemStack;
                        }
                    } else if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(trouser)) {
                        newTrouser = itemStack;
                    } else if (this.getDamageReduceAmount(itemStack) == this.getDamageReduceAmount(trouser) && itemStack.getItemDamage() < trouser.getItemDamage()) {
                        newTrouser = itemStack;
                    }
                }
            }

            var29 = this.boots.iterator();

            while (var29.hasNext()) {
                itemStack = (ItemStack) var29.next();
                if (boot == null) {
                    if (newBoot != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newBoot)) {
                            newBoot = itemStack;
                        } else if (this.getDamageReduceAmount(itemStack) == this.getDamageReduceAmount(newBoot) && itemStack.getItemDamage() > newBoot.getItemDamage()) {
                            newBoot = itemStack;
                        }
                    } else {
                        newBoot = itemStack;
                    }
                } else if (boot.getItem() instanceof ItemArmor) {
                    if (newBoot != null) {
                        if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(newBoot)) {
                            newBoot = itemStack;
                        } else if (this.getDamageReduceAmount(itemStack) == this.getDamageReduceAmount(newBoot) && itemStack.getItemDamage() < newBoot.getItemDamage()) {
                            newBoot = itemStack;
                        }
                    } else if (this.getDamageReduceAmount(itemStack) > this.getDamageReduceAmount(boot)) {
                        newBoot = itemStack;
                    } else if (this.getDamageReduceAmount(itemStack) == this.getDamageReduceAmount(boot) && itemStack.getItemDamage() < boot.getItemDamage()) {
                        newBoot = itemStack;
                    }
                }
            }

            if (this.timeHelper4.hasTimePassed(delay4) && this.timeHelper.hasTimePassed(delay)) {
                if (newBoot == null && newTrouser == null && newChestPlate == null && newHelm == null) {
                    this.invManager = true;
                }

                if (newChestPlate != null) {
                    if (chestPlate == null) {
                        this.shiftClick(newChestPlate.getSlotID());
                    } else {
                        this.replaceArmor(6);
                    }

                    this.timeHelper.reset();
                    this.invManager = false;
                    if (this.delay.getCurrentMin() != 0.0) {
                        return;
                    }
                }

                if (newTrouser != null) {
                    if (trouser == null) {
                        this.shiftClick(newTrouser.getSlotID());
                    } else {
                        this.replaceArmor(7);
                    }

                    this.timeHelper.reset();
                    this.invManager = false;
                    if (this.delay.getCurrentMin() != 0.0) {
                        return;
                    }
                }

                if (newHelm != null) {
                    if (helm == null) {
                        this.shiftClick(newHelm.getSlotID());
                    } else {
                        this.replaceArmor(5);
                    }

                    this.timeHelper.reset();
                    this.invManager = false;
                    if (this.delay.getCurrentMin() != 0.0) {
                        return;
                    }
                }

                if (newBoot != null) {
                    if (boot == null) {
                        this.shiftClick(newBoot.getSlotID());
                    } else {
                        this.replaceArmor(8);
                    }

                    this.timeHelper.reset();
                    this.invManager = false;
                    if (this.delay.getCurrentMin() != 0.0) {
                        return;
                    }
                }
            }
        } else {
            this.timeHelper4.reset();
        }
    }

    public boolean isInvManager() {
        return this.invManager;
    }

    private void shiftClick(int slotID) {
        if (this.mode.getCurrent().equalsIgnoreCase("OpenInv")) {
            Slot slot1 = MC.thePlayer.inventoryContainer.getSlot(slotID);

            try {
                GuiInventory guiInventory = (GuiInventory) MC.currentScreen;
                guiInventory.forceShift = true;
                guiInventory.mouseClicked(slot1.xDisplayPosition + 2 + guiInventory.guiLeft, slot1.yDisplayPosition + 2 + guiInventory.guiTop, 0);
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        } else {
            MC.playerController.windowClick(MC.thePlayer.inventoryContainer.windowId, slotID, 0, 1, MC.thePlayer);
        }

        this.timeHelper.reset();
    }

    private void replaceArmor(int slotID) {
        MC.playerController.windowClick(MC.thePlayer.inventoryContainer.windowId, slotID, 1, 4, MC.thePlayer);
        this.timeHelper.reset();
    }

    private void rightClick(int slotID) {

        MC.thePlayer.inventory.currentItem = slotID - 36;
        MC.rightClickMouse();
        this.timeHelper.reset();
    }

    private double getDamageReduceAmount(ItemStack itemStack) {
        double damageReduceAmount = 0.0;
        if (itemStack.getItem() instanceof ItemArmor) {
            damageReduceAmount += (double) ((float) ((ItemArmor) itemStack.getItem()).damageReduceAmount + (float) (6 + EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, itemStack) * EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, itemStack)) / 3.0F);
            damageReduceAmount += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, itemStack) / 11.0;
            damageReduceAmount += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, itemStack) / 11.0;
            damageReduceAmount += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, itemStack) / 11.0;
            damageReduceAmount += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, itemStack) / 11.0;
            damageReduceAmount += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, itemStack) / 11.0;
            damageReduceAmount += (double) EnchantmentHelper.getEnchantmentLevel(Enchantment.featherFalling.effectId, itemStack) / 11.0;
            if (((ItemArmor) itemStack.getItem()).armorType == 0 && ((ItemArmor) itemStack.getItem()).getArmorMaterial() == ArmorMaterial.GOLD) {
                damageReduceAmount -= 0.01;
            }
        }

        return damageReduceAmount;
    }
}
