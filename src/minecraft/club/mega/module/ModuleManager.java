package club.mega.module;

import club.mega.Mega;
import club.mega.module.impl.combat.*;
import club.mega.module.impl.combat.TargetESP;
import club.mega.module.impl.hud.*;
import club.mega.module.impl.misc.*;
import club.mega.module.impl.movement.*;
import club.mega.module.impl.movement.Timer;
import club.mega.module.impl.player.*;
import club.mega.module.impl.visual.*;

import java.util.*;

public class ModuleManager {

    private final ArrayList<Module> modules = new ArrayList<>();
    private final ArrayList<Module> mlModules = new ArrayList<>();

    public ModuleManager() {
        add(
                // Combat
                new KillAura(),
                new Criticals(),
                new Velocity(),
                new BackTrack(),
                new STap(),
                new WTap(),
                new TickBase(),
                new AutoGApple(),
                new AutoRod(),
                new AutoClicker(),
                new AntiBot(),
                new RotationHandler(),
                // Player
                new NoFall(),
                new NoRotate(),
                new AutoRespawn(),
                new Scaffold(),
                new Disabler(),
                new Range(),
                new BetterHits(),
                new ChestStealer(),
                new AutoArmor(),
                new InvCleaner(),
                new FastPlace(),
                new BedFucker(),
                new MCF(),
                new Regen(),
                new ChestAura(),
                // Movement
                new Sprint(),
                new Speed(),
                new Flight(),
                new Step(),
                new InvMove(),
                new NoSlow(),
                new NoJumpDelay(),
                new Timer(),
                new AntiVoid(),
                new VClip(),
                new AutoTool(),
                new Strafe(),
                // Visual
                new ESP(),
                new ItemESP(),
                new ChestESP(),
                new NoFov(),
                new NoHurtCam(),
                new Ambiance(),
                new Animations(),
                new NoBob(),
                new NameProtect(),
                new TargetESP(),
                new Scoreboard(),
                new Hotbar(),
                new Nametags(),
                new TNTRange(),
                new BlockOverlay(),
                new Trajectories(),
                new CustomSkin(),
                new Chat(),
                new AttackEffects(),
                new ItemPhysics(),
                new Projectiles(),
                new Blur(),
                new Rotation(),
                new CustomItemPos(),
                //Misc
                new Friends(),
                new AntiCheat(),
                new Discord(),
                new AutoRegister(),
                new StaffDetector(),
                // Hud
                new WaterMark(),
                new ModuleList(),
                new ClickGui(),
                new TargetHud(),
                new Shadow(),
                new Sounds()
        );
        mlModules.addAll(modules);
    }

    private void add(final Module... modules) {
        this.modules.addAll(Arrays.asList(modules));
    }

    public final ArrayList<Module> getModules() {
        return modules;
    }
    public final ArrayList<Module> getModules(final Category category) {
        ArrayList<Module> modules = new ArrayList<>();
        for (Module module : this.modules) {
            if (module.getCategory() == category)
                modules.add(module);
        }
        return modules;
    }

    public final Module getModule(final String name) {
        return modules.stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public final <T extends Module> T getModule(final Class<T> tClass) {
        List<Module> modulesCopy = new ArrayList<>(modules);
        return (T) modulesCopy.stream().filter(module -> module.getClass().equals(tClass)).findFirst().orElse(null);
    }

    public final boolean isToggled(final Class tClass) {
        return getModule(tClass).isToggled();
    }


}
