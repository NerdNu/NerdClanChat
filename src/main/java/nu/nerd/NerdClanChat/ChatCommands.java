package nu.nerd.NerdClanChat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import nu.nerd.NerdClanChat.database.Bulletin;
import nu.nerd.NerdClanChat.database.Channel;
import nu.nerd.NerdClanChat.database.ChannelMember;
import nu.nerd.NerdClanChat.database.PlayerMeta;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;


public class ChatCommands implements TabExecutor {


    private final NerdClanChat plugin;
    private enum MessageType {
        NORMAL, ME, ALERT, SARCASM
    }
    private final BukkitScheduler bukkitScheduler = Bukkit.getScheduler();


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

        String command = cmd.getName().toLowerCase();

        return switch (command) {
            case "c" -> {
                if (args.length < 1) {
                    sender.sendMessage(Component.text("Usage: /c [#<channel>] <message>", NamedTextColor.RED));
                } else if (args[0].charAt(0) == '#' && args.length == 1) {
                    this.setDefaultChannel(sender, args[0].substring(1));
                } else {
                    this.chat(sender, args, MessageType.NORMAL);
                }
                yield true;
            }
            case "cq" -> {
                if (args.length < 2 || args[0].charAt(0) != '#') {
                    sender.sendMessage(Component.text("Usage: /cq #<channel> <message>", NamedTextColor.RED));
                } else {
                    this.cq(sender, args);
                }
                yield true;
            }
            case "ca" -> {
                if (args.length < 1) {
                    sender.sendMessage(Component.text("Usage: /ca [#<channel>] <message>", NamedTextColor.RED));
                } else {
                    this.chat(sender, args, MessageType.ALERT);
                }
                yield true;
            }
            case "cme" -> {
                if (args.length < 1) {
                    sender.sendMessage(Component.text("Usage: /cme [#<channel>] <message>", NamedTextColor.RED));
                } else if (args[0].charAt(0) == '#' && args.length == 1) {
                    this.setDefaultChannel(sender, args[0].substring(1));
                } else {
                    this.chat(sender, args, MessageType.ME);
                }
                yield true;
            }
            case "cs" -> {
                if (args.length < 1) {
                    sender.sendMessage(Component.text("Usage: /cs [#<channel>] <message>", NamedTextColor.RED));
                } else if (args[0].charAt(0) == '#' && args.length == 1) {
                    this.setDefaultChannel(sender, args[0].substring(1));
                } else {
                    this.chat(sender, args, MessageType.SARCASM);
                }
                yield true;
            }
            case "cr" -> {
                if (args.length < 1) {
                    sender.sendMessage(Component.text("Usage: /cr <message>", NamedTextColor.RED));
                } else {
                    this.cr(sender, args);
                }
                yield true;
            }
            case "cm" -> {
                this.cm(sender, args);
                yield true;
            }
            case "cb" -> {
                this.cb(sender, args);
                yield true;
            }
            default -> false;
        };
    }


    private void chat(CommandSender sender, String[] args, MessageType type) {
        if (args[0].charAt(0) == '#') {
            String channel = args[0].substring(1);
            String message = NCCUtil.joinArray(" ", Arrays.copyOfRange(args, 1, args.length));
            this.sendMessage(sender, channel, message, type, true);
        } else {
            getDefaultChannel(sender).thenAccept(channel -> {
                String message = NCCUtil.joinArray(" ", args);
                if (channel == null) {
                    sender.sendMessage(Component.text("You don't have a default channel set (or aren't in any" +
                                    " channels). Run ", NamedTextColor.RED)
                            .append(Component.text("/clanchat", NamedTextColor.LIGHT_PURPLE))
                            .append(Component.text(" for help", NamedTextColor.RED)));
                } else {
                    bukkitScheduler.runTask(plugin, () -> this.sendMessage(sender, channel, message, type, true));
                }
            });
        }
    }


    private void cq(CommandSender sender, String[] args) {
        String channel = args[0].substring(1);
        String message = NCCUtil.joinArray(" ", Arrays.copyOfRange(args, 1, args.length));
        this.sendMessage(sender, channel, message, MessageType.NORMAL, false);
    }


    private void cr(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            String message = NCCUtil.joinArray(" ", args);
            this.getLastChannelReceived(player).thenAccept(channelName -> {
                if (channelName != null) {
                    this.sendMessage(sender, channelName, message, MessageType.NORMAL, true);
                } else {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You have not" +
                            " yet received a message to reply to.", NamedTextColor.RED)));
                }
            });
        } else {
            sender.sendMessage(Component.text("You can't do that from console.", NamedTextColor.RED));
        }
    }


    private void cm(CommandSender sender, String[] args) {
        if (args.length < 1) {
            getDefaultChannel(sender).thenAccept(channel -> {
                if (channel == null) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("No previous" +
                            " channel", NamedTextColor.RED)));
                } else {
                    listChannelMembers(sender, channel);
                }
            });
        } else if (args.length == 1 && args[0].charAt(0) == '#') {
            String channel = args[0].substring(1);
            listChannelMembers(sender, channel);
        } else {
            bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Usage: /cm" +
                    " [#<channel>]", NamedTextColor.RED)));
        }
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

        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName.toLowerCase());
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName.toLowerCase());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(channelFuture, membersFuture);

        allFutures.thenRun(() -> {
            try {

                Channel channel = channelFuture.get();
                HashMap<String, ChannelMember> members = membersFuture.get();

                // Check permission
                if (sender instanceof Player player && !(members.containsKey(player.getUniqueId().toString())) ) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You can't send" +
                            " a message to a channel you aren't a member of", NamedTextColor.RED)));
                    return;
                }

                if (type == MessageType.ALERT) {
                    this.assertManager(sender, channelName).thenAccept(isManager -> {
                        if(isManager) {
                            TextComponent msg = Component.text("[" + channelName + "]", NCCUtil.color(channel.getColor()))
                                    .append(Component.text(" <", NamedTextColor.GRAY))
                                    .append(Component.text(sender.getName(), NamedTextColor.WHITE))
                                    .append(Component.text("> ", NamedTextColor.GRAY))
                                    .append(Component.text(message, NCCUtil.color(channel.getAlertColor()), TextDecoration.UNDERLINED));

                            logAndSend(channelName, sender, message, msg, members);
                        }
                    });
                    return;
                }

                TextComponent tag;
                TextComponent msg;
                TextComponent name;

                TextColor channelColor = NCCUtil.color(channel.getColor());
                TextColor channelTextColor = NCCUtil.color(channel.getTextColor());

                // Get sender name, using ~console for console
                name = Component.text("");
                if(type != MessageType.ME) name = name.append(Component.text(" <", NamedTextColor.GRAY));
                if (sender instanceof Player player) {
                    name = name.append(Component.text(player.getName()));
                } else {
                    name = name.append(Component.text("~console", NamedTextColor.RED));
                }
                if(type != MessageType.ME) name = name.append(Component.text("> ", NamedTextColor.GRAY));

                tag = Component.text("[" + channel.getName() + "]", channelColor);

                // Format message
                if (type == MessageType.ME) {
                    msg = tag.append(Component.text(" * ", channelTextColor))
                            .append(name.color(channelTextColor))
                            .append(Component.text(" "))
                            .append(Component.text(message, channelTextColor));
                }
                else if (type == MessageType.SARCASM) {
                    msg = tag.append(name.color(NamedTextColor.WHITE)).append(Component.text(message, channelTextColor, TextDecoration.ITALIC));
                }
                else {
                    msg = tag.append(name.color(NamedTextColor.WHITE)).append(Component.text(message, channelTextColor));
                }

                // Change default, if applicable
                if (changeDefault) {
                    this.setDefaultChannel(sender, channelName);
                }

                logAndSend(channelName, sender, message, msg, members);

            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error sending a clanchat message.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });

    }

    private void logAndSend(String channelName, CommandSender sender, String message, TextComponent msg,
                            HashMap<String, ChannelMember> members) {
        // Send message
        plugin.log("[" + channelName + "] <" + sender.getName() + "> " + message, Level.INFO);
        this.sendRawMessage(channelName, msg);

        // Update last received channel
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (members.containsKey(player.getUniqueId().toString())) {
                this.updateLastChannelReceived(player, channelName);
            }
        }
    }


    private void sendRawMessage(String channelName, TextComponent message) {
        plugin.channelCache.getChannelMembers(channelName.toLowerCase()).thenAccept(members -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (members.containsKey(player.getUniqueId().toString())) {
                    bukkitScheduler.runTask(plugin, () -> player.sendMessage(message));
                }
            }
        });
    }


    private void listChannelMembers(CommandSender sender, String channelName) {

        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName.toLowerCase());
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName.toLowerCase());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(channelFuture, membersFuture);

        allFutures.thenRun(() -> {
            try {

                Channel channel = channelFuture.get();
                HashMap<String, ChannelMember> members = membersFuture.get();

                if (channel == null) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("That channel" +
                            " does not exist", NamedTextColor.RED)));
                    return;
                }

                // Deny access if a non-member tries to list a secret channel
                if (sender instanceof Player player) {
                    if (channel.isSecret()) {
                        if (!members.containsKey(player.getUniqueId().toString())) {
                            bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("This" +
                                    " channel is secret. You must be a member of the channel to see who is in the channel",
                                    NamedTextColor.RED)));
                            return;
                        }
                    }
                }

                // Collect players into online and offline lists
                List<String> online = new ArrayList<>();
                List<String> offline = new ArrayList<>();
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
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("Online: ", NamedTextColor.GOLD)
                            .append(NCCUtil.formatList(online, NamedTextColor.WHITE, NamedTextColor.GRAY)));
                    sender.sendMessage(Component.text("Offline: ", NamedTextColor.GOLD)
                            .append(NCCUtil.formatList(offline, NamedTextColor.WHITE, NamedTextColor.GRAY)));
                });
            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error fetching the list of channel members.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }


    private void printBulletins(CommandSender sender, String channelName) {

        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName.toLowerCase());
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        CompletableFuture<List<Bulletin>> bulletinsFuture = plugin.channelCache.getBulletins(channelName.toLowerCase());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf();

        allFutures.thenRun(() -> {
            try {

                Channel channel = channelFuture.get();
                HashMap<String, ChannelMember> members = membersFuture.get();
                List<Bulletin> bulletins = bulletinsFuture.get();

                if (channel == null) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("That channel" +
                            " does not exist", NamedTextColor.RED)));
                    return;
                }

                if (sender instanceof Player player && !members.containsKey(player.getUniqueId().toString())) {
                        bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You must be" +
                                " a member of " + channelName + " to see their bulletins", NamedTextColor.RED)));
                        return;
                    }


                if (!bulletins.isEmpty()) {
                    TextComponent tag = Component.text("[" + channelName + "] ", NCCUtil.color(channel.getColor()));
                    for (Bulletin bulletin : bulletins) {
                        TextComponent msg = tag.append(Component.text(bulletin.getMessage(), NCCUtil.color(channel.getAlertColor())));
                        bukkitScheduler.runTask(plugin, () -> bukkitScheduler.runTask(plugin, () -> sender.sendMessage(msg)));
                    }
                } else {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("There are no" +
                            " active bulletins for " + channelName, NamedTextColor.RED)));
                }
            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error printing a channel's bulletins.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }


    private void printAllBulletins(CommandSender sender) {
        if (sender instanceof Player player) {
            String UUID = player.getUniqueId().toString();
            plugin.transientPlayerCache.getChannelsForPlayer(UUID).thenAccept(channels -> {
                if (channels != null && !channels.isEmpty()) {
                    for (ChannelMember cm : channels) {
                        plugin.channelCache.getBulletins(cm.getChannel()).thenAccept(bulletins -> {
                            if (!bulletins.isEmpty()) {
                                bukkitScheduler.runTask(plugin, () -> this.printBulletins(sender, cm.getChannel()));
                            }
                        });
                    }
                }
            });
        }
    }


    private void setDefaultChannel(CommandSender sender, String channelName) {
        if (sender instanceof Player player) {

            CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName.toLowerCase());
            CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName.toLowerCase());

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(channelFuture, membersFuture);

            allFutures.thenRun(() -> {
                try {

                    Channel channel = channelFuture.get();
                    HashMap<String, ChannelMember> members = membersFuture.get();
                    String UUID = player.getUniqueId().toString();

                    plugin.playerMetaCache.getPlayerMeta(UUID).thenAccept(meta -> {
                        if (channel == null) {
                            bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("The" +
                                    " channel \"" + channelName + "\" doesn't exist", NamedTextColor.RED)));
                            return;
                        }

                        if (!members.containsKey(UUID)) {
                            ChannelMember owner = members.get(channel.getOwner());
                            TextComponent subtext = (!channel.isPub())
                                    ? Component.text("Please speak to " + owner.getName() + "to join.", NamedTextColor.RED)
                                    : Component.text("Join with /clanchat join " + channelName, NamedTextColor.RED);
                            bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You are" +
                                    " not a member of " + channelName + ". ").append(subtext)));
                            return;
                        }

                        if (meta.getDefaultChannel() == null || !meta.getDefaultChannel().equals(channelName)) {
                            bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Your" +
                                    " default channel has been changed to " + channelName, NamedTextColor.BLUE)));
                        }

                        meta.setDefaultChannel(channelName);
                        plugin.playerMetaCache.updatePlayerMeta(UUID, meta);
                    });
                } catch(Exception exception) {
                    bukkitScheduler.runTask(plugin, () -> {
                        sender.sendMessage(Component.text("There was an error setting a player's default channel.", NamedTextColor.RED));
                        plugin.log(exception.toString(), Level.SEVERE);
                    });
                    if(Thread.currentThread().isInterrupted()) {
                        Thread.currentThread().interrupt();
                    }
                }
            });

        }
    }


    private CompletableFuture<String> getDefaultChannel(CommandSender sender) {
        if (sender instanceof Player player) {
            String UUID = player.getUniqueId().toString();
            return plugin.playerMetaCache.getPlayerMeta(UUID).thenApply(meta -> {
                if (meta != null && meta.getDefaultChannel() != null) {
                    return meta.getDefaultChannel();
                } else {
                    return null;
                }
            });
        } else {
            return null;
        }
    }


    private void updateLastChannelReceived(Player player, String channel) {
        String UUID = player.getUniqueId().toString();
        plugin.playerMetaCache.getPlayerMeta(UUID).thenAccept(meta -> {
            meta.setLastReceived(channel);
            plugin.playerMetaCache.updatePlayerMeta(UUID, meta);
        });
    }


    private CompletableFuture<String> getLastChannelReceived(Player player) {
        String UUID = player.getUniqueId().toString();
        return plugin.playerMetaCache.getPlayerMeta(UUID).thenApply(PlayerMeta::getLastReceived);
    }

    private CompletableFuture<Boolean> assertManager(CommandSender sender, String channelName) {
        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName.toLowerCase());
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName.toLowerCase());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(channelFuture, membersFuture);

        return allFutures.thenApply(v -> {
            try {

                Channel channel = channelFuture.get();
                HashMap<String, ChannelMember> members = membersFuture.get();

                TextComponent errorMsg = Component.text("Sorry, you have to be a manager to do that!", NamedTextColor.RED);
                if (!(sender instanceof Player player)) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(errorMsg));
                    return false;
                }
                String UUID = player.getUniqueId().toString();
                if (!(members.containsKey(UUID))) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(errorMsg));
                    return false;
                }
                if (!members.get(UUID).isManager() && !channel.getOwner().equals(UUID)) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(errorMsg));
                    return false;
                }
                return true;
            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error setting a player's default channel.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
                return false;
            }
        });
    }

}
