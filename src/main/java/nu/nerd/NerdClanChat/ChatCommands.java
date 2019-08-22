package nu.nerd.NerdClanChat;

import nu.nerd.NerdClanChat.database.Bulletin;
import nu.nerd.NerdClanChat.database.Channel;
import nu.nerd.NerdClanChat.database.ChannelMember;
import nu.nerd.NerdClanChat.database.PlayerMeta;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class ChatCommands implements TabExecutor {


    private final NerdClanChat plugin;
    private enum MessageType {
        NORMAL, ME, ALERT, SARCASM
    }


    public ChatCommands(NerdClanChat plugin) {
        this.plugin = plugin;
    }

    /**
     * Prevent server from autosuggesting player names.
     */
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("c")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /c [#<channel>] <message>");
            }
            else if (args[0].charAt(0) == '#' && args.length == 1) {
                this.setDefaultChannel(sender, args[0].substring(1));
            } else {
                this.chat(sender, args, MessageType.NORMAL);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("cq")) {
            if (args.length < 2 || args[0].charAt(0) != '#') {
                sender.sendMessage(ChatColor.RED + "Usage: /cq #<channel> <message>");
            } else {
                this.cq(sender, args);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("crq")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /crq <message>");
            } else {
                this.crq(sender, args);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("cqr")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /cqr <message>");
            } else {
                this.cqr(sender, args);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("ca")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /ca [#<channel>] <message>");
            } else {
                this.chat(sender, args, MessageType.ALERT);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("cme")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /cme [#<channel>] <message>");
            }
            else if (args[0].charAt(0) == '#' && args.length == 1) {
                this.setDefaultChannel(sender, args[0].substring(1));
            } else {
                this.chat(sender, args, MessageType.ME);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("cs")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /cs [#<channel>] <message>");
            }
            else if (args[0].charAt(0) == '#' && args.length == 1) {
                this.setDefaultChannel(sender, args[0].substring(1));
            } else {
                this.chat(sender, args, MessageType.SARCASM);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("cr")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /cr <message>");
            } else {
                this.cr(sender, args);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("cm")) {
            this.cm(sender, args);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("cb")) {
            this.cb(sender, args);
            return true;
        }

        return false;

    }


    private void chat(CommandSender sender, String[] args, MessageType type) {
        String channel;
        String message;
        if (args[0].charAt(0) == '#') {
            channel = args[0].substring(1);
            message = NCCUtil.joinArray(" ", Arrays.copyOfRange(args, 1, args.length));
        } else {
            channel = getDefaultChannel(sender);
            message = NCCUtil.joinArray(" ", args);
            if (channel == null) {
                sender.sendMessage(String.format("%sYou don't have a default channel set (or aren't in any channels). Run %s/clanchat%s for help", ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.RED));
                return;
            }
        }
        this.sendMessage(sender, channel, message, type, true);
    }


    private void cq(CommandSender sender, String[] args) {
        String channel = args[0].substring(1);
        String message = NCCUtil.joinArray(" ", Arrays.copyOfRange(args, 1, args.length));
        this.sendMessage(sender, channel, message, MessageType.NORMAL, false);
    }


    private void crq(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String message = NCCUtil.joinArray(" ", args);
            String channelName = this.getLastChannelReceived(player);
            if (channelName != null) {
                this.sendMessage(sender, channelName, message, MessageType.NORMAL, false);
            } else {
                sender.sendMessage(ChatColor.RED + "You have not yet received a message to reply to.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You can't do that from console.");
        }
    }


    private void cqr(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String message = NCCUtil.joinArray(" ", args);
            String channelName = this.getLastChannelReceived(player);
            if (channelName != null) {
                this.sendMessage(sender, channelName, message, MessageType.NORMAL, false);
            } else {
                sender.sendMessage(ChatColor.RED + "You have not yet received a message to reply to.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You can't do that from console.");
        }
    }


    private void cr(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String message = NCCUtil.joinArray(" ", args);
            String channelName = this.getLastChannelReceived(player);
            if (channelName != null) {
                this.sendMessage(sender, channelName, message, MessageType.NORMAL, true);
            } else {
                sender.sendMessage(ChatColor.RED + "You have not yet received a message to reply to.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You can't do that from console.");
        }
    }


    private void cm(CommandSender sender, String[] args) {
        String channel;
        if (args.length < 1) {
            channel = getDefaultChannel(sender);
            if (channel == null) {
                sender.sendMessage(ChatColor.RED + "No previous channel");
                return;
            }
        }
        else if (args.length == 1 && args[0].charAt(0) == '#') {
            channel = args[0].substring(1);
        }
        else {
            sender.sendMessage(ChatColor.RED + "Usage: /cm [#<channel>]");
            return;
        }
        this.listChannelMembers(sender, channel);
    }


    private void cb(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].charAt(0) == '#') {
            String channel = args[0].substring(1);
            this.printBulletins(sender, channel);
        } else {
            this.printAllBulletins(sender);
        }
    }


    private void sendMessage(CommandSender sender, String channelName, String message, MessageType type, boolean changeDefault) {

        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        String tag;
        String msg;
        String name;

        // Check permission
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if ( !(members.containsKey(player.getUniqueId().toString())) ) {
                sender.sendMessage(ChatColor.RED + "You can't send a message to a channel you aren't a member of");
                return;
            }
        }
        if (type == MessageType.ALERT && !this.assertManager(sender, channelName)) return;

        // Get sender name, using ~console for console
        if (sender instanceof Player) {
            Player player = (Player) sender;
            name = ChatColor.WHITE + player.getName();
        } else {
            name = ChatColor.RED + "~console";
        }

        // Format message
        if (type == MessageType.ME) {
            tag = String.format("%s[%s]", this.color(channel.getColor()), channel.getName());
            msg = String.format("%s %s* %s %s", tag, this.color(channel.getTextColor()), ChatColor.stripColor(name), message);
        }
        else if (type == MessageType.ALERT) {
            tag = String.format("%s[%s] %s<%s%s> ", this.color(channel.getColor()), channel.getName(), this.color(channel.getAlertColor()), name, this.color(channel.getAlertColor()));
            msg = tag + ChatColor.UNDERLINE + message;
        }
        else if (type == MessageType.SARCASM) {
            tag = String.format("%s[%s] %s<%s%s> ", this.color(channel.getColor()), channel.getName(), ChatColor.GRAY, name, ChatColor.GRAY);
            msg = tag + this.color(channel.getTextColor()) + ChatColor.ITALIC + message;
        }
        else {
            tag = String.format("%s[%s] %s<%s%s> ", this.color(channel.getColor()), channel.getName(), ChatColor.GRAY, name, ChatColor.GRAY);
            msg = tag + this.color(channel.getTextColor()) + message;
        }

        // Change default, if applicable
        if (changeDefault) {
            this.setDefaultChannel(sender, channelName);
        }

        // Send message
        plugin.getLogger().info(ChatColor.stripColor(msg));
        this.sendRawMessage(channelName, msg);

        // Update last received channel
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (members.containsKey(player.getUniqueId().toString())) {
                this.updateLastChannelReceived(player, channelName);
            }
        }

    }


    private void sendRawMessage(String channelName, String message) {
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (members.containsKey(player.getUniqueId().toString())) {
                player.sendMessage(message);
            }
        }
    }


    private void listChannelMembers(CommandSender sender, String channelName) {

        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());

        if (channel == null) {
            sender.sendMessage(ChatColor.RED + "That channel does not exist");
            return;
        }

        // Deny access if a non-member tries to list a secret channel
        if (sender instanceof Player) {
            if (channel.isSecret()) {
                Player player = (Player) sender;
                if (!members.containsKey(player.getUniqueId().toString())) {
                    sender.sendMessage(ChatColor.RED + "This channel is secret. You must be a member of the channel to see who is in the channel");
                    return;
                }
            }
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


    private void printBulletins(CommandSender sender, String channelName) {

        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        List<Bulletin> bulletins = plugin.channelCache.getBulletins(channelName.toLowerCase());

        if (channel == null) {
            sender.sendMessage(ChatColor.RED + "That channel does not exist");
            return;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!members.containsKey(player.getUniqueId().toString())) {
                sender.sendMessage(ChatColor.RED + String.format("You must be a member of %s to see their bulletins", channelName));
                return;
            }
        }

        if (bulletins.size() > 0) {
            String tag = String.format("%s[%s] ", this.color(channel.getColor()), channel.getName());
            for (Bulletin bulletin : bulletins) {
                String msg = tag + this.color(channel.getAlertColor()) + bulletin.getMessage();
                sender.sendMessage(msg);
            }
        } else {
            sender.sendMessage(ChatColor.RED + String.format("There are no active bulletins for %s", channelName));
        }

    }


    private void printAllBulletins(CommandSender sender) {
        if (sender instanceof Player) {

            List<Bulletin> bulletins;
            Player player = (Player) sender;
            String UUID = player.getUniqueId().toString();
            List<ChannelMember> channels = plugin.transientPlayerCache.getChannelsForPlayer(UUID);

            if (channels != null && channels.size() > 0) {
                for (ChannelMember cm : channels) {
                    bulletins = plugin.channelCache.getBulletins(cm.getChannel());
                    if (bulletins.size() > 0) {
                        this.printBulletins(sender, cm.getChannel());
                    }
                }
            }

        }
    }


    private void setDefaultChannel(CommandSender sender, String channelName) {
        if (sender instanceof Player) {

            Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
            HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
            Player player = (Player) sender;
            String UUID = player.getUniqueId().toString();
            PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);

            if (channel == null) {
                sender.sendMessage(ChatColor.RED + String.format("The channel \"%s\" doesn't exist", channelName));
                return;
            }

            if (!members.containsKey(UUID)) {
                ChannelMember owner = members.get(channel.getOwner());
                String subtext = (!channel.isPub()) ? String.format("Please speak to %s to join.", owner.getName()) : String.format("Join with /clanchat join %s", channelName);
                sender.sendMessage(ChatColor.RED + String.format("You are not a member of %s. %s", channelName, subtext));
                return;
            }

            if (meta.getDefaultChannel() == null || !meta.getDefaultChannel().equals(channelName)) {
                sender.sendMessage(ChatColor.BLUE + "Your default channel has been changed to " + channelName);
            }

            meta.setDefaultChannel(channelName);
            plugin.playerMetaCache.updatePlayerMeta(UUID, meta);

        }
    }


    private String getDefaultChannel(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String UUID = player.getUniqueId().toString();
            PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);
            if (meta != null && meta.getDefaultChannel() != null) {
                return meta.getDefaultChannel();
            }
        }
        return null;
    }


    private void updateLastChannelReceived(Player player, String channel) {
        String UUID = player.getUniqueId().toString();
        PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);
        meta.setLastReceived(channel);
        plugin.playerMetaCache.updatePlayerMeta(UUID, meta);
    }


    private String getLastChannelReceived(Player player) {
        String UUID = player.getUniqueId().toString();
        PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);
        return meta.getLastReceived();
    }


    private ChatColor color(String color) {
        return ChatColor.valueOf(color);
    }


    private boolean assertManager(CommandSender sender, String channelName) {
        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        String errorMsg = ChatColor.RED + "Sorry, you have to be a manager to do that!";
        if (!(sender instanceof Player)) {
            sender.sendMessage(errorMsg);
            return false;
        }
        Player player = (Player) sender;
        String UUID = player.getUniqueId().toString();
        if (!(members.containsKey(UUID))) {
            sender.sendMessage(errorMsg);
            return false;
        }
        if (!members.get(UUID).isManager() && !channel.getOwner().equals(UUID)) {
            sender.sendMessage(errorMsg);
            return false;
        }
        return true;
    }


}
