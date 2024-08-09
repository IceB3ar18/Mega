package club.mega.module.impl.visual;

import club.mega.Mega;
import club.mega.event.impl.EventEarlyTick;
import club.mega.module.Category;
import club.mega.module.Module;
import club.mega.module.setting.impl.TextSetting;
import com.mojang.util.UUIDTypeAdapter;
import rip.hippo.lwjeb.annotation.Handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@Module.ModuleInfo(name = "CustomSkin", description = "Changes your skin", category = Category.VISUAL)
public class CustomSkin extends Module {
    public final TextSetting name = new TextSetting("Name", this, "G0mme");
    private String currentName, prevName;
    public UUID uuid;
    @Handler
    public final void onEarly(EventEarlyTick eventEarlyTick) {
        prevName = currentName;
        currentName = name.getText();
        if(prevName.equalsIgnoreCase(currentName)) {
            uuid = getUUIDFromUsername(name.getText());
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    private UUID getUUIDFromUsername(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String jsonString = response.toString();
            if (!jsonString.isEmpty()) {
                return UUIDTypeAdapter.fromString(jsonString.split("\"id\":\"")[1].split("\"")[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    public void onEnable() {
        super.onEnable();
        prevName = name.getText();
        uuid = getUUIDFromUsername(name.getText());
    }
}
