package club.mega.gui.click;

import club.mega.Mega;
import club.mega.gui.widgets.ConfigButton;
import club.mega.util.ChatUtil;
import club.mega.util.ConfigUtil;
import club.mega.util.GuiBlurUtil;
import club.mega.util.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

public class ConfigGui extends GuiScreen {
    private GuiTextField createTextField;
    private final GuiScreen parent;
    private ConfigButton selected;
    private ArrayList<ConfigButton> configButtons = new ArrayList();
    private ArrayList<ConfigButton> onlineConfigButtons = new ArrayList<>();

    public ConfigGui(GuiScreen parent) {
        this.parent = parent;
    }

    public void initGui() {
        super.initGui();
        ScaledResolution sr = new ScaledResolution(this.mc);

        this.createTextField = new GuiTextField(1, this.fontRendererObj, sr.getScaledWidth() / 2 - 150 + 5, sr.getScaledHeight() / 8 * 6 + sr.getScaledHeight() / 8 - 20 - 5, 150, 20);
        this.createTextField.setMaxStringLength(1377);
        int rectX = sr.getScaledWidth() / 2 - 150;
        int rectY = sr.getScaledHeight() / 8;
        int rectWidth = 300;
        int rectHeight = sr.getScaledHeight() / 8 * 6;

        // Buttons hinzufügen
        int buttonWidth = 90;
        int buttonHeight = 20;
        int buttonMargin = 5; // Abstand zwischen den Buttons und dem Rechteck

        int buttonY = rectY + rectHeight / 2 - buttonHeight / 2; // Mittelpunkt der Höhe des Rechtecks für die Y-Koordinate der Buttons

        int buttonX = rectX + rectWidth - 5 - buttonWidth; // Start X-Koordinate für die Buttons

        this.buttonList.add(new GuiButton(2, buttonX, buttonY - 25 * 3, buttonWidth, buttonHeight, "Create"));
        this.buttonList.add(new GuiButton(3, buttonX, buttonY - 25 * 2, buttonWidth, buttonHeight, "Load"));
        this.buttonList.add(new GuiButton(4, buttonX, buttonY - 25 * 1, buttonWidth, buttonHeight, "Save"));
        this.buttonList.add(new GuiButton(5, buttonX, buttonY, buttonWidth, buttonHeight, "Delete"));
        this.initConfigs();
        this.initOnlineConfigs();
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 2) {
            this.createConfig();
        } else if (button.id == 3) {
            this.loadConfig();
        } else if (button.id == 4) {
            this.saveConfigs();
        } else if (button.id == 5) {
            this.deleteConfigs();
        }

        super.actionPerformed(button);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(this.mc);
        int rectX = sr.getScaledWidth() / 2 - 150; // Ursprüngliche X-Koordinate des Rechtecks
        int rectY = sr.getScaledHeight() / 8; // Ursprüngliche Y-Koordinate des Rechtecks
        int rectWidth = 300; // Ursprüngliche Breite des Rechtecks
        int rectHeight = sr.getScaledHeight() / 8 * 6; // Ursprüngliche Höhe des Rechtecks

        // Neuberechnung der Koordinaten für das linke Drittel
        rectWidth /= 3;
        int leftThirdX = rectX;
        int leftThirdY = rectY;
        int leftThirdWidth = rectWidth;
        int leftThirdHeight = rectHeight;
        GuiBlurUtil.drawBlurredRect(sr.getScaledWidth() / 2 - 150, sr.getScaledHeight() / 8, 300, sr.getScaledHeight() / 8 * 6, 5, new Color(20, 20, 20, 180));
        RenderUtil.drawPartialRoundedRect(sr.getScaledWidth() / 2 - 150, sr.getScaledHeight() / 8, 300, Mega.INSTANCE.getFontManager().getFont("Arial 20").getHeight("Configs") + 3, 6, new Color(20, 20, 20, 144));

