package club.mega.util;

import club.mega.Mega;
import club.mega.interfaces.MinecraftInterface;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.ChatComponentText;

public final class ChatUtil implements MinecraftInterface {

    public static void sendMessage(final String message) {
        MC.thePlayer.addChatMessage(new ChatComponentText(ChatFormatting.WHITE + "[" + ChatFormatting.RED + Mega.INSTANCE.getName() + ChatFormatting.WHITE + "] " + ChatFormatting.GRAY + message));
    }

    public static void sendMessage(final double message) {
        sendMessage(String.valueOf(message));
    }

}
