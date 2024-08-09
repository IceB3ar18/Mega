package club.mega.file;

import club.mega.Mega;
import club.mega.interfaces.MinecraftInterface;
import club.mega.module.Module;
import club.mega.module.setting.Setting;
import club.mega.module.setting.impl.*;
import club.mega.util.ColorUtil;
import club.mega.util.glsl.BGShaderUtil;
import com.google.gson.*;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public final class ModuleSaver implements MinecraftInterface {

    public static void saveClientState() {
        final File file = new File("Mega/clientState.json");

        try {
            if (file.exists())
                file.delete();
            file.createNewFile();

            final JsonObject stateObject = new JsonObject();

            // Speichern des Hintergrunds
            stateObject.addProperty("background", BGShaderUtil.getInstance().getCurrentShader());

            // Speichern der Hauptfarbe
            stateObject.addProperty("mainColor", colorToHex(ColorUtil.getMainColor()));

            final Gson gson = new GsonBuilder().setPrettyPrinting().create();

            final FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            gson.toJson(stateObject, fileWriter);
            fileWriter.close();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public static void loadClientState() {
        final File file = new File("Mega/clientState.json");

        try {
            if (!file.exists())
                return;

            FileReader reader = new FileReader(file.getAbsoluteFile());
            JsonObject stateObject = new JsonParser().parse(reader).getAsJsonObject();

            reader.close();

            // Laden des Hintergrunds
            if (stateObject.has("background")) {
                String background = stateObject.get("background").getAsString();
                BGShaderUtil.getInstance().setCurrentShader(background);
                BGShaderUtil.getInstance().setup();
            }

            // Laden der Hauptfarbe
            if (stateObject.has("mainColor")) {
                String mainColor = stateObject.get("mainColor").getAsString();
                ColorUtil.setMainColor(hexToColor(mainColor));
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public static void save() {
        final File file = new File(MC.mcDataDir, Mega.INSTANCE.getName() + "/Modules.json");

        try {
            if (file.exists())
                file.delete();
            if (new File(MC.mcDataDir, Mega.INSTANCE.getName() + "/").mkdir() || !file.exists())
                file.createNewFile();

            final JsonObject mainObject = new JsonObject();

            for (final Module module : Mega.INSTANCE.getModuleManager().getModules()) {
                final JsonObject moduleObject = new JsonObject();
                final JsonObject settingObject = new JsonObject();

                for (final Setting setting : module.getSettings()) {
                    if (setting instanceof TextSetting)
                        settingObject.addProperty(setting.getName(), ((TextSetting) setting).getRawText());
                    if (setting instanceof ListSetting)
                        settingObject.addProperty(setting.getName(), ((ListSetting) setting).getCurrent());
                    if (setting instanceof BooleanSetting)
                        settingObject.addProperty(setting.getName(), ((BooleanSetting) setting).get());
                    if (setting instanceof ColorSetting)
                        settingObject.addProperty(setting.getName(), colorToHex(((ColorSetting) setting).getColor().getAlpha(), ((ColorSetting) setting).getColor().getRed(), ((ColorSetting) setting).getColor().getGreen(), ((ColorSetting) setting).getColor().getBlue()));
                    if (setting instanceof NumberSetting)
                        settingObject.addProperty(setting.getName(), ((NumberSetting) setting).getAsDouble());
                    if (setting instanceof RangeSetting) {
                        JsonObject rangeObject = new JsonObject();
                        rangeObject.addProperty("min", ((RangeSetting) setting).getCurrentMin());
                        rangeObject.addProperty("max", ((RangeSetting) setting).getCurrentMax());
                        settingObject.add(setting.getName(), rangeObject);
                    }
                }
                moduleObject.addProperty("Toggled", module.isToggled());
                moduleObject.addProperty("Key", module.getKey());
                moduleObject.add("Setting", settingObject);
                mainObject.add(module.getName(), moduleObject);
            }
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();

            final FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            gson.toJson(mainObject, fileWriter);
            fileWriter.close();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public static void load() {
        final File file = new File(MC.mcDataDir, Mega.INSTANCE.getName() + "/Modules.json");

        try {
            if (!file.exists()) {
                return;
            }

            FileReader reader = new FileReader(file.getAbsoluteFile());
            JsonObject object = new JsonParser().parse(reader).getAsJsonObject();
            reader.close();

            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                Module module = Mega.INSTANCE.getModuleManager().getModule(entry.getKey());
                if (module != null) {
                    final JsonObject jsonModule = (JsonObject) entry.getValue();
                    if (jsonModule.has("Toggled")) {
                        module.setToggled(jsonModule.get("Toggled").getAsBoolean());
                    }
                    if (jsonModule.has("Key")) {
                        module.setKey(jsonModule.get("Key").getAsInt());
                    }

                    JsonObject settingObject = jsonModule.getAsJsonObject("Setting");
                    if (settingObject != null) {
                        for (Map.Entry<String, JsonElement> settingEntry : settingObject.entrySet()) {
                            Setting s = module.getSetting(settingEntry.getKey());
                            if (s != null) {
                                try {
                                    if (s instanceof TextSetting) {
                                        ((TextSetting) s).setText(settingEntry.getValue().getAsString());
                                    } else if (s instanceof ListSetting) {
                                        ((ListSetting) s).setCurrent(settingEntry.getValue().getAsString());
                                    } else if (s instanceof BooleanSetting) {
                                        ((BooleanSetting) s).set(settingEntry.getValue().getAsBoolean());
                                    } else if (s instanceof ColorSetting) {
                                        ((ColorSetting) s).setColor(hexToColor(settingEntry.getValue().getAsString()));
                                    } else if (s instanceof NumberSetting) {
                                        ((NumberSetting) s).setCurrent(settingEntry.getValue().getAsDouble());
                                    } else if (s instanceof RangeSetting) {
                                        JsonObject rangeObject = settingEntry.getValue().getAsJsonObject();
                                        if (rangeObject.has("min") && rangeObject.has("max")) {
                                            ((RangeSetting) s).setCurrentMin(rangeObject.get("min").getAsDouble());
                                            ((RangeSetting) s).setCurrentMax(rangeObject.get("max").getAsDouble());
                                        }
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error loading setting '" + settingEntry.getKey() + "' for module '" + module.getName() + "': " + e.getMessage());
                                }
                            } else {
                                System.err.println("Setting '" + settingEntry.getKey() + "' not found for module '" + module.getName() + "'");
                            }
                        }
                    }
                } else {
                    System.err.println("Module '" + entry.getKey() + "' not found.");
                }
            }
        } catch (IOException | JsonParseException exception) {
            System.err.println("Error loading modules: " + exception.getMessage());
        }
    }

    public static Color hexToColor(String hex) {
        try {
            int alpha = Integer.parseInt(hex.substring(1, 3), 16);
            int red = Integer.parseInt(hex.substring(3, 5), 16);
            int green = Integer.parseInt(hex.substring(5, 7), 16);
            int blue = Integer.parseInt(hex.substring(7, 9), 16);
            return new Color(red, green, blue, alpha);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.err.println("Invalid color format: " + hex);
            return Color.BLACK;  // Default fallback color
        }
    }

    public static String colorToHex(int alpha, int red, int green, int blue) {
        return String.format("#%02X%02X%02X%02X", alpha, red, green, blue);
    }

    public static String colorToHex(Color color) {
        return String.format("#%02X%02X%02X%02X", color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
    }

}
