package club.mega.file;

import club.mega.Mega;
import club.mega.interfaces.MinecraftInterface;
import club.mega.module.Module;
import club.mega.module.setting.Setting;
import club.mega.module.setting.impl.BooleanSetting;
import club.mega.module.setting.impl.ListSetting;
import club.mega.module.setting.impl.NumberSetting;
import club.mega.module.setting.impl.TextSetting;
import club.mega.util.FriendUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public final class FriendSaver implements MinecraftInterface {

    public static void save() {
        final File file = new File(MC.mcDataDir, Mega.INSTANCE.getName() + "/Friends.json");

        try {
            if (file.exists())
                file.delete();
            if (new File(MC.mcDataDir, Mega.INSTANCE.getName() + "/").mkdir() || !file.exists())
                file.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            Iterator iterator = FriendUtil.getFriends().iterator();

            while (iterator.hasNext()) {
                String name = (String) iterator.next();
                writer.write(name + ":" + FriendUtil.getFriends());
                writer.newLine();
            }

            writer.close();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

}
