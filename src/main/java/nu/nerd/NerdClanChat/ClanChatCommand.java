package nu.nerd.NerdClanChat;

import nu.nerd.NerdClanChat.database.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;


public class ClanChatCommand implements CommandExecutor {


    private final NerdClanChat plugin;
    private HashMap<String, String> confirmChannelDeletion;
    private enum ChannelColor { COLOR, TEXTCOLOR, ALERTCOLOR }


    public ClanChatCommand(NerdClanChat plugin) {
        this.plugin = plugin;
        this.confirmChannelDeletion = new HashMap<String, String>();
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length < 1) {
            this.printHelpText(sender);
        }

        else if (args[0].equalsIgnoreCase("more")) {
            this.printMoreHelpText(sender);
        }

        else if (args[0].equalsIgnoreCase("create")) {
            if (args.length > 1) {
                this.createChannel(sender, args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat create <channel>");
            }
        }

        else if (args[0].equalsIgnoreCase("delete")) {
            if (args.length > 1) {
                this.deleteChannel(sender, args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat delete <channel>");
            }
        }

        else if (args[0].equalsIgnoreCase("confirm") && args.length == 2) {
            if (args[1].equalsIgnoreCase("delete")) {
                this.actuallyDeleteChannel(sender);
            }
        }

        else if (args[0].equalsIgnoreCase("color")) {
            if (args.length == 3) {
                this.setChannelColor(sender, args[1], args[2], ChannelColor.COLOR);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat color <channel> <color>");
                sender.sendMessage("Available colors: " + NCCUtil.formatColorList(NCCUtil.colorList()));
            }
        }

        else if (args[0].equalsIgnoreCase("textcolor")) {
            if (args.length == 3) {
                this.setChannelColor(sender, args[1], args[2], ChannelColor.TEXTCOLOR);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat textcolor <channel> <color>");
                sender.sendMessage("Available colors: " + NCCUtil.formatColorList(NCCUtil.colorList()));
            }
        }

        else if (args[0].equalsIgnoreCase("alertcolor")) {
            if (args.length == 3) {
                this.setChannelColor(sender, args[1], args[2], ChannelColor.ALERTCOLOR);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat alertcolor <channel> <color>");
                sender.sendMessage("Available colors: " + NCCUtil.formatColorList(NCCUtil.colorList()));
            }
        }

        else if (args[0].equalsIgnoreCase("members")) {
            if (args.length == 2) {
                this.listChannelMembers(sender, args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat members <channel>");
            }
        }

        else if (args[0].equalsIgnoreCase("invite")) {
            if (args.length == 3) {
                this.inviteMember(sender, args[1], args[2]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat invite <channel> <player>");
            }
        }

        else if (args[0].equalsIgnoreCase("uninvite")) {
            if (args.length == 3) {
                this.uninviteMember(sender, args[1], args[2]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat uninvite <channel> <player>");
            }
        }

        else if (args[0].equalsIgnoreCase("changeowner")) {
            if (args.length == 3) {
                this.changeChannelOwner(sender, args[1], args[2]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat changeowner <channel> <player>");
            }
        }

        else if (args[0].equalsIgnoreCase("addmanager")) {
            if (args.length == 3) {
                this.addChannelManager(sender, args[1], args[2]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat addmanager <channel> <player>");
            }
        }

        else if (args[0].equalsIgnoreCase("removemanager")) {
            if (args.length == 3) {
                this.removeChannelManager(sender, args[1], args[2]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat removemanager <channel> <player>");
            }
        }

        else if (args[0].equalsIgnoreCase("listmanagers")) {
            if (args.length == 2) {
                this.listChannelManagers(sender, args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat listmanagers <channel>");
            }
        }

        else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 3) {
                this.removeMemberFromChannel(sender, args[1], args[2]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat remove <channel> <player>");
            }
        }

        else if (args[0].equalsIgnoreCase("join")) {
            if (args.length == 2) {
                this.joinChannel(sender, args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat join <channel>");
            }
        }

        else if (args[0].equalsIgnoreCase("leave")) {
            if (args.length == 2) {
                this.leaveChannel(sender, args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat leave <channel>");
            }
        }

        else if (args[0].equalsIgnoreCase("list")) {
            this.listChannels(sender);
        }

        else if (args[0].equalsIgnoreCase("channels")) {
            this.listAllChannels(sender);
        }

        else if (args[0].equalsIgnoreCase("public")) {
            this.listAllPublicChannels(sender);
        }

        else if (args[0].equalsIgnoreCase("addbulletin")) {
            if (args.length > 2) {
                this.addBulletin(sender, args);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat addbulletin <channel> <bulletin>");
            }
        }

        else if (args[0].equalsIgnoreCase("removebulletin")) {
            if (args.length == 3) {
                this.removeBulletin(sender, args[1], args[2]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat removebulletin <channel> <number>");
                sender.sendMessage(ChatColor.RED + "The <number> field starts at 1 from the top bulletin.");
            }
        }

        else if (args[0].equalsIgnoreCase("subscribe")) {
            if (args.length == 2) {
                this.subscribeToBulletins(sender, args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat subscribe <channel>");
            }
        }

        else if (args[0].equalsIgnoreCase("unsubscribe")) {
            if (args.length == 2) {
                this.unsubscribeFromBulletins(sender, args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat unsubscribe <channel>");
            }
        }

        else if (args[0].equalsIgnoreCase("subscriptions")) {
            this.listSubscriptions(sender);
        }

        else if (args[0].equalsIgnoreCase("chat")) {
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "Sorry, you can't use this command");
                return true;
            }
            if (args.length > 2) {
                this.consoleChat(sender, args);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat chat <channel> <message>");
            }
        }

        else if (args[0].equalsIgnoreCase("flags")) {
            if (args.length == 4) {
                this.setFlags(sender, args);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat flags <channel> <flag> <boolean>");
                sender.sendMessage("Two flags are supported: 'public' and 'secret'.");
                sender.sendMessage("A 'public' channel can be joined by anyone, without invite.");
                sender.sendMessage("A 'secret' channel requires membership to view channel members.");
                sender.sendMessage("A boolean is either the word 'true' or 'false'.");
            }
        }

        else if (args[0].equalsIgnoreCase("reloadconfig")) {
            if (sender.hasPermission("nerdclanchat.admin")) {
                plugin.config.load();
                sender.sendMessage(ChatColor.BLUE + "Configuration reloaded.");
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            }
        }

        else {
            this.printHelpText(sender);
        }

        return true;

    }


    private void createChannel(CommandSender sender, String name) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return;
        }
        name = name.toLowerCase();
        Player owner = (Player) sender;
        if (plugin.channelCache.getChannel(name) != null) {
            sender.sendMessage(ChatColor.RED + "That channel already exists. If you would like to join the channel, speak with the owner.");
        } else {

            if (!name.matches("^(?i)[a-z0-9_]+$")) {
                sender.sendMessage(ChatColor.RED + "Oops! That channel name isn't valid. You can only have letters and numbers in your channel name.");
                return;
            }
            if (name.length() > 16) {
                sender.sendMessage(ChatColor.RED + "Oops! Please limit your channel name length to 16 characters!");
                return;
            }

            //Create the channel
            Channel ch = new Channel(name, owner.getUniqueId().toString());
            ChannelMember mem = new ChannelMember(name, owner.getUniqueId().toString(), owner.getName(), true);
            try {
                plugin.channelsTable.save(ch);
                plugin.channelMembersTable.save(mem);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "There was an error creating the channel.");
                plugin.getLogger().warning(ex.toString());
                return;
            }

            //Cache the newly created channel
            HashMap<String, ChannelMember> members = new HashMap<String, ChannelMember>();
            members.put(owner.getUniqueId().toString(), mem);
            plugin.channelCache.updateChannel(name, ch);
            plugin.channelCache.updateChannelMembers(name, members);

            //Update owner's player meta to make this their default channel
            String UUID = owner.getUniqueId().toString();
            PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);
            meta.setDefaultChannel(name);
            plugin.playerMetaCache.updatePlayerMeta(UUID, meta);

            sender.sendMessage(ChatColor.BLUE + "You will receive bulletins from this channel on login. To unsubscribe run /clanchat unsubscribe " + name);
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Channel created!");

        }
    }


    private void deleteChannel(CommandSender sender, String name) {

        if (!this.senderIsOwner(sender, name, false)) {
            sender.sendMessage(ChatColor.RED + "Only the owner can delete a channel");
            return;
        }

        Player owner = (Player) sender;
        this.confirmChannelDeletion.put(owner.getUniqueId().toString(), name.toLowerCase());
        sender.sendMessage(ChatColor.RED + "This command is irreversible. Your channel and all associated data WILL Be lost!");
        sender.sendMessage(ChatColor.BLUE + "Type \"/clanchat confirm delete\" to confirm you actually want to delete #" + name);

    }


    private void actuallyDeleteChannel(CommandSender sender) {
        if (sender instanceof Player) {

            Player owner = (Player) sender;
            String ownerUUID = owner.getUniqueId().toString();
            String channelName;

            if (!this.confirmChannelDeletion.containsKey(ownerUUID)) {
                return;
            } else {
                channelName = this.confirmChannelDeletion.get(ownerUUID);
                this.confirmChannelDeletion.remove(ownerUUID);
            }

            try {
                Channel ch = plugin.channelCache.getChannel(channelName);
                plugin.channelsTable.delete(ch);
                plugin.channelMembersTable.deleteChannelMembers(channelName);
                plugin.bulletinsTable.deleteChannelBulletins(channelName);
                plugin.invitesTable.deleteChannelInvites(channelName);
                plugin.channelCache.remove(channelName);
            } catch (Exception ex) {
                plugin.getLogger().warning(ex.toString());
                sender.sendMessage(ChatColor.RED + "There was an error deleting your channel.");
                return;
            }

            sender.sendMessage(ChatColor.RED + "Your channel was deleted!");

        }
    }


    private void setChannelColor(CommandSender sender, String channelName, String color, ChannelColor key) {

        if (!this.senderIsManager(sender, channelName, false)) {
            sender.sendMessage(ChatColor.RED + "Sorry, you have to be a manager to do that!");
            return;
        }

        channelName = channelName.toLowerCase();
        color = color.toUpperCase();
        Channel channel = plugin.channelCache.getChannel(channelName);

        if (channel == null) {
            sender.sendMessage(ChatColor.RED + String.format("The channel \"%s\" doesn't exist", channelName));
            return;
        }

        if (!NCCUtil.colorList().contains(color)) {
            sender.sendMessage(ChatColor.RED + "Only one of the following colors can be used:");
            sender.sendMessage(NCCUtil.formatColorList(NCCUtil.colorList()));
            return;
        }

        if (key == ChannelColor.ALERTCOLOR) {
            channel.setAlertColor(color);
        } else if (key == ChannelColor.TEXTCOLOR) {
            channel.setTextColor(color);
        } else {
            channel.setColor(color);
        }

        try {
            plugin.channelsTable.update(channel);
            plugin.channelCache.updateChannel(channelName, channel);
            String msg = String.format("Channel %s changed to %s%s!", key.name().toLowerCase(), ChatColor.valueOf(color), color);
            sender.sendMessage(ChatColor.BLUE + msg);
        } catch (Exception ex) {
            plugin.getLogger().warning(ex.toString());
            sender.sendMessage(ChatColor.RED + "There was an error updating the channel color.");
        }

    }


    private void listChannelMembers(CommandSender sender, String channelName) {

        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());

        if (channel == null) {
            sender.sendMessage(ChatColor.RED + "That channel does not exist");
            return;
        }

        if (!this.senderIsMember(sender, channelName) && channel.isSecret()) {
            sender.sendMessage(ChatColor.RED + "This channel is secret. You must be a member of the channel to see who is in the channel");
            return;
        }

        // Collect players into online and offline lists
        List<String> online = new ArrayList<String>();
        List<String> offline = new ArrayList<String>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (members.containsKey(player.getUniqueId().toString())) {
                online.add(player.getName());
            }
        }
        for (ChannelMember member : members.values()) {
            if (!online.contains(member.getName())) {
                offline.add(member.getName());
            }
        }

        // Output
        sender.sendMessage(ChatColor.GOLD + "Online: " + NCCUtil.formatList(online, ChatColor.WHITE, ChatColor.GRAY));
        sender.sendMessage(ChatColor.GOLD + "Offline: " + NCCUtil.formatList(offline, ChatColor.WHITE, ChatColor.GRAY));

    }


    private void inviteMember(CommandSender sender, String channelName, String playerName) {

        if (!this.senderIsManager(sender, channelName, false)) {
            sender.sendMessage(ChatColor.RED + "Sorry, you have to be a manager to do that!");
            return;
        }

        channelName = channelName.toLowerCase();
        PlayerMeta playerMeta = plugin.playerMetaCache.getPlayerMetaByName(playerName.toLowerCase());

        if (playerMeta == null) {
            sender.sendMessage(ChatColor.RED + "Sorry, but that player hasn't logged on recently. Try again later.");
            return;
        }

        if (plugin.invitesTable.alreadyInvited(playerMeta.getUUID(), channelName)) {
            sender.sendMessage(ChatColor.BLUE + "That player was already invited, but they haven't accepted yet");
            return;
        }

        try {
            Invite inv = new Invite(channelName, playerMeta.getUUID());
            plugin.invitesTable.save(inv);
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "There was an error processing the invite.");
            plugin.getLogger().warning(ex.toString());
            return;
        }

        Player player = plugin.getServer().getPlayer(UUID.fromString(playerMeta.getUUID()));
        if (player != null) {
            player.sendMessage(ChatColor.BLUE + String.format("You have been invited to %s by %s.", channelName, sender.getName()));
            player.sendMessage(ChatColor.BLUE + String.format("Type %s/clanchat join %s%s to join", ChatColor.GRAY, channelName, ChatColor.BLUE));
        }

        sender.sendMessage(ChatColor.BLUE + String.format("%s has been invited to %s", playerName, channelName));

    }


    private void uninviteMember(CommandSender sender, String channelName, String playerName) {

        if (!this.senderIsManager(sender, channelName, false)) {
            sender.sendMessage(ChatColor.RED + "Sorry, you have to be a manager to do that!");
            return;
        }

        channelName = channelName.toLowerCase();
        PlayerMeta playerMeta = plugin.playerMetaCache.getPlayerMetaByName(playerName.toLowerCase());

        if (!plugin.invitesTable.alreadyInvited(playerMeta.getUUID(), channelName)) {
            sender.sendMessage(ChatColor.RED + "That player isn't in the invite list.");
            return;
        }

        try {
            plugin.invitesTable.closeInvitation(playerMeta.getUUID(), channelName);
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "There was an error removing the invite.");
            plugin.getLogger().warning(ex.toString());
            return;
        }

        sender.sendMessage(ChatColor.BLUE + "Player removed from the invite list");

    }


    private void changeChannelOwner(CommandSender sender, String channelName, String newOwner) {

        if (!this.senderIsOwner(sender, channelName, true) && !sender.hasPermission("nerdclanchat.admin")) {
            sender.sendMessage(ChatColor.RED + "Only the owner can set a new owner for the channel");
            return;
        }

        channelName = channelName.toLowerCase();
        PlayerMeta newOwnerMeta = plugin.playerMetaCache.getPlayerMetaByName(newOwner.toLowerCase());
        Channel channel = plugin.channelCache.getChannel(channelName);
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName);

        if (newOwnerMeta == null || !members.containsKey(newOwnerMeta.getUUID())) {
            sender.sendMessage(ChatColor.RED + "The new owner must be a member of the channel.");
            return;
        }

        try {
            // Make the old owner a manager, if they're not already
            ChannelMember oldOwnerMember = members.get(channel.getOwner());
            oldOwnerMember.setManager(true);
            members.put(channel.getOwner(), oldOwnerMember);
            plugin.channelMembersTable.update(oldOwnerMember);

            // Change the channel owner
            channel.setOwner(newOwnerMeta.getUUID());
            plugin.channelsTable.update(channel);

            // Ensure the new owner is a manager, for consistency
            ChannelMember newOwnerMember = members.get(newOwnerMeta.getUUID());
            newOwnerMember.setManager(true);
            members.put(newOwnerMeta.getUUID(), newOwnerMember);
            plugin.channelMembersTable.update(newOwnerMember);

            // Update cache
            plugin.channelCache.updateChannel(channelName, channel);
            plugin.channelCache.updateChannelMembers(channelName, members);

        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "There was an error changing the channel owner.");
            plugin.getLogger().warning(ex.toString());
            return;
        }

        sender.sendMessage(ChatColor.BLUE + String.format("You have relinquished ownership on %s to %s", channelName, newOwner));

    }