        // Rendern der Kopfzeile "Configs" in der linken Spalte
        Mega.INSTANCE.getFontManager().getFont("Roboto medium 25").drawStringWithShadow("Configs", rectX + 3, rectY + 2, Color.white.getRGB());
        // Berechnung der Y-Koordinate für die Unterüberschrift "Local Configs"
        int headerY = (int) (leftThirdY + Mega.INSTANCE.getFontManager().getFont("Arial 20").getHeight("Configs") + 2) + 7;

        // Rendern der Unterüberschrift "Local Configs" in der linken Spalte

        Mega.INSTANCE.getFontManager().getFont("Arial 20").drawCenteredString("Local Configs", leftThirdX + leftThirdWidth / 2, headerY, new Color(220, 220, 220));
        Mega.INSTANCE.getFontManager().getFont("Arial 20").drawCenteredString("Online Configs", sr.getScaledWidth() / 2, headerY, new Color(220, 220, 220));
        // Start Y-Koordinate der Buttons unterhalb der Unterüberschrift "Local Configs"
        int buttonY = headerY + 25;
        for (ConfigButton onlineConfigButton : this.onlineConfigButtons) {
            onlineConfigButton.xPosition = sr.getScaledWidth() / 2 - onlineConfigButton.width / 2;
            onlineConfigButton.yPosition = buttonY;
            onlineConfigButton.draw(this.mc, mouseX, mouseY);
            buttonY += 15;
        }
        buttonY = headerY + 25;
        // Rendern der Config-Buttons in der linken Spalte
        for (ConfigButton configButton : this.configButtons) {
            configButton.xPosition = leftThirdX + (leftThirdWidth - configButton.width) / 2; // Zentriert in der linken Spalte
            configButton.yPosition = buttonY; // Setze die Y-Koordinate des Buttons
            configButton.draw(this.mc, mouseX, mouseY);
            buttonY += 15; // Verschiebe die Y-Koordinate für den nächsten Button
        }


