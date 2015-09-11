package nu.nerd.NerdClanChat;

import nu.nerd.NerdClanChat.database.Channel;
import nu.nerd.NerdClanChat.database.ChannelMember;
import nu.nerd.NerdClanChat.database.PlayerMeta;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class ClanChatCommand implements CommandExecutor {


    private final NerdClanChat plugin;


    public ClanChatCommand(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length < 1) {
            this.printHelpText(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("more")) {
            this.printMoreHelpText(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("create") && args.length > 1) {
            this.createChannel(sender, args[1]);
            return true;
        }
        else if (args[0].equalsIgnoreCase("test")) {
            try {
                Channel ch = new Channel();
                ch.setName(args[1]);
                ch.setOwner("fake-uuid-placeholder");
                ch.setColor("BLUE");
                ch.setTextColor("GRAY");
                ch.setAlertColor("GRAY");
                ch.setSecret(false);
                ch.setPub(false);
                plugin.channelsTable.save(ch);
            } catch (Exception ex) {
                sender.sendMessage(ex.toString());
            }
            return true;
        }
        else {
            this.printHelpText(sender);
            return true;
        }

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
            ChannelMember mem = new ChannelMember(name, owner.getUniqueId().toString(), true);
            try {
                plugin.channelsTable.save(ch);
                plugin.channelMembersTable.save(mem);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "There was an error creating the channel.");
                plugin.getLogger().warning(ex.toString());
                return;
            }

            plugin.channelCache.updateChannel(name, ch); //Cache the newly created channel

            //Update owner's player meta to make this their default channel
            String UUID = owner.getUniqueId().toString();
            PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);
            meta.setDefaultChannel(name);
            plugin.playerMetaCache.updatePlayerMeta(UUID, meta);

            sender.sendMessage(ChatColor.BLUE + "You will receive bulletins from this channel on login. To unsubscribe run /clanchat unsubscribe " + name);
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Channel created!");

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
        sender.sendMessage(ChatColor.BLUE + "/cs [#<channel>] <message>" + ChatColor.WHITE + "Sends a \"/s\" type sarcasm message to your current, or specified, channel.");
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
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.BLUE + "/clanchat chat <channel> <message>" + ChatColor.WHITE + " - Chats to an arbitrary channel. Only available to console.");
        }
    }


}
