package club.mega;

import club.mega.command.CommandRegistry;
import club.mega.event.impl.EventChat;
import club.mega.event.impl.EventKey;
import club.mega.event.impl.EventResize;
import club.mega.file.ModuleSaver;
import club.mega.fontrenderer.FontManager;
import club.mega.gui.altmanager.GuiAltManager;
import club.mega.gui.changelog.Changelog;
import club.mega.gui.click.ClickGUI;
import club.mega.gui.clicknew.ClickGui;
import club.mega.interfaces.MinecraftInterface;
import club.mega.module.Module;
import club.mega.module.ModuleManager;
import club.mega.module.impl.combat.KillAura;
import club.mega.module.impl.player.Scaffold;
import club.mega.util.DiscordRP;
import club.mega.util.RenderUtil;
import club.mega.util.SlotSpoofHandler;
import de.florianmichael.viamcp.ViaMCP;
import net.minecraft.client.gui.GuiMainMenu;
import rip.hippo.lwjeb.annotation.Handler;
import rip.hippo.lwjeb.bus.PubSub;
import rip.hippo.lwjeb.configuration.BusConfigurations;
import rip.hippo.lwjeb.configuration.config.impl.BusPubSubConfiguration;
import rip.hippo.lwjeb.message.scan.impl.MethodAndFieldBasedMessageScanner;

public enum Mega implements MinecraftInterface {

    INSTANCE;

    private String username;

    private final PubSub<String> pubSub = new PubSub<>(new BusConfigurations.Builder().setConfiguration(BusPubSubConfiguration.class, () -> {
        BusPubSubConfiguration busPubSubConfiguration = BusPubSubConfiguration.getDefault();
        busPubSubConfiguration.setScanner(new MethodAndFieldBasedMessageScanner<>());
        return busPubSubConfiguration;
    }).build());
    private Changelog changelog;

    private ModuleManager moduleManager;
    private CommandRegistry commandRegistry;
    private FontManager fontManager;
    private ClickGUI clickGUI;
    private ClickGui clickGui;
    private GuiAltManager altManager;
    private SlotSpoofHandler slotSpoofHandler;
    private DiscordRP discordRP;

    public DiscordRP getDiscordRP() {
        return discordRP;
    }

    public final void startUp() {

        pubSub.subscribe(this);
        fontManager = new FontManager();
        moduleManager = new ModuleManager();
        commandRegistry = new CommandRegistry();
        clickGUI = new ClickGUI();
        clickGui = new ClickGui();
        altManager = new GuiAltManager(new GuiMainMenu());
        changelog = new Changelog();
        slotSpoofHandler = new SlotSpoofHandler();
        discordRP = new DiscordRP();
        discordRP.start();
        ModuleSaver.load();
        ModuleSaver.loadClientState();

        for (final Module module : Mega.INSTANCE.getModuleManager().getModules()) {
            module.setCurrentWidth(RenderUtil.getScaledResolution().getScaledWidth());
            module.setCurrentHeight(0);
        }
        if(getModuleManager().getModule(KillAura.class).isToggled())
            getModuleManager().getModule(KillAura.class).toggle();
        if(getModuleManager().getModule(Scaffold.class).isToggled())
            getModuleManager().getModule(Scaffold.class).toggle();
        try {
            ViaMCP.create();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void shutDown() {
        pubSub.unsubscribe(this);
        altManager.loadAlts();
        discordRP.shutDown();
        ModuleSaver.save();
    }
    public SlotSpoofHandler getSlotSpoofHandler() {
        return this.slotSpoofHandler;
    }
    @Handler
    public final void resize(final EventResize event) {
        changelog = new Changelog();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Handler
    public final String getUsername() {
        return username;
    }

    @Handler
    public final void key(final EventKey event) {
        moduleManager.getModules().forEach(module -> {
            if (module.getKey() == event.getKey())
                module.toggle();
        });

    }

    @Handler
    public final void chat(final EventChat event) {
        if (event.getMessage().startsWith(".")) {
            event.setCancelled(true);
            getCommandRegistry().execute(event.getMessage().substring(1));
        }
    }

    public final PubSub getPubSub() {
        return pubSub;
    }

    public final Changelog getChangelog() {
        return changelog;
    }

    public final ModuleManager getModuleManager() {
        return moduleManager;
    }

    public final CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public final FontManager getFontManager() {
        return fontManager;
    }

    public final ClickGUI getClickGUI() {
        return clickGUI;
    }
    public ClickGui getClickGui() {
        return clickGui;
    }


    public final void fixClickGui() {
        clickGUI = new ClickGUI();
        MC.displayGuiScreen(null);
        MC.displayGuiScreen(clickGUI);
    }

    public final GuiAltManager getAltManager() {
        return altManager;
    }

    public final String getName() {
        return "Mega";
    }

    public final String getVersion() {
        return "1.6.0";

    }

    public final String getDev() {
        return "Exeos, LCA_MODZ, Felix1337, Kroko, Ice_B3ar";
    }

    public final String getUserName() {
        return username;
    }

}