        super.drawScreen(mouseX, mouseY, partialTicks);
        this.createTextField.drawTextBox();
        GlStateManager.pushMatrix();
        GlStateManager.popMatrix();

    }



    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.createTextField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == 1) {
            this.mc.displayGuiScreen(this.parent);
        }

    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.createTextField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (ConfigButton configButton : this.configButtons) {
            if (this.mouseOver((double) mouseX, (double) mouseY, (double) configButton.xPosition, (double) configButton.yPosition, (double) configButton.getButtonWidth(), (double) configButton.getButtonHeight())) {
                this.selected = configButton;
                configButton.setSelected(true);
            } else {
                configButton.setSelected(false);
            }
        }

        for (ConfigButton onlineConfigButton : this.onlineConfigButtons) {
            if (this.mouseOver((double) mouseX, (double) mouseY, (double) onlineConfigButton.xPosition, (double) onlineConfigButton.yPosition, (double) onlineConfigButton.getButtonWidth(), (double) onlineConfigButton.getButtonHeight())) {
                this.selected = onlineConfigButton;
                onlineConfigButton.setSelected(true);
            } else {
                onlineConfigButton.setSelected(false);
            }
        }

    }

    public void updateScreen() {
        this.createTextField.updateCursorCounter();
        super.updateScreen();
    }

    private void deleteConfigs() {
        if (this.selected != null) {
            final File file = new File(this.mc.mcDataDir, "Mega/Configs/" + this.selected.displayString + ".json");
            if (file.exists()) {
                file.delete();
                ChatUtil.sendMessage("Config " + ChatFormatting.WHITE + this.selected.displayString + ChatFormatting.GRAY + " was deleted.");
            } else
                ChatUtil.sendMessage("Config " + ChatFormatting.WHITE + this.selected.displayString + ChatFormatting.GRAY + " doesn't exist!");
        }

        this.initConfigs();
    }

    private void createConfig() {
        if (!this.createTextField.getText().equals("")) {
            if (!new File(Minecraft.getMinecraft().mcDataDir, "Async/Configs/ " + this.createTextField.getText() + ".json").exists()) {
                ConfigUtil.save(this.createTextField.getText());
                ChatUtil.sendMessage("Config created " + ChatFormatting.WHITE + this.createTextField.getText());
            } else
                ChatUtil.sendMessage("Config " + ChatFormatting.WHITE + this.createTextField.getText() + ChatFormatting.GRAY + " is already existing");
            this.createTextField.setText("");
        }

        this.initConfigs();
    }

    private void saveConfigs() {
        if (this.selected != null) {
            ConfigUtil.save(this.selected.displayString);
            ChatUtil.sendMessage("Config saved " + ChatFormatting.WHITE + this.selected.displayString);
        }

    }

    private void loadConfig() {
        if (selected != null) {
            String configName = selected.displayString;
            System.out.println(selected.id);
            if (selected.id == -1) {

                ConfigUtil.loadOnlineConfig(configName);
                ChatUtil.sendMessage("Config " + ChatFormatting.WHITE + this.selected.displayString + ChatFormatting.GRAY + " was loaded.");

                // Laden einer Online-Konfiguration

            } else {
                if (ConfigUtil.configExists(this.selected.displayString)) {
                    ConfigUtil.load(this.selected.displayString);

                    ChatUtil.sendMessage("Config " + ChatFormatting.WHITE + this.selected.displayString + ChatFormatting.GRAY + " was loaded.");
                } else {
                    ChatUtil.sendMessage("Config " + ChatFormatting.WHITE + this.selected.displayString + ChatFormatting.GRAY + " doesn't exist!");
                }
            }
        }

    }

    private void initConfigs() {

        this.configButtons = new ArrayList();
        final File[] listOfFiles = new File(Minecraft.getMinecraft().mcDataDir, "/Mega/Configs").listFiles();
        int yAdd = 0;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        for (int i = 0; i < Objects.requireNonNull(listOfFiles).length; i++) {
            long lastModified = listOfFiles[i].lastModified();

// Datumsformatierung
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String formattedDate = dateFormat.format(new Date(lastModified));

// Uhrzeitformatierung
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String formattedTime = timeFormat.format(new Date(lastModified));
            this.configButtons.add(new ConfigButton(6, sr.getScaledWidth() / 2 - 140, 55 + yAdd, 90, 18, listOfFiles[i].getName().replace(".json", ""), formattedDate, formattedTime, Color.GRAY));
            yAdd += 18;

            //ChatUtil.sendMessage(ChatFormatting.WHITE + listOfFiles[i].getName().replace(".json", "") + ChatFormatting.GRAY + " " + formattedDate);
        }


    }

    private void initOnlineConfigs() {
        this.onlineConfigButtons.clear();
        ScaledResolution sr = new ScaledResolution(this.mc);
        try {
            String s = new Scanner(new URL("https://api.github.com/repos/IceB3ar18/KrypticLauncher/contents/configs").openStream(), "UTF-8").useDelimiter("\\A").next();
            JSONArray array = new JSONArray(s);

            int yAdd = 0;
            ArrayList<String> configs = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                String name = object.getString("name");
                if (name.endsWith(".json")) {
                    String configName = name.substring(0, name.lastIndexOf('.'));
                    onlineConfigButtons.add(new ConfigButton(-1, sr.getScaledWidth() / 2 - 140, 55 + yAdd, 90, 18, configName, "Online", "N/A", Color.GRAY));
                    yAdd += 18;
                    System.out.println("Gefundene Konfiguration: " + configName);
                }
            }

        } catch (JSONException | MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean mouseOver(double mouseX, double mouseY, double posX, double posY, double width, double height) {
        return mouseX >= posX && mouseX <= posX + width && mouseY >= posY && mouseY <= posY + height;
    }
}
