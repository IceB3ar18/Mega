package club.mega.command.impl;

import club.mega.Mega;
import club.mega.command.Command;
import club.mega.util.ChatUtil;
import club.mega.util.ConfigUtil;
import com.mojang.realmsclient.gui.ChatFormatting;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

@Command.Info(name = "Config", description = "Save / Load / Create / List / Delete Configs", usage = ".config (save / load / create / list / delete) <name>", aliases = {"config", "c"})
public class ConfigCommand extends Command {

    @Override
    public void execute(final String[] args) {
        switch (args[0].toLowerCase()) {
            case "save":
                ConfigUtil.save(args[1]);
                ChatUtil.sendMessage("Config saved " + ChatFormatting.WHITE + args[1]);
                break;
            case "load":
                if(ConfigUtil.configExists(args[1])) {
                    ConfigUtil.load(args[1]);

                    ChatUtil.sendMessage("Config " + ChatFormatting.WHITE + args[1] + ChatFormatting.GRAY + " was loaded.");
                } else {
                    ChatUtil.sendMessage("Config " + ChatFormatting.WHITE + args[1] + ChatFormatting.GRAY + " doesn't exist!");
                }
                break;

            case "create":
                if (!new File(MC.mcDataDir, "Async/Configs/ " + args[1] + ".json").exists()) {
                    ConfigUtil.save(args[1]);
                    ChatUtil.sendMessage("Config created " + ChatFormatting.WHITE + args[1]);
                } else
                    ChatUtil.sendMessage("Config " + ChatFormatting.WHITE + args[1] + ChatFormatting.GRAY + " is already existing");
                break;
            case "list":
                final File[] listOfFiles = new File(MC.mcDataDir, "/Mega/Configs").listFiles();

                for (int i = 0; i < Objects.requireNonNull(listOfFiles).length; i++) {
                    long lastModified = listOfFiles[i].lastModified();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    String formattedDate = sdf.format(new Date(lastModified));

                    ChatUtil.sendMessage(ChatFormatting.WHITE + listOfFiles[i].getName().replace(".json", "") + ChatFormatting.GRAY + " " + formattedDate);
                }
                break;

            case "delete":
                final File file = new File(MC.mcDataDir, "Mega/Configs/" + args[1] + ".json");
                if (file.exists()) {
                    file.delete();
                    ChatUtil.sendMessage("Config " + ChatFormatting.WHITE + args[1] + ChatFormatting.GRAY + " was deleted.");
                } else
                    ChatUtil.sendMessage("Config " + ChatFormatting.WHITE + args[1] + ChatFormatting.GRAY + " doesn't exist!");

                break;
        }
    }
}
