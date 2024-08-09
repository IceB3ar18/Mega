package club.mega.util;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.*;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;

public class BlockUtil {
    private final ArrayList<Item> nonValidItems = new ArrayList();

    public BlockUtil() {
        this.nonValidItems.add(Item.getItemById(30));
        this.nonValidItems.add(Item.getItemById(58));
        this.nonValidItems.add(Item.getItemById(116));
        this.nonValidItems.add(Item.getItemById(158));
        this.nonValidItems.add(Item.getItemById(23));
        this.nonValidItems.add(Item.getItemById(6));
        this.nonValidItems.add(Item.getItemById(54));
        this.nonValidItems.add(Item.getItemById(146));
        this.nonValidItems.add(Item.getItemById(130));
        this.nonValidItems.add(Item.getItemById(26));
        this.nonValidItems.add(Item.getItemById(50));
        this.nonValidItems.add(Item.getItemById(76));
        this.nonValidItems.add(Item.getItemById(46));
        this.nonValidItems.add(Item.getItemById(37));
        this.nonValidItems.add(Item.getItemById(38));
    }

    public boolean isValidStack(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!(item instanceof ItemSlab || item instanceof ItemLeaves || item instanceof ItemSnow || item instanceof ItemBanner || item instanceof ItemFlintAndSteel)) {
            for (Item item1 : this.nonValidItems) {
                if (!item.equals(item1)) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean isValidBock(BlockPos blockPos) {
        Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();
        return !(block instanceof BlockLiquid) && !(block instanceof BlockAir) && !(block instanceof BlockChest) && !(block instanceof BlockFurnace);
    }

    public static boolean isAirBlock(BlockPos blockPos) {
        Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();
        return block instanceof BlockAir;
    }
}

