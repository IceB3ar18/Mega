package club.mega.module.impl.player;

import club.mega.event.Event;
import club.mega.event.impl.EventMouseClicked;
import club.mega.event.impl.EventTick;
import club.mega.event.impl.TickEvent;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.util.ChatUtil;
import club.mega.util.FriendUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Mouse;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "MCF", description = "Adds or removeds a friend with mid click", category = Category.PLAYER)
public class MCF extends Module {
    private long milliSeconds = System.currentTimeMillis();
    @Handler
    public final void onTick(final TickEvent eventTick) {

            if (MC.gameSettings.keyBindPickBlock.pressed && MC.objectMouseOver != null && MC.objectMouseOver.entityHit != null) {
                MC.gameSettings.keyBindPickBlock.pressed = false;
                final Entity entity = MC.objectMouseOver.entityHit;
                if(entity instanceof EntityPlayer && entity.getName() != null) {
                    if (!FriendUtil.isFriendString(entity.getName())) {
                        FriendUtil.addFriend(entity.getName());
                        ChatUtil.sendMessage("Added friend §e" + entity.getName());
                    } else {
                        FriendUtil.removeFriend(entity.getName());
                        ChatUtil.sendMessage("Removed friend §e" + entity.getName());
                    }
                }
            }
        }
    
}