    private void addChannelManager(CommandSender sender, String channelName, String playerName) {

        if (!this.senderIsOwner(sender, channelName, false)) {
            sender.sendMessage(ChatColor.RED + "Only the owner may add or remove managers");
            return;
        }

        channelName = channelName.toLowerCase();
        PlayerMeta managerMeta = plugin.playerMetaCache.getPlayerMetaByName(playerName);
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName);

        if (managerMeta == null || !members.containsKey(managerMeta.getUUID())) {
            sender.sendMessage(ChatColor.RED + "Only members can be made managers. Invite them to the channel, and wait for them to join first.");
            return;
        }

        if (members.get(managerMeta.getUUID()).isManager()) {
            sender.sendMessage(ChatColor.RED + String.format("%s is already a manager", playerName));
            return;
        }

        try {
            ChannelMember cm = members.get(managerMeta.getUUID());
            cm.setManager(true);
            members.put(cm.getUUID(), cm);
            plugin.channelMembersTable.update(cm);
            plugin.channelCache.updateChannelMembers(channelName, members);
            sender.sendMessage(ChatColor.BLUE + String.format("%s added as a manager!", playerName));
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "There was an error updating the manager status.");
            plugin.getLogger().warning(ex.toString());
            return;
        }

        Player player = plugin.getServer().getPlayer(UUID.fromString(managerMeta.getUUID()));
        if (player != null) {
            player.sendMessage(ChatColor.BLUE + String.format("You have been made a manager in %s", channelName));
        }

    }


    private void removeChannelManager(CommandSender sender, String channelName, String playerName) {

        if (!this.senderIsOwner(sender, channelName, false)) {
            sender.sendMessage(ChatColor.RED + "Only the owner may add or remove managers");
            return;
        }

        channelName = channelName.toLowerCase();
        PlayerMeta managerMeta = plugin.playerMetaCache.getPlayerMetaByName(playerName);
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName);

        if (managerMeta == null || !members.containsKey(managerMeta.getUUID()) || !members.get(managerMeta.getUUID()).isManager()) {
            sender.sendMessage(ChatColor.RED + String.format("%s is not a manager in %s", playerName, channelName));
            return;
        }

        try {
            ChannelMember cm = members.get(managerMeta.getUUID());
            cm.setManager(false);
            members.put(cm.getUUID(), cm);
            plugin.channelMembersTable.update(cm);
            plugin.channelCache.updateChannelMembers(channelName, members);
            sender.sendMessage(ChatColor.BLUE + String.format("%s removed as a manager from %s", playerName, channelName));
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "There was an error updating the manager status.");
            plugin.getLogger().warning(ex.toString());
        }

    }


    private void listChannelManagers(CommandSender sender, String channelName) {

        if (!this.senderIsOwner(sender, channelName, true)) {
            sender.sendMessage(ChatColor.RED + "Only channel owners can do that");
            return;
        }

        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName);

        List<String> online = new ArrayList<String>();
        List<String> offline = new ArrayList<String>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (members.containsKey(player.getUniqueId().toString())) {
                if (members.get(player.getUniqueId().toString()).isManager()) {
                    online.add(player.getName());
                }
            }
        }
        for (ChannelMember member : members.values()) {
            if (member.isManager()) {
                if (!online.contains(member.getName())) {
                    offline.add(member.getName());
                }
            }
        }

        sender.sendMessage(ChatColor.GOLD + "Online: " + NCCUtil.formatList(online, ChatColor.WHITE, ChatColor.GRAY));
        sender.sendMessage(ChatColor.GOLD + "Offline: " + NCCUtil.formatList(offline, ChatColor.WHITE, ChatColor.GRAY));

    }


    private void removeMemberFromChannel(CommandSender sender, String channelName, String playerName) {

        if (!this.senderIsManager(sender, channelName, false)) {
            sender.sendMessage(ChatColor.RED + "Sorry, you have to be a manager to do that!");
            return;
        }

        PlayerMeta playerMeta = plugin.playerMetaCache.getPlayerMetaByName(playerName);
        Channel channel = plugin.channelCache.getChannel(channelName);
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName);
        boolean senderIsOwner = this.senderIsOwner(sender, channelName, false);
        ChannelMember member;

        if (playerMeta != null && members.containsKey(playerMeta.getUUID())) {
            member = members.get(playerMeta.getUUID());
        } else {
            sender.sendMessage(ChatColor.RED + "That player isn't a member.");
            return;
        }

        if (member.getUUID().equals(channel.getOwner())) {
            sender.sendMessage(ChatColor.RED + "You cannot remove the owner from a channel!");
            return;
        }

        if (!senderIsOwner && member.isManager()) {
            sender.sendMessage(ChatColor.RED + "Only the owner can remove a manager");
            return;
        }

        try {
            plugin.channelMembersTable.delete(member);
            members.remove(member.getUUID());
            plugin.channelCache.updateChannelMembers(channelName, members);
            sender.sendMessage(ChatColor.BLUE + "Member removed.");
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "There was an error removing the channel member.");
            plugin.getLogger().warning(ex.toString());
        }

    }


    private void joinChannel(CommandSender sender, String channelName) {

        if (!(sender instanceof Player)) return;

        channelName = channelName.toLowerCase();
        Player player = (Player) sender;
        String UUID = player.getUniqueId().toString();
        PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);
        Channel channel = plugin.channelCache.getChannel(channelName);
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName);

        if (members.containsKey(UUID)) {
            sender.sendMessage(ChatColor.RED + "You're already a member of this channel!");
            return;
        }

        if (channel == null) {
            sender.sendMessage(ChatColor.RED + "That channel doesn't exist...yet.");
            return;
        }

        if (!channel.isPub() && !plugin.invitesTable.alreadyInvited(UUID, channelName)) {
            ChannelMember owner = members.get(channel.getOwner());
            sender.sendMessage(ChatColor.RED + String.format("You can't join a non-public channel without an invite. Please speak with %s about joining", owner.getName()));
            return;
        }

        try {
            ChannelMember mem = new ChannelMember(channelName, UUID, player.getName(), false);
            members.put(UUID, mem);
            plugin.channelMembersTable.save(mem);
            plugin.channelCache.updateChannelMembers(channelName, members);
            plugin.invitesTable.closeInvitation(UUID, channelName);
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "There was an error joining the channel.");
            plugin.getLogger().warning(ex.toString());
            return;
        }

        meta.setDefaultChannel(channelName);
        plugin.playerMetaCache.updatePlayerMeta(UUID, meta);

        sender.sendMessage(ChatColor.BLUE + String.format("You will receive bulletins from this channel on login. To unsubscribe run /clanchat unsubscribe %s", channelName));
        String helpMsg = String.format("Type %s/c #%s <msg>%s to say something to this channel, or just %s/c <msg>%s if this is already your default channel", ChatColor.GRAY, channelName, ChatColor.BLUE, ChatColor.GRAY, ChatColor.BLUE);
        sender.sendMessage(ChatColor.BLUE + helpMsg);
        this.sendRawMessage(channelName, String.format("%s%s%s has joined %s%s%s, say hi!", ChatColor.RED, player.getName(), ChatColor.BLUE, ChatColor.valueOf(channel.getColor()), channelName, ChatColor.BLUE));

    }


    private void leaveChannel(CommandSender sender, String channelName) {

        if (!(sender instanceof Player)) return;

        channelName = channelName.toLowerCase();
        Channel channel = plugin.channelCache.getChannel(channelName);
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName);
        Player player = (Player) sender;
        String UUID = player.getUniqueId().toString();

        if (!members.containsKey(UUID)) {
            sender.sendMessage(ChatColor.RED + "You can't leave a channel you're not in");
            return;
        }

        if (channel.getOwner().equals(UUID) && members.size() > 1) {
            sender.sendMessage(ChatColor.RED + "The owner can't leave their channel unless the channel is empty. Please set someone else as owner first, or use \"/clanchat delete <channel>\" to remove the channel.");
        }

        // Leave the channel
        try {
            plugin.channelMembersTable.delete(members.get(UUID));
            members.remove(UUID);
            plugin.channelCache.updateChannelMembers(channelName, members);
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "There was an error leaving the channel.");
            plugin.getLogger().warning(ex.toString());
            return;
        }

        // Delete the channel if it's empty when the owner leaves it
        if (members.size() == 0) {
            try {
                plugin.channelsTable.delete(channel);
                plugin.channelMembersTable.deleteChannelMembers(channelName);
                plugin.bulletinsTable.deleteChannelBulletins(channelName);
                plugin.invitesTable.deleteChannelInvites(channelName);
                plugin.channelCache.remove(channelName);
            } catch (Exception ex) {
                plugin.getLogger().warning(ex.toString());
                sender.sendMessage(ChatColor.RED + "There was an error deleting your channel.");
                return;
            }
        }

        sender.sendMessage(ChatColor.BLUE + "You have been removed from " + channelName);

    }


    private void listChannels(CommandSender sender) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Console can't join channels. Try \"/clanchat public\" to list all public channels.");
            return;
        }

        Player player = (Player) sender;
        String UUID = player.getUniqueId().toString();
        List<ChannelMember> channels = plugin.transientPlayerCache.getChannelsForPlayer(UUID);
        List<Channel> channelList = new ArrayList<Channel>();

        if (channels.size() < 1) {
            sender.sendMessage(ChatColor.RED + "You aren't in any channels");
            return;
        }

        for (ChannelMember cm : channels) {
            Channel channel = plugin.channelCache.getChannel(cm.getChannel());
            channelList.add(channel);
        }
        sender.sendMessage(NCCUtil.formatChannelList(channelList));

    }


    private void listAllChannels(CommandSender sender) {

        if (!sender.hasPermission("nerdclanchat.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        List<Channel> channels = plugin.channelsTable.getAllChannels();
        StringBuilder sb = new StringBuilder();

        for (Channel channel : channels) {

            PlayerMeta owner = plugin.playerMetaCache.getPlayerMeta(channel.getOwner());

            sb.append(ChatColor.BLUE);
            sb.append(channel.getName());

            if (owner != null) {
                sb.append(ChatColor.WHITE);
                sb.append(": ");
                sb.append(ChatColor.GRAY);
                sb.append(owner.getName());
            }

            if (channels.indexOf(channel) != (channels.size() -1)) {
                sb.append(ChatColor.WHITE);
                sb.append(", ");
            }

        }

        sender.sendMessage(sb.toString());

    }


    private void listAllPublicChannels(CommandSender sender) {
        List<Channel> channels = plugin.channelsTable.getAllChannels();
        List<Channel> list = new ArrayList<Channel>();
        for (Channel c : channels) {
            if (c.isPub()) {
                list.add(c);
            }
        }
        if (list.size() > 0) {
            sender.sendMessage(NCCUtil.formatChannelList(list));
        } else {
            sender.sendMessage(ChatColor.RED + "There are no public channels yet.");
        }
    }


    private void addBulletin(CommandSender sender, String[] args) {

        String channelName = args[1].toLowerCase();
        String message = NCCUtil.joinArray(" ", Arrays.copyOfRange(args, 2, args.length));

        if (!this.senderIsManager(sender, channelName, false)) {
            sender.sendMessage(ChatColor.RED + "Sorry, you have to be a manager to do that!");
            return;
        }

        Channel channel = plugin.channelCache.getChannel(channelName);
        List<Bulletin> bulletins = plugin.channelCache.getBulletins(channelName);

        try {
            Bulletin nb = new Bulletin(channelName, message);
            bulletins.add(nb);
            plugin.bulletinsTable.save(nb);
            plugin.channelCache.updateBulletins(channelName, bulletins);
        } catch (Exception ex) {
            plugin.getLogger().warning(ex.toString());
            sender.sendMessage(ChatColor.RED + "There was an error adding your bulletin.");
            return;
        }

        sender.sendMessage(ChatColor.BLUE + "Bulletin added successfully.");
        String msg = String.format("%s[%s] %s%s", ChatColor.valueOf(channel.getColor()), channelName, ChatColor.valueOf(channel.getAlertColor()), message);
        this.sendRawMessage(channelName, msg);

    }


    private void removeBulletin(CommandSender sender, String channelName, String number) {

        if (!this.senderIsManager(sender, channelName, false)) {
            sender.sendMessage(ChatColor.RED + "Sorry, you have to be a manager to do that!");
            return;
        }

        Integer index = Integer.parseInt(number);
        List<Bulletin> bulletins = plugin.channelCache.getBulletins(channelName);

        if (index < 1) {
            sender.sendMessage(ChatColor.RED + "The bulletin index specified must be a non-zero integer");
            return;
        }

        if (index > bulletins.size()) {
            sender.sendMessage(ChatColor.RED + "There is no bulletin at that index");
            return;
        }

        try {
            Bulletin rb = bulletins.get(index - 1);
            bulletins.remove(rb);
            plugin.bulletinsTable.delete(rb);
            plugin.channelCache.updateBulletins(channelName, bulletins);
        } catch (Exception ex) {
            plugin.getLogger().warning(ex.toString());
            sender.sendMessage(ChatColor.RED + "There was an error removing your bulletin.");
            return;
        }

        sender.sendMessage(ChatColor.BLUE + "Bulletin successfully removed");

    }


    private void subscribeToBulletins(CommandSender sender, String channelName) {

        if (!(sender instanceof Player)) return;

        if (!this.senderIsMember(sender, channelName)) {
            sender.sendMessage(ChatColor.RED + String.format("You must be a member of %s to subscribe to their bulletins", channelName));
            return;
        }

        Player player = (Player) sender;
        String UUID = player.getUniqueId().toString();
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName);
        ChannelMember member = members.get(UUID);

        if (member.isSubscribed()) {
            sender.sendMessage(ChatColor.RED + "You are already subscribed to that channel");
            return;
        }

        try {
            member.setSubscribed(true);
            members.put(UUID, member);
            plugin.channelMembersTable.update(member);
            plugin.channelCache.updateChannelMembers(channelName, members);
            sender.sendMessage(ChatColor.BLUE + String.format("You are now subscribed to bulletins made in %s", channelName));
        } catch (Exception ex) {
            plugin.getLogger().warning(ex.toString());
            sender.sendMessage(ChatColor.RED + "There was an error subscribing to the channel's bulletins.");
        }

    }


    private void unsubscribeFromBulletins(CommandSender sender, String channelName) {

        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        String UUID = player.getUniqueId().toString();
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName);

        if (!members.containsKey(UUID) || !members.get(UUID).isSubscribed()) {
            sender.sendMessage(ChatColor.RED + "You are not subscribed to that channel.");
            return;
        }

        try {
            ChannelMember member = members.get(UUID);
            member.setSubscribed(false);
            members.put(UUID, member);
            plugin.channelMembersTable.update(member);
            plugin.channelCache.updateChannelMembers(channelName, members);
            sender.sendMessage(ChatColor.BLUE + String.format("You are now unsubscribed from bulletins made in %s", channelName));
        } catch (Exception ex) {
            plugin.getLogger().warning(ex.toString());
            sender.sendMessage(ChatColor.RED + "There was an error unsubscribing from the channel's bulletins.");
        }

    }


    private void listSubscriptions(CommandSender sender) {

        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        String UUID = player.getUniqueId().toString();
        List<ChannelMember> channels = plugin.transientPlayerCache.getChannelsForPlayer(UUID);
        List<String> subscribed = new ArrayList<String>();

        for (ChannelMember channel : channels) {
            if (channel.isSubscribed()) {
                subscribed.add(channel.getChannel());
            }
        }

        if (subscribed.size() < 1) {
            sender.sendMessage(ChatColor.BLUE + "You have no current subscriptions.");
            return;
        }

        String list = NCCUtil.formatList(subscribed, ChatColor.GRAY, ChatColor.GRAY);
        sender.sendMessage(ChatColor.BLUE + "Current subscriptions: " + ChatColor.GRAY + list);

    }


    private void consoleChat(CommandSender sender, String[] args) {

        String channelName = args[1].toLowerCase();
        String message = NCCUtil.joinArray(" ", Arrays.copyOfRange(args, 2, args.length));
        Channel channel = plugin.channelCache.getChannel(channelName);

        if (channel != null) {
            String name = ChatColor.RED + "~console";
            String tag = String.format("%s[%s] %s<%s%s> ", ChatColor.valueOf(channel.getColor()), channel.getName(), ChatColor.GRAY, name, ChatColor.GRAY);
            String msg = tag + ChatColor.valueOf(channel.getTextColor()) + message;
            this.sendRawMessage(channelName, msg);
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid channel");
        }

    }


    private void setFlags(CommandSender sender, String[] args) {

        String channelName = args[1].toLowerCase();
        String flagKey = args[2].toLowerCase();
        String flagValue = args[3].toLowerCase();
        Channel channel = plugin.channelCache.getChannel(channelName);

        if (!this.senderIsOwner(sender, channelName, false)) {
            sender.sendMessage(ChatColor.RED + "This command is only available to the channel owner");
            return;
        }

        if (!flagValue.equals("true") && !flagValue.equals("false")) {
            sender.sendMessage(ChatColor.RED + "Invalid command, type /clanchat flags for help (invalid boolean)");
            return;
        }

        if (flagKey.equals("public")) {
            channel.setPub(Boolean.valueOf(flagValue));
        }
        else if (flagKey.equals("secret")) {
            channel.setSecret(Boolean.valueOf(flagValue));
        }
        else {
            sender.sendMessage(ChatColor.RED + "Invalid command, type /clanchat flags for help (invalid flag)");
            return;
        }

        try {
            plugin.channelsTable.update(channel);
            plugin.channelCache.updateChannel(channelName, channel);
            sender.sendMessage(ChatColor.BLUE + String.format("Flag %s has been set to %s for %s", flagKey, flagValue, channelName));
        } catch (Exception ex) {
            plugin.getLogger().warning(ex.toString());
            sender.sendMessage(ChatColor.RED + "There was an error updating the channel flags.");
        }

    }


    private boolean senderIsMember(CommandSender sender, String channelName) {
        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        if (!(sender instanceof Player)) return true;
        if (channel == null) return false;
        Player player = (Player) sender;
        return members.containsKey(player.getUniqueId().toString());
    }


    private boolean senderIsManager(CommandSender sender, String channelName, boolean allowConsole) {
        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        if (allowConsole && !(sender instanceof Player)) return true;
        if (!(sender instanceof Player)) return false;
        if (channel == null) return false;
        Player player = (Player) sender;
        String UUID = player.getUniqueId().toString();
        if (!(members.containsKey(UUID))) return false;
        return (members.get(UUID).isManager() || channel.getOwner().equals(UUID));
    }


    private boolean senderIsOwner(CommandSender sender, String channelName, boolean allowConsole) {
        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        if (allowConsole && !(sender instanceof Player)) return true;
        if (!(sender instanceof Player)) return false;
        if (channel == null) return false;
        Player player = (Player) sender;
        String UUID = player.getUniqueId().toString();
        return channel.getOwner().equals(UUID);
    }


    private void sendRawMessage(String channelName, String message) {
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (members.containsKey(player.getUniqueId().toString())) {
                player.sendMessage(message);
            }
        }
    }


    private void printHelpText(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "ClanChat usage:");
        sender.sendMessage(ChatColor.BLUE + "/clanchat create <channel>" + ChatColor.WHITE + " - Creates a new channel, with you as the owner.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat join <channel>" + ChatColor.WHITE + " - Joins a channel that you've already been invited to.");
        sender.sendMessage(ChatColor.BLUE + "/c [#<channel>] <message>" + ChatColor.WHITE + " - Sends a message to the channel. You must be a member of the channel. If you do not include a channel (prefixed by '#'), it will default to the last channel used.");
        sender.sendMessage(ChatColor.BLUE + "/c #<channel>" + ChatColor.WHITE + "Set your default channel without sending a message.");
        sender.sendMessage(ChatColor.BLUE + "/cq #<channel> <message>" + ChatColor.WHITE + " - Sends a quick message to the specified channel. This does not change your default channel.");
        sender.sendMessage(ChatColor.BLUE + "/ca [#<channel>] <message>" + ChatColor.WHITE + " - Sends an alert message to the channel. You must be an owner/manager of the channel. If a channel name is not included it will alert your current channel.");
        sender.sendMessage(ChatColor.BLUE + "/cme [#<channel>] <message>" + ChatColor.WHITE + "Sends a \"/me\" type message to your current, or specified, channel.");
        sender.sendMessage(ChatColor.BLUE + "/cr <message>" + ChatColor.WHITE + " - Sends a message to the last channel that you received a message from, regardless of what your default channel is.");
        sender.sendMessage(ChatColor.BLUE + "/cb [#<channel>]" + ChatColor.WHITE + " - List bulletins for all channels you are a member of, or specify a channel to receive its bulletins.");
        sender.sendMessage(ChatColor.BLUE + "/cm [#<channel>]" + ChatColor.WHITE + " - Lists all members in your default channel. If a channel is given, lists members in that channel.");
        sender.sendMessage(ChatColor.GRAY + "Type " + ChatColor.BLUE + "/clanchat more" + ChatColor.GRAY + " for additional commands.");
    }


    private void printMoreHelpText(CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "/cs [#<channel>] <message>" + ChatColor.WHITE + " - Sends a \"/s\" type sarcasm message to your current, or specified, channel.");
        sender.sendMessage(ChatColor.BLUE + "/crq [#<channel>] <message>" + ChatColor.WHITE + " or " + ChatColor.BLUE + "/cqr [#<channel>] <message>" + ChatColor.WHITE + " - Sends a message to the last channel that you received a message from, regardless of what your default channel is and does not change your default channel.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat delete <channel>" + ChatColor.WHITE + " - Deletes a channel. Owner only.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat color <channel> <color>" + ChatColor.WHITE + " - Sets the channel color. Set by channel owner/managers.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat textcolor <channel> <color>" + ChatColor.WHITE + " - Sets the channel text color. Set by channel owner/managers.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat alertcolor <channel> <color>" + ChatColor.WHITE + " - Sets the channel alert color. Set by channel owner/managers.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat members <channel>" + ChatColor.WHITE + " - Lists all the members in a channel.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat invite <channel> <player>" + ChatColor.WHITE + " - Invites a player to the channel. You must be a manager to invite.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat uninvite <channel> <player>" + ChatColor.WHITE + " - Uninvites a previously invited player. You must be a manager.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat changeowner <channel> <player>" + ChatColor.WHITE + " - Changes the owner on the given channel. Only the owner can set this. " + ChatColor.RED + "Careful! This can't be undone.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat addmanager <channel> <player>" + ChatColor.WHITE + " - Adds a manager to the channel. You must be the owner to run this.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat removemanager <channel> <player>" + ChatColor.WHITE + " - Removes a manager from the channel. You must be the owner to run this.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat listmanagers <channel>" + ChatColor.WHITE + " - List all managers in the channel. You must be owner to run this.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat remove <channel> <player>" + ChatColor.WHITE + " - Removes a player from the channel. You must be a manager to do this.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat leave <channel>" + ChatColor.WHITE + " - Leaves a channel that you're in.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat list" + ChatColor.WHITE + " - Lists all the channels you are in.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat public" + ChatColor.WHITE + " - Lists all public channels.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat flags" + ChatColor.WHITE + " - Sets channel flags. Type /clanchat flags for more information.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat addbulletin <channel> <bulletin>" + ChatColor.WHITE + " - Add bulletin to the channel.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat removebulletin <channel> <number>" + ChatColor.WHITE + " - Remove the bulletin from the channel, <number> starts at 1 with the top bulletin.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat subscribe <channel>" + ChatColor.WHITE + " - Subscribe to a channel's bulletins.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat unsubscribe <channel>" + ChatColor.WHITE + " - Unsubscribe from a channel's bulletins.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat subscriptions" + ChatColor.WHITE + " - List your current bulletin subscriptions.");
        if (sender.hasPermission("nerdclanchat.admin")) {
            sender.sendMessage(ChatColor.BLUE + "/clanchat channels" + ChatColor.WHITE + " - Lists all the channels and their owners.");
            sender.sendMessage(ChatColor.BLUE + "/clanchat reloadconfig" + ChatColor.WHITE + " - Reload the plugin configuration.");
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.BLUE + "/clanchat chat <channel> <message>" + ChatColor.WHITE + " - Chats to an arbitrary channel. Only available to console.");
        }
    }


}
