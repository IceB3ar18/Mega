package club.mega.util.glsl;

import club.mega.Mega;
import club.mega.module.impl.player.Scaffold;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BGShaderUtil {
    private GLSLSandboxShader backgroundShader;
    private long initTime = System.currentTimeMillis();
    public String currentShader = "polar.fsh";
    public String getCurrentShader() {
        return currentShader;
    }

    public void setCurrentShader(String currentShader) {
        this.currentShader = currentShader;
    }

    private static BGShaderUtil instance = new BGShaderUtil();
    public static List<String> getFileNamesWithoutExtension() {
        List<String> fileNames = new ArrayList<>();
        fileNames.add("polar");
        fileNames.add("energy");
        fileNames.add("fire");
        fileNames.add("green");
        fileNames.add("grey");
        fileNames.add("purple");
        fileNames.add("sea");

        return fileNames;
    }

    public void setup() {
        try {
            this.backgroundShader = new GLSLSandboxShader("/backgrounds/" + BGShaderUtil.getInstance().getCurrentShader());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load backgound shader", e);
        }
    }

    public void render(int width, int height, int mouseX, int mouseY) {
        GlStateManager.disableCull();
        backgroundShader.useShader(width * 2, height * 2, mouseX, mouseY, (System.currentTimeMillis() - initTime) / 1000f);
        renderFullscreenQuad();
        GL20.glUseProgram(0);
    }

    private void renderFullscreenQuad() {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1f, -1f);
        GL11.glVertex2f(-1f, 1f);
        GL11.glVertex2f(1f, 1f);
        GL11.glVertex2f(1f, -1f);
        GL11.glEnd();
    }
    private BGShaderUtil() {}

    public static BGShaderUtil getInstance() {
        return instance;
    }
}
