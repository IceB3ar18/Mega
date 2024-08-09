package club.mega.module.impl.player;

import club.mega.event.impl.EventPacket;
import club.mega.event.impl.EventTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.TextSetting;
import club.mega.util.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import org.apache.commons.lang3.StringUtils;
import rip.hippo.lwjeb.annotation.Handler;

@Module.ModuleInfo(name = "AutoRegister", description = "Registers automaticly", category = Category.MISC)
public class AutoRegister extends Module {
    private final TimeUtil timeHelper = new TimeUtil();
    private String text;

    private final TextSetting password = new TextSetting("Password", this, "Pass1234!");
    String pass;
    @Handler
    public final void packet(final EventPacket event) {
        if (MC.theWorld != null) {
            String pass = "";
            String passwordText = password.getText();
            if (passwordText != null) {
                pass = passwordText.replace(" ", "");
            }
            Packet packet = event.getPacket();
            if (packet instanceof S02PacketChat && MC.theWorld != null) {
                S02PacketChat s02PacketChat = (S02PacketChat) packet;
                String text = s02PacketChat.getChatComponent().getUnformattedText();
                if (!StringUtils.containsIgnoreCase(text, "/register") && !StringUtils.containsIgnoreCase(text, "/register password password") && !text.equalsIgnoreCase("/register <password> <password>")) {
                    if (StringUtils.containsIgnoreCase(text, "/login password") || StringUtils.containsIgnoreCase(text, "/login") || text.equalsIgnoreCase("/login <password>")) {

                        MC.thePlayer.sendChatMessage("/login " + pass);
                        this.text = "/login " + pass;
                        this.timeHelper.reset();
                    }
                } else {
                    MC.thePlayer.sendChatMessage("/register " + pass + " " + pass);
                    this.text = "/register " + pass + " " + pass;
                    this.timeHelper.reset();
                }
            }
            if (packet instanceof S02PacketChat && MC.theWorld != null) {
                S02PacketChat s02PacketChat = (S02PacketChat) packet;
                String text = s02PacketChat.getChatComponent().getUnformattedText();
                if (text.equalsIgnoreCase("/reg [пароль]")) {
                    MC.thePlayer.sendChatMessage("/reg " + pass);
                } else if(text.equalsIgnoreCase("/login [пароль]")) {
                    MC.thePlayer.sendChatMessage("/login " + pass);
                }
            }
        }
    }
    @Handler
    public final void tick(final EventTick event) {
        if (this.timeHelper.hasTimePassed(1500L) && this.text != null && !this.text.equals("")) {
            MC.thePlayer.sendChatMessage(this.text);
            System.out.println(this.text);
            this.text = "";
        }
    }
}
