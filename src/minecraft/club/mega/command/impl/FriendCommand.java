package club.mega.command.impl;

import club.mega.Mega;
import club.mega.command.Command;
import club.mega.file.FriendSaver;
import club.mega.util.ChatUtil;
import club.mega.util.FriendUtil;

import java.util.Arrays;

@Command.Info(name = "Friend", description = "Adds a friend", usage = ".friend add", aliases = {"friend", "f"})
public final class FriendCommand extends Command {

    @Override
    public void execute(final String[] args) {
        switch (args[0].toLowerCase()) {
            case "list":
                if (FriendUtil.getFriends().isEmpty()) {
                    ChatUtil.sendMessage("§5No friends added yet.");
                } else {
                    for (int i = 0; i < FriendUtil.getFriends().size(); i++) {
                        ChatUtil.sendMessage("§5" + FriendUtil.getFriends().get(i).toString());
                    }
                }
                break;
            case "add":
                if (args.length != 2) {
                    ChatUtil.sendMessage("Please use: §e.friend <add | remove> <Player> §7| §e.friend list");
                    return;
                }
                if (!FriendUtil.getFriends().contains(args[1])) {
                    FriendUtil.addFriend(args[1]);
                    ChatUtil.sendMessage("Successfully added §a" + args[1]);
                    FriendSaver.save();
                } else {
                    ChatUtil.sendMessage("Player already added.");
                }
                break;
            case "remove":
                if (args.length != 2) {
                    ChatUtil.sendMessage("Please use: §e.friend <add | remove> <Player> §7| §e.friend <list>");
                    return;
                }
                if (FriendUtil.getFriends().contains(args[1])) {
                    FriendUtil.removeFriend(args[1]);
                    ChatUtil.sendMessage("Successfully removed §a" + args[1]);
                    FriendSaver.save();
                } else {
                    ChatUtil.sendMessage("Player not added");
                }
                break;
        }
    }


}
