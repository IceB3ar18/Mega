package club.mega.util;

import club.mega.Mega;
import club.mega.module.impl.misc.Discord;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.arikia.dev.drpc.DiscordUser;
import net.arikia.dev.drpc.callbacks.ReadyCallback;

public class DiscordRP {

    private boolean running = true;
    private long created = 0;

    public void start()  {
        this.created = System.currentTimeMillis();
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(new ReadyCallback() {
            @Override
            public void apply(DiscordUser discordUser) {
                update("Starting...", "");
            }
        }).build();
        DiscordRPC.discordInitialize("1224739952584167486", handlers, running);

        new Thread("Discord RPC callbacks") {
            @Override
            public void run() {
                while(running) {
                    DiscordRPC.discordRunCallbacks();
                }
            }
        }.start();
    }

    public void shutDown() {
        running = false;
        DiscordRPC.discordShutdown();
    }
    public void update(String firstLine, String secondLine) {
        if(Mega.INSTANCE.getModuleManager().getModule(Discord.class).isToggled()) {
            DiscordRichPresence.Builder b = new DiscordRichPresence.Builder(secondLine);
            b.setBigImage("large", "");
            b.setDetails(firstLine);
            b.setStartTimestamps(created);

            DiscordRPC.discordUpdatePresence(b.build());
        }
    }
}
