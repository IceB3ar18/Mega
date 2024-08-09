package club.mega.module.impl.hud;

import club.mega.event.impl.EventBlur;
import club.mega.event.impl.EventShader;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.util.RenderUtil;
import club.mega.util.blur.BloomUtil;
import club.mega.util.blur.GaussianBlur;
import club.mega.util.blur.KawaseBlur;
import club.mega.util.shader.StencilUtil;
import net.minecraft.client.shader.Framebuffer;

@Module.ModuleInfo(name = "Blur", description = "Blurs stuff", category = Category.HUD)
public class Blur extends Module {

    private final BooleanSetting blur = new BooleanSetting("Blur", this, true);
    private final ListSetting blurMode = new ListSetting("Mode", this, new String[] {"Kawase", "Gaussian"}, blur::get);
    private final NumberSetting radius = new NumberSetting("Blur Radius", this, 1, 50, 10, 1, () -> blurMode.is("Kawase"));
    private final NumberSetting iterations = new NumberSetting("Blur Iterations", this, 1, 15, 4, 1, () -> blurMode.is("Kawase"));
    private final NumberSetting offset = new NumberSetting("Blur Offset", this, 1, 20, 3, 1, () -> blurMode.is("Kawase"));
    private final BooleanSetting shadow = new BooleanSetting("Shadow", this, true);
    private final NumberSetting shadowRadius = new NumberSetting("Shadow Radius", this, 1, 20, 6, 1, shadow::get);
    private final NumberSetting shadowOffset = new NumberSetting("Shadow Offset", this, 1, 15, 2, 1, shadow::get);

    private String currentMode;
    private Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);

    @Override
    public void onEnable() {
        currentMode = blurMode.getCurrent();
        super.onEnable();
    }

    public void stuffToBlur(boolean bloom) {
        EventBlur eventBlur = new EventBlur();
        eventBlur.fire();
    }



    public void blurScreen() {
        if (!isToggled()) return;
        if (!currentMode.equals(blurMode.getCurrent())) {
            currentMode = blurMode.getCurrent();
        }
        if (blur.get()) {
            StencilUtil.initStencilToWrite();
            EventShader eventShader = new EventShader(false);
            eventShader.fire();

            stuffToBlur(false);
            StencilUtil.readStencilBuffer(1);

            switch (currentMode) {
                case "Gaussian":
                    GaussianBlur.renderBlur(radius.getAsFloat());
                    break;
                case "Kawase":
                    KawaseBlur.renderBlur(iterations.getAsInt(), offset.getAsInt());
                    break;
            }
            StencilUtil.uninitStencilBuffer();
        }



        if (shadow.get()) {
            bloomFramebuffer = RenderUtil.createFrameBuffer(bloomFramebuffer);

            bloomFramebuffer.framebufferClear();
            bloomFramebuffer.bindFramebuffer(true);
            EventShader eventShader = new EventShader(true);
            eventShader.fire();
            stuffToBlur(true);
            bloomFramebuffer.unbindFramebuffer();

            BloomUtil.renderBlur(bloomFramebuffer.framebufferTexture, shadowRadius.getAsInt(), shadowOffset.getAsInt());
        }


    }

}
