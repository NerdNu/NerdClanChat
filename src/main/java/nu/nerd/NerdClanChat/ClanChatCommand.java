package nu.nerd.NerdClanChat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import nu.nerd.NerdClanChat.database.Bulletin;
import nu.nerd.NerdClanChat.database.Channel;
import nu.nerd.NerdClanChat.database.ChannelMember;
import nu.nerd.NerdClanChat.database.Invite;
import nu.nerd.NerdClanChat.database.PlayerMeta;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;


public class ClanChatCommand implements TabExecutor {


    private final NerdClanChat plugin;
    private HashMap<String, String> confirmChannelDeletion;
    private enum ChannelColor { COLOR, TEXTCOLOR, ALERTCOLOR }
    BukkitScheduler bukkitScheduler = Bukkit.getScheduler();

    private static final LinkedHashSet<String> SUBCOMMANDS = new LinkedHashSet<>(Arrays.asList(
        "addbulletin", "addmanager", "alertcolor", "changeowner", "channels", "color", "confirm",
        "create", "delete", "flags", "invite", "join", "leave", "list", "listmanagers", "members",
        "more", "public", "reloadconfig", "remove", "removebulletin", "removemanager", "subscribe",
        "subscriptions", "textcolor", "uninvite", "unsubscribe"
    ));

    private static final LinkedHashSet<String> FLAGS = new LinkedHashSet<>(Arrays.asList(
        "public", "secret"
    ));

    private static final LinkedHashSet<String> COLORS = new LinkedHashSet<>();

    public ClanChatCommand(NerdClanChat plugin) {
        this.plugin = plugin;
        this.confirmChannelDeletion = new HashMap<>();
        NamedTextColor.NAMES.keys().stream()
                .sorted()
                .forEachOrdered(COLORS::add);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (args == null || args.length == 0) {

            completions.addAll(SUBCOMMANDS);

        } else if (args.length == 1) {

            String arg = args[0];
            if (arg.isEmpty()) {
                completions.addAll(SUBCOMMANDS);
            } else {
                SUBCOMMANDS.stream()
                           .filter(s -> s.startsWith(arg))
                           .forEach(completions::add);
            }

        } else if (args.length == 3) {

            String arg = args[0];
            if (arg.equalsIgnoreCase("color") || arg.equalsIgnoreCase("textcolor") || arg.equalsIgnoreCase("alertcolor")) {

                String color = args[2];
                if (color.isEmpty()) {
                    completions.addAll(COLORS);
                } else {
                    COLORS.stream()
                          .filter(s -> s.startsWith(color))
                          .forEach(completions::add);
                }

            } else if (arg.equalsIgnoreCase("flags")) {

                String flag = args[2];
                if (flag.isEmpty()) {
                    completions.addAll(FLAGS);
                } else {
                    FLAGS.stream()
                         .filter(s -> s.startsWith(flag))
                         .forEach(completions::add);
                }

            }

        }

        return completions;
    }

    /**
     * The method for grabbing which subcommand is run for /clanchat. Calls the appropriate method.
     * @param sender The player sending the command
     * @param cmd The command being run
     * @param args The command arguments
     * @return Whether or not the command was run successfully
     */
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (args.length < 1) {
            this.printHelpText(sender);
        } else {
            switch(args[0].toLowerCase()) {
                case "more":
                    this.printMoreHelpText(sender);
                    break;

                case "create":
                    if(args.length > 1) {
                        this.createChannel(sender, args[1]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat create <channel>", NamedTextColor.RED));
                    }
                    break;

                case "delete":
                    if(args.length > 1) {
                        this.deleteChannel(sender, args[1]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat delete <channel>", NamedTextColor.RED));
                    }
                    break;

                case "confirm":
                    if(args.length == 2 && args[1].equalsIgnoreCase("delete")) {
                        this.actuallyDeleteChannel(sender);
                    }
                    break;

                case "color":
                    if(args.length == 3) {
                        this.setChannelColor(sender, args[1], args[2], ChannelColor.COLOR);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat color <channel> <color>", NamedTextColor.RED));
                        sendAllowedColours(sender);
                    }
                    break;

                case "textcolor":
                    if(args.length == 3) {
                        this.setChannelColor(sender, args[1], args[2], ChannelColor.TEXTCOLOR);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat textcolor <channel> <color>", NamedTextColor.RED));
                        sendAllowedColours(sender);
                    }
                    break;

                case "alertcolor":
                    if (args.length == 3) {
                        this.setChannelColor(sender, args[1], args[2], ChannelColor.ALERTCOLOR);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat alertcolor <channel> <color>", NamedTextColor.RED));
                        sendAllowedColours(sender);
                    }
                    break;

                case "members":
                    if (args.length == 2) {
                        this.listChannelMembers(sender, args[1]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat members <channel>", NamedTextColor.RED));
                    }
                    break;

                case "invite":
                    if (args.length == 3) {
                        this.inviteMember(sender, args[1], args[2]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat invite <channel> <player>", NamedTextColor.RED));
                    }
                    break;

                case "uninvite":
                    if (args.length == 3) {
                        this.uninviteMember(sender, args[1], args[2]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat uninvite <channel> <player>", NamedTextColor.RED));
                    }
                    break;

                case "changeowner":
                    if (args.length == 3) {
                        this.changeChannelOwner(sender, args[1], args[2]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat changeowner <channel> <player>", NamedTextColor.RED));
                    }
                    break;

                case "addmanager":
                    if (args.length == 3) {
                        this.addChannelManager(sender, args[1], args[2]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat addmanager <channel> <player>", NamedTextColor.RED));
                    }
                    break;

                case "removemanager":
                    if (args.length == 3) {
                        this.removeChannelManager(sender, args[1], args[2]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat removemanager <channel> <player>", NamedTextColor.RED));
                    }
                    break;

                case "listmanagers":
                    if (args.length == 2) {
                        this.listChannelManagers(sender, args[1]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat listmanagers <channel>", NamedTextColor.RED));
                    }
                    break;

                case "remove":
                    if (args.length == 3) {
                        this.removeMemberFromChannel(sender, args[1], args[2]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat remove <channel> <player>", NamedTextColor.RED));
                    }
                    break;

                case "join":
                    if (args.length == 2) {
                        this.joinChannel(sender, args[1]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat join <channel>", NamedTextColor.RED));
                    }
                    break;

                case "leave":
                    if (args.length == 2) {
                        this.leaveChannel(sender, args[1]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat leave <channel>", NamedTextColor.RED));
                    }
                    break;

                case "list":
                    this.listChannels(sender);
                    break;

                case "channels":
                    this.listAllChannels(sender);
                    break;

                case "public":
                    this.listAllPublicChannels(sender);
                    break;

                case "addbulletin":
                    if (args.length > 2) {
                        this.addBulletin(sender, args);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat addbulletin <channel> <bulletin>", NamedTextColor.RED));
                    }
                    break;

                case "removebulletin":
                    if (args.length == 3) {
                        this.removeBulletin(sender, args[1], args[2]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat removebulletin <channel> <number>", NamedTextColor.RED));
                        sender.sendMessage(Component.text("The <number> field starts at 1 from the top bulletin.", NamedTextColor.RED));
                    }
                    break;

                case "subscribe":
                    if (args.length == 2) {
                        this.subscribeToBulletins(sender, args[1]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat subscribe <channel>", NamedTextColor.RED));
                    }
                    break;

                case "unsubscribe":
                    if (args.length == 2) {
                        this.unsubscribeFromBulletins(sender, args[1]);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat unsubscribe <channel>", NamedTextColor.RED));
                    }
                    break;

                case "subscriptions":
                    this.listSubscriptions(sender);
                    break;

                case "chat":
                    if (sender instanceof Player) {
                        sender.sendMessage(Component.text("Sorry, you can't use this command", NamedTextColor.RED));
                        return true;
                    }
                    if (args.length > 2) {
                        this.consoleChat(sender, args);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat chat <channel> <message>", NamedTextColor.RED));
                    }
                    break;

                case "flags":
                    if (args.length == 4) {
                        this.setFlags(sender, args);
                    } else {
                        sender.sendMessage(Component.text("Usage: /clanchat flags <channel> <flag> <boolean>", NamedTextColor.RED));
                        sender.sendMessage("Two flags are supported: 'public' and 'secret'.");
                        sender.sendMessage("A 'public' channel can be joined by anyone, without invite.");
                        sender.sendMessage("A 'secret' channel requires membership to view channel members.");
                        sender.sendMessage("A boolean is either the word 'true' or 'false'.");
                    }
                    break;

                case "reloadconfig":
                    if (sender.hasPermission("nerdclanchat.admin")) {
                        plugin.config.load();
                        sender.sendMessage(Component.text("Configuration reloaded.", NamedTextColor.BLUE));
                    } else {
                        sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
                    }
                    break;

                default:
                    this.printHelpText(sender);
                    break;
            }
        }
        return true;
    }

/*
                                                 Create Channel
------------------------------------------------------------------------------------------------------------------------
 */

    private void createChannel(CommandSender sender, String name) {
        if (!(sender instanceof Player owner)) {
            sender.sendMessage(Component.text("This command can only be run by a player.", NamedTextColor.RED));
            return;
        }
        name = name.toLowerCase();
        String finalName = name;
        plugin.channelCache.getChannel(name).thenAccept(channel -> {
            if (channel != null) {
                sender.sendMessage(Component.text("That channel already exists. If you would like to join the" +
                        " channel, speak with the owner.", NamedTextColor.RED));
            } else {

                if (!finalName.matches("^(?i)[a-z0-9_]+$")) {
                    sender.sendMessage(Component.text("Oops! That channel name isn't valid. You can only have" +
                            " letters and numbers in your channel name.", NamedTextColor.RED));
                    return;
                }
                if (finalName.length() > 16) {
                    sender.sendMessage(Component.text("Oops! Please limit your channel name length to 16 characters!",
                            NamedTextColor.RED));
                    return;
                }

                //Create the channel
                Channel ch = new Channel(finalName, owner.getUniqueId().toString());
                ChannelMember mem = new ChannelMember(finalName, owner.getUniqueId().toString(), owner.getName(), true);
                try {
                    plugin.channelsTable.save(ch);
                    plugin.channelMembersTable.save(mem);
                } catch (Exception ex) {
                    sender.sendMessage(Component.text("There was an error creating the channel.", NamedTextColor.RED));
                    plugin.getLogger().warning(ex.toString());
                    return;
                }

                //Cache the newly created channel
                HashMap<String, ChannelMember> members = new HashMap<String, ChannelMember>();
                members.put(owner.getUniqueId().toString(), mem);
                plugin.channelCache.updateChannel(finalName, ch);
                plugin.channelCache.updateChannelMembers(finalName, members);

                //Update owner's player meta to make this their default channel
                String UUID = owner.getUniqueId().toString();
                plugin.playerMetaCache.getPlayerMeta(UUID).thenAccept(playerMeta -> {
                    playerMeta.setDefault_channel(finalName);
                    plugin.playerMetaCache.updatePlayerMeta(UUID, playerMeta);

                    bukkitScheduler.runTask(plugin, () -> {
                        sender.sendMessage(Component.text("You will receive bulletins from this channel on login." +
                                " To unsubscribe run /clanchat unsubscribe " + finalName, NamedTextColor.BLUE));
                        sender.sendMessage(Component.text("Channel created!", NamedTextColor.LIGHT_PURPLE));
                    });
                });
            }
        });
    }

/*
                                                 Delete Channel
------------------------------------------------------------------------------------------------------------------------
 */

    private void deleteChannel(CommandSender sender, String name) {

        this.senderIsOwner(sender, name, false).thenAccept(isOwner -> {
            if (!isOwner) {
                bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Only the owner can" +
                        " delete a channel", NamedTextColor.RED)));
                return;
            }

            Player owner = (Player) sender;
            this.confirmChannelDeletion.put(owner.getUniqueId().toString(), name.toLowerCase());
            bukkitScheduler.runTask(plugin, () -> {
                sender.sendMessage(Component.text("This command is irreversible. Your channel and all associated" +
                        " data WILL Be lost!", NamedTextColor.RED));
                sender.sendMessage(Component.text("Type \"/clanchat confirm delete\" to confirm you actually want" +
                        " to delete #" + name, NamedTextColor.BLUE));
            });
        });
    }

/*
                                              Actually Delete Channel
------------------------------------------------------------------------------------------------------------------------
 */

    private void actuallyDeleteChannel(CommandSender sender) {
        if (sender instanceof Player owner) {

            String ownerUUID = owner.getUniqueId().toString();
            String channelName;

            if (!this.confirmChannelDeletion.containsKey(ownerUUID)) {
                return;
            } else {
                channelName = this.confirmChannelDeletion.get(ownerUUID);
                this.confirmChannelDeletion.remove(ownerUUID);
            }

            try {
                plugin.channelCache.getChannel(channelName).thenAccept(channel -> {
                    plugin.channelsTable.delete(channel);
                    plugin.channelMembersTable.deleteChannelMembers(channelName);
                    plugin.bulletinsTable.deleteChannelBulletins(channelName);
                    plugin.invitesTable.deleteChannelInvites(channelName);
                    plugin.channelCache.remove(channelName);
                });
            } catch (Exception ex) {
                plugin.getLogger().warning(ex.toString());
                sender.sendMessage(Component.text("There was an error deleting your channel.", NamedTextColor.RED));
                return;
            }

            sender.sendMessage(Component.text("Your channel was deleted!", NamedTextColor.RED));

        }
    }

/*
                                               Set Channel Color
------------------------------------------------------------------------------------------------------------------------
 */

    private void setChannelColor(CommandSender sender, String channelName, String color, ChannelColor key) {

        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName);
        CompletableFuture<Boolean> isManagerFuture = this.senderIsManager(sender, channelName, false);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(channelFuture, isManagerFuture);

        channelName = channelName.toLowerCase();
        color = color.toUpperCase();
        String finalChannelName = channelName;
        String finalColor = color;
        allFutures.thenRun(() -> {
           try {

               Channel channel = channelFuture.get();
               boolean isManager = isManagerFuture.get();

               if (!isManager) {
                   bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Sorry, you have to" +
                           " be a manager to do that!", NamedTextColor.RED)));
                   return;
               }

               if (channel == null) {
                   bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("The channel \""+
                           finalChannelName + "\" doesn't exist.", NamedTextColor.RED)));
                   return;
               }

               if(finalColor.charAt(0) != '#') {
                   if (!NCCUtil.colorList().contains(finalColor)) {
                       bukkitScheduler.runTask(plugin, () -> sendAllowedColours(sender));
                       return;
                   }
               } else {
                   if(!finalColor.toUpperCase().matches("^#[0-9A-F]{6}$")) {
                       bukkitScheduler.runTask(plugin, () -> sendAllowedColours(sender));
                       return;
                   }
               }

               if (key == ChannelColor.ALERTCOLOR) {
                   channel.setAlert_color(finalColor);
               } else if (key == ChannelColor.TEXTCOLOR) {
                   channel.setText_color(finalColor);
               } else {
                   channel.setColor(finalColor);
               }

               plugin.channelsTable.save(channel);
               plugin.channelCache.updateChannel(finalChannelName, channel);

               TextColor displayColor = NCCUtil.color(finalColor);

               bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Channel " +
                               key.name().toLowerCase() + " changed to ", NamedTextColor.BLUE)
                       .append(Component.text(finalColor, displayColor))));

           } catch(Exception exception) {
               bukkitScheduler.runTask(plugin, () -> {
                   sender.sendMessage(Component.text("There was an error updating the channel color.", NamedTextColor.RED));
                   plugin.log(exception.toString(), Level.SEVERE);
               });
               if(Thread.currentThread().isInterrupted()) {
                   Thread.currentThread().interrupt();
               }
           }
        });
    }

/*
                                               List Channel Members
------------------------------------------------------------------------------------------------------------------------
 */

    private void listChannelMembers(CommandSender sender, String channelName) {

        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName.toLowerCase());
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        CompletableFuture<Boolean> isMemberFuture = this.senderIsMember(sender, channelName);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(channelFuture, membersFuture, isMemberFuture);

        allFutures.thenRun(() -> {
            try {

                Channel channel = channelFuture.get();
                HashMap<String, ChannelMember> channelMembers = membersFuture.get();
                boolean isMember = isMemberFuture.get();

                if (channel == null) {
                    sender.sendMessage(Component.text("That channel does not exist", NamedTextColor.RED));
                    return;
                }

                if (!isMember && channel.isSecret()) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("This channel is" +
                            " secret. You must be a member of the channel to see who is in the channel", NamedTextColor.RED)));
                    return;
                }

                // Collect players into online and offline lists
                List<String> online = new ArrayList<>();
                List<String> offline = new ArrayList<>();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (channelMembers.containsKey(player.getUniqueId().toString())) {
                        online.add(player.getName());
                    }
                }
                for (ChannelMember member : channelMembers.values()) {
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
                    sender.sendMessage(Component.text("There was an listing the channel's members.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

/*
                                                   Invite Member
------------------------------------------------------------------------------------------------------------------------
 */

    private void inviteMember(CommandSender sender, String channelName, String playerName) {

        CompletableFuture<PlayerMeta> playerMetaFuture = plugin.playerMetaCache.getPlayerMetaByName(playerName.toLowerCase());
        CompletableFuture<Boolean> isManagerFuture = this.senderIsManager(sender, channelName, false);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(playerMetaFuture, isManagerFuture);

        allFutures.thenRun(() -> {
            try {
                PlayerMeta playerMeta = playerMetaFuture.get();
                boolean isManager = isManagerFuture.get();
                String lowChannelName = channelName.toLowerCase();
                System.out.println(lowChannelName);

                if (!isManager) {
                    sender.sendMessage(Component.text("Sorry, you have to be a manager to do that!", NamedTextColor.RED));
                    return;
                }

                plugin.invitesTable.alreadyInvited(playerMeta.getUuid(), lowChannelName).thenAccept(invitedBoolean -> {
                    if (playerMeta == null) {
                        bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Sorry," +
                                " but that player hasn't logged on recently. Try again later.", NamedTextColor.RED)));
                        return;
                    }

                    if (invitedBoolean) {
                        bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("That player" +
                                " was already invited, but they haven't accepted yet", NamedTextColor.BLUE)));
                        return;
                    }

                    Invite inv = new Invite(lowChannelName, playerMeta.getUuid());
                    plugin.invitesTable.save(inv);

                    Player player = plugin.getServer().getPlayer(UUID.fromString(playerMeta.getUuid()));
                    if (player != null) {
                        bukkitScheduler.runTask(plugin, () -> {
                            player.sendMessage(Component.text("You have been invited to " + lowChannelName +
                                    " by " + sender.getName(), NamedTextColor.BLUE));
                            player.sendMessage(Component.text("Type ", NamedTextColor.BLUE)
                                    .append(Component.text("/clanchat join " + lowChannelName, NamedTextColor.GRAY))
                                    .append(Component.text(" to join", NamedTextColor.BLUE)));
                        });
                    }

                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text(playerName +
                            " has been invited to " + lowChannelName, NamedTextColor.BLUE)));
                });
            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error processing the invite.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

/*
                                                   Uninvite Member
------------------------------------------------------------------------------------------------------------------------
 */

    private void uninviteMember(CommandSender sender, String channelName, String playerName) {

        CompletableFuture<PlayerMeta> playerMetaFuture = plugin.playerMetaCache.getPlayerMetaByName(playerName.toLowerCase());
        CompletableFuture<Boolean> isManagerFuture = this.senderIsManager(sender, channelName, false);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(playerMetaFuture, isManagerFuture);

        channelName = channelName.toLowerCase();
        String finalChannelName = channelName;
        allFutures.thenRun(() -> {
           try {

               PlayerMeta playerMeta = playerMetaFuture.get();
               boolean isManager = isManagerFuture.get();

               if (!isManager) {
                   bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Sorry, you have to" +
                           " be a manager to do that!", NamedTextColor.RED)));
                   return;
               }

               plugin.invitesTable.alreadyInvited(playerMeta.getUuid(), finalChannelName).thenAccept(invitedBoolean -> {
                   if (!invitedBoolean) {
                       bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("That player" +
                               " isn't in the invite list.", NamedTextColor.RED)));
                       return;
                   }

                   plugin.invitesTable.closeInvitation(playerMeta.getUuid(), finalChannelName);

                   bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Player removed from" +
                           " the invite list", NamedTextColor.BLUE)));
               });
           } catch(Exception exception) {
               bukkitScheduler.runTask(plugin, () -> {
                   sender.sendMessage(Component.text("There was an error uninviting a player.", NamedTextColor.RED));
                   plugin.log(exception.toString(), Level.SEVERE);
               });
               if(Thread.currentThread().isInterrupted()) {
                   Thread.currentThread().interrupt();
               }
           }
        });
    }

/*
                                               Change Channel Owner
------------------------------------------------------------------------------------------------------------------------
 */

    private void changeChannelOwner(CommandSender sender, String channelName, String newOwner) {

        channelName = channelName.toLowerCase();
        CompletableFuture<PlayerMeta> newOwnerMetaFuture = plugin.playerMetaCache.getPlayerMetaByName(newOwner.toLowerCase());
        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName);
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName);
        CompletableFuture<Boolean> isOwnerFuture = this.senderIsOwner(sender, channelName, false);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(newOwnerMetaFuture, channelFuture, membersFuture, isOwnerFuture);

        String finalChannelName = channelName;
        allFutures.thenRun(() -> {
            try {

                PlayerMeta newOwnerMeta = newOwnerMetaFuture.get();
                Channel channel = channelFuture.get();
                HashMap<String, ChannelMember> members = membersFuture.get();
                boolean isOwner = isOwnerFuture.get();

                if (!isOwner && !sender.hasPermission("nerdclanchat.admin")) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Only the owner can" +
                            " set a new owner for the channel", NamedTextColor.RED)));
                    return;
                }

                if (newOwnerMeta == null || !members.containsKey(newOwnerMeta.getUuid())) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("The new owner" +
                            " must be a member of the channel.", NamedTextColor.RED)));
                    return;
                }
                // Make the old owner a manager, if they're not already
                ChannelMember oldOwnerMember = members.get(channel.getOwner());
                oldOwnerMember.setManager(true);
                members.put(channel.getOwner(), oldOwnerMember);
                plugin.channelMembersTable.save(oldOwnerMember);

                // Change the channel owner
                channel.setOwner(newOwnerMeta.getUuid());
                plugin.channelsTable.save(channel);

                // Ensure the new owner is a manager, for consistency
                ChannelMember newOwnerMember = members.get(newOwnerMeta.getUuid());
                newOwnerMember.setManager(true);
                members.put(newOwnerMeta.getUuid(), newOwnerMember);
                plugin.channelMembersTable.save(newOwnerMember);

                // Update cache
                plugin.channelCache.updateChannel(finalChannelName, channel);
                plugin.channelCache.updateChannelMembers(finalChannelName, members);

                bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You have relinquished" +
                        " ownership of " + finalChannelName + " to " + newOwner, NamedTextColor.BLUE)));
            } catch (Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error changing the channel owner.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

/*
                                               Add Channel Manager
------------------------------------------------------------------------------------------------------------------------
 */

    private void addChannelManager(CommandSender sender, String channelName, String playerName) {

        channelName = channelName.toLowerCase();
        CompletableFuture <PlayerMeta> managerMetaFuture = plugin.playerMetaCache.getPlayerMetaByName(playerName);
        CompletableFuture <HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName);
        CompletableFuture<Boolean> isOwnerFuture = this.senderIsOwner(sender, channelName, false);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(managerMetaFuture, membersFuture, isOwnerFuture);

        String finalChannelName = channelName;
        allFutures.thenRun(() -> {
            try {

                PlayerMeta managerMeta = managerMetaFuture.get();
                HashMap<String, ChannelMember> members = membersFuture.get();
                boolean isOwner = isOwnerFuture.get();

                if (!isOwner) {
                    sender.sendMessage(Component.text("Only the owner may add or remove managers", NamedTextColor.RED));
                    return;
                }

                if (managerMeta == null || !members.containsKey(managerMeta.getUuid())) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Only members" +
                            " can be made managers. Invite them to the channel, and wait for them to join first.",
                            NamedTextColor.RED)));
                    return;
                }

                if (members.get(managerMeta.getUuid()).isManager()) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text(playerName +
                            " is already a manager", NamedTextColor.RED)));
                    return;
                }

                ChannelMember cm = members.get(managerMeta.getUuid());
                cm.setManager(true);
                members.put(cm.getUuid(), cm);
                plugin.channelMembersTable.save(cm);
                plugin.channelCache.updateChannelMembers(finalChannelName, members);
                bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text(playerName +
                        " added as a manager!", NamedTextColor.BLUE)));

                Player player = plugin.getServer().getPlayer(UUID.fromString(managerMeta.getUuid()));
                if (player != null) {
                    bukkitScheduler.runTask(plugin, () -> player.sendMessage(Component.text("You have been" +
                            " made a manager in " + finalChannelName, NamedTextColor.BLUE)));
                }
            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error updating the manager status.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });

    }

/*
                                               Remove Channel Manager
------------------------------------------------------------------------------------------------------------------------
 */

    private void removeChannelManager(CommandSender sender, String channelName, String playerName) {

        channelName = channelName.toLowerCase();
        CompletableFuture<PlayerMeta> managerMetaFuture = plugin.playerMetaCache.getPlayerMetaByName(playerName);
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName);
        CompletableFuture<Boolean> isOwnerFuture = this.senderIsOwner(sender, channelName, false);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(managerMetaFuture, membersFuture, isOwnerFuture);

        String finalChannelName = channelName;
        allFutures.thenRun(() -> {
            try {

                PlayerMeta managerMeta = managerMetaFuture.get();
                HashMap<String, ChannelMember> members = membersFuture.get();
                boolean isOwner = isOwnerFuture.get();

                if (!isOwner) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Only the owner may" +
                            " add or remove managers", NamedTextColor.RED)));
                    return;
                }

                if (managerMeta == null || !members.containsKey(managerMeta.getUuid()) || !members.get(managerMeta.getUuid()).isManager()) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text(playerName + " is not" +
                            " a manager in " + finalChannelName, NamedTextColor.RED)));
                    return;
                }
                ChannelMember cm = members.get(managerMeta.getUuid());
                cm.setManager(false);
                members.put(cm.getUuid(), cm);
                plugin.channelMembersTable.save(cm);
                plugin.channelCache.updateChannelMembers(finalChannelName, members);

                sender.sendMessage(Component.text(playerName + " removed as a manager from " + finalChannelName,
                        NamedTextColor.BLUE));

            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error updating the manager status.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });

    }

/*
                                               List Channel Managers
------------------------------------------------------------------------------------------------------------------------
 */

    private void listChannelManagers(CommandSender sender, String channelName) {

        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName.toLowerCase());
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName);
        CompletableFuture<Boolean> isMemberFuture = this.senderIsMember(sender, channelName);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(channelFuture, membersFuture, isMemberFuture);

        allFutures.thenRun(() -> {
            try {

                Channel channel = channelFuture.get();
                HashMap<String, ChannelMember> members = membersFuture.get();
                boolean isMember = isMemberFuture.get();

                if ((!isMember) && channel.isSecret()) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("This channel" +
                            " is secret. You must be a member of the channel to see who is in the channel", NamedTextColor.RED)));
                    return;
                }

                List<String> online = new ArrayList<>();
                List<String> offline = new ArrayList<>();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (members.containsKey(player.getUniqueId().toString()) && members.get(player.getUniqueId().toString()).isManager()) {
                            online.add(player.getName());
                        }

                }
                for (ChannelMember member : members.values()) {
                    if (member.isManager() && !online.contains(member.getName())) {
                            offline.add(member.getName());
                        }
                }

                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("Online: ", NamedTextColor.GOLD)
                            .append(NCCUtil.formatList(online, NamedTextColor.WHITE, NamedTextColor.GRAY)));
                    sender.sendMessage(Component.text("Offline: ", NamedTextColor.GOLD)
                            .append(NCCUtil.formatList(offline, NamedTextColor.WHITE, NamedTextColor.GRAY)));
                });
            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error listing the channel managers", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

/*
                                             Remove Member From Channel
------------------------------------------------------------------------------------------------------------------------
 */

    private void removeMemberFromChannel(CommandSender sender, String channelName, String playerName) {

        CompletableFuture<PlayerMeta> playerMetaFuture = plugin.playerMetaCache.getPlayerMetaByName(playerName);
        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName);
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName);
        CompletableFuture<Boolean> isManagerFuture = this.senderIsManager(sender, channelName, false);
        CompletableFuture<Boolean> isOwnerFuture = this.senderIsOwner(sender, channelName, false);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(playerMetaFuture, channelFuture, membersFuture);

        allFutures.thenRun(() -> {
           try {

               PlayerMeta playerMeta = playerMetaFuture.get();
               Channel channel = channelFuture.get();
               HashMap<String, ChannelMember> members = membersFuture.get();
               boolean isManager = isManagerFuture.get();
               boolean isOwner = isOwnerFuture.get();

               if (!isManager) {
                   sender.sendMessage(Component.text("Sorry, you have to be a manager to do that!", NamedTextColor.RED));
                   return;
               }

               ChannelMember member;

               if (playerMeta != null && members.containsKey(playerMeta.getUuid())) {
                   member = members.get(playerMeta.getUuid());
               } else {
                   bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("That player" +
                           " isn't a member.", NamedTextColor.RED)));
                   return;
               }

               if (member.getUuid().equals(channel.getOwner())) {
                   bukkitScheduler.runTask(plugin, () ->sender.sendMessage(Component.text("You cannot remove" +
                           " the owner from a channel!", NamedTextColor.RED)));
                   return;
               }

               if (!isOwner && member.isManager()) {
                   bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Only the owner" +
                           " can remove a manager", NamedTextColor.RED)));
                   return;
               }

               plugin.channelMembersTable.delete(member);
               members.remove(member.getUuid());
               plugin.channelCache.updateChannelMembers(channelName, members);
               bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Member removed.", NamedTextColor.BLUE)));

           } catch(Exception exception) {
               bukkitScheduler.runTask(plugin, () -> {
                   sender.sendMessage(Component.text("There was an error removing the channel member.", NamedTextColor.RED));
                   plugin.log(exception.toString(), Level.SEVERE);
               });
               if(Thread.currentThread().isInterrupted()) {
                   Thread.currentThread().interrupt();
               }
           }
        });
    }

/*
                                                    Join Channel
------------------------------------------------------------------------------------------------------------------------
 */

    private void joinChannel(CommandSender sender, String channelName) {

        if (!(sender instanceof Player player)) return;

        channelName = channelName.toLowerCase();
        String UUID = player.getUniqueId().toString();
        CompletableFuture<PlayerMeta> metaFuture = plugin.playerMetaCache.getPlayerMeta(UUID);
        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName);
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName);
        CompletableFuture<Boolean> alreadyInvitedFuture = plugin.invitesTable.alreadyInvited(UUID, channelName);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(metaFuture, channelFuture, membersFuture, alreadyInvitedFuture);

        String finalChannelName = channelName.toLowerCase();
        allFutures.thenRun(() -> {
            try {

                PlayerMeta meta = metaFuture.get();
                Channel channel = channelFuture.get();
                HashMap<String, ChannelMember> members = membersFuture.get();
                boolean alreadyInvited = alreadyInvitedFuture.get();

                if (members.containsKey(UUID)) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You're already" +
                            " a member of this channel!", NamedTextColor.RED)));
                    return;
                }

                if (channel == null) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("That channel" +
                            " doesn't exist...yet.", NamedTextColor.RED)));
                    return;
                }

                if (!channel.isPub() && !alreadyInvited) {
                    ChannelMember owner = members.get(channel.getOwner());
                    List<String> managers = new ArrayList<>();
                    for (ChannelMember m : members.values()) {
                        if (m.isManager()) managers.add(m.getName());
                    }
                    bukkitScheduler.runTask(plugin, () -> {
                        sender.sendMessage(Component.text("You can't join a non-public channel without an invite." +
                                " Please speak with a channel owner or manager to join.", NamedTextColor.RED));
                        sender.sendMessage(Component.text("Owner: ", NamedTextColor.GOLD)
                                .append(Component.text(owner.getName(), NamedTextColor.GRAY)));
                        if (!channel.isSecret()) {
                            sender.sendMessage(Component.text("Managers: ", NamedTextColor.GOLD)
                                    .append(NCCUtil.formatList(managers, NamedTextColor.GRAY, NamedTextColor.WHITE)));
                        }
                    });
                    return;
                }

                ChannelMember mem = new ChannelMember(finalChannelName, UUID, player.getName(), false);
                members.put(UUID, mem);
                plugin.channelMembersTable.save(mem);
                plugin.channelCache.updateChannelMembers(finalChannelName, members);
                plugin.invitesTable.closeInvitation(UUID, finalChannelName);

                meta.setDefault_channel(finalChannelName);
                plugin.playerMetaCache.updatePlayerMeta(UUID, meta);

                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("You will receive bulletins from this channel on login. To" +
                            " unsubscribe run /clanchat unsubscribe " + finalChannelName, NamedTextColor.BLUE));

                    sender.sendMessage(Component.text("Type ", NamedTextColor.BLUE)
                            .append(Component.text("/c #" + finalChannelName + " <msg>", NamedTextColor.GRAY))
                            .append(Component.text(" to say something to this channel, or just ", NamedTextColor.BLUE))
                            .append(Component.text(" /c <msg>", NamedTextColor.GRAY))
                            .append(Component.text(" if this is already your default channel", NamedTextColor.BLUE)));

                    this.sendRawMessage(finalChannelName, Component.text(player.getName(), NamedTextColor.RED)
                            .append(Component.text(" has joined ", NamedTextColor.BLUE))
                            .append(Component.text(finalChannelName, NamedTextColor.NAMES.value(channel.getColor())))
                            .append(Component.text(", say hi!", NamedTextColor.BLUE)));
                });

            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error joining the channel.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

/*
                                                    Leave Channel
------------------------------------------------------------------------------------------------------------------------
 */

    private void leaveChannel(CommandSender sender, String channelName) {

        if (!(sender instanceof Player player)) return;

        channelName = channelName.toLowerCase();
        String UUID = player.getUniqueId().toString();
        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName);
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(channelFuture, membersFuture);

        String finalChannelName = channelName;
        allFutures.thenRun(() -> {
            try {

                Channel channel = channelFuture.get();
                HashMap<String, ChannelMember> members = membersFuture.get();

                if (!members.containsKey(UUID)) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You can't leave" +
                            " a channel you're not in", NamedTextColor.RED)));
                    return;
                }

                if (channel.getOwner().equals(UUID) && members.size() > 1) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("The owner can't" +
                            " leave their channel unless the channel is empty. Please set someone else as owner first," +
                            " or use \"/clanchat delete <channel>\" to remove the channel.", NamedTextColor.RED)));
                }

                // Leave the channel
                plugin.channelMembersTable.delete(members.get(UUID));
                members.remove(UUID);
                plugin.channelCache.updateChannelMembers(finalChannelName, members);

                // Delete the channel if it's empty when the owner leaves it
                if (members.isEmpty()) {
                    plugin.channelsTable.delete(channel);
                    plugin.channelMembersTable.deleteChannelMembers(finalChannelName);
                    plugin.bulletinsTable.deleteChannelBulletins(finalChannelName);
                    plugin.invitesTable.deleteChannelInvites(finalChannelName);
                    plugin.channelCache.remove(finalChannelName);
                }

                bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You have been removed from "
                        + finalChannelName, NamedTextColor.BLUE)));
            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    plugin.getLogger().warning(exception.toString());
                    sender.sendMessage(Component.text("There was an error deleting your channel.", NamedTextColor.RED));
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

/*
                                                    List Channels
------------------------------------------------------------------------------------------------------------------------
 */

    private void listChannels(CommandSender sender) {

        if (!(sender instanceof Player player)) {
            bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Console can't join" +
                    " channels. Try \"/clanchat public\" to list all public channels.", NamedTextColor.RED)));
            return;
        }

        String UUID = player.getUniqueId().toString();
        plugin.transientPlayerCache.getChannelsForPlayer(UUID).thenAccept(channels -> {
            List<Channel> channelList = new ArrayList<>();

            if (channels.isEmpty()) {
                bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You aren't in any" +
                        " channels", NamedTextColor.RED)));
                return;
            }

            for (ChannelMember cm : channels) {
                plugin.channelCache.getChannel(cm.getChannel()).thenAccept(channelList::add);
            }
            bukkitScheduler.runTask(plugin, () -> sender.sendMessage(NCCUtil.formatChannelList(channelList)));
        });
    }

/*
                                                 List All Channels
------------------------------------------------------------------------------------------------------------------------
 */

    private void listAllChannels(CommandSender sender) {

        if (!sender.hasPermission("nerdclanchat.admin")) {
            sender.sendMessage(Component.text("You do not have permission to do this!", NamedTextColor.RED));
            return;
        }

        plugin.channelsTable.getAllChannels().thenCompose(channels -> {

            List<CompletableFuture<PlayerMeta>> ownerFutures = channels.stream()
                    .map(channel -> plugin.playerMetaCache.getPlayerMeta(channel.getOwner())).toList();

            return CompletableFuture.allOf(ownerFutures.toArray(new CompletableFuture[0]))
                    .thenApply(none -> {
                        List<PlayerMeta> owners = ownerFutures.stream().map(CompletableFuture::join).toList();
                        return Map.entry(channels, owners);
                    });
        }).thenAccept(entry -> {

            List<Channel> channels = entry.getKey();
            List<PlayerMeta> owners = entry.getValue();

            TextComponent channelList = Component.text("");

            for(int i = 0; i < channels.size(); i++) {
                Channel channel = channels.get(i);
                PlayerMeta owner = owners.get(i);

                channelList = channelList.append(Component.text(channel.getName(), NamedTextColor.BLUE));

                if (owner != null) {
                    channelList = channelList.append(Component.text(": ", NamedTextColor.WHITE))
                            .append(Component.text(owner.getName(), NamedTextColor.GRAY));
                }
                if (channels.indexOf(channel) != (channels.size() -1)) {
                    channelList = channelList.append(Component.text(", ", NamedTextColor.WHITE));
                }
            }
            TextComponent finalChannelList = channelList;
            bukkitScheduler.runTask(plugin, () -> sender.sendMessage(finalChannelList));
        });
    }

/*
                                              List All Public Channels
------------------------------------------------------------------------------------------------------------------------
 */

    private void listAllPublicChannels(CommandSender sender) {
        plugin.channelsTable.getAllChannels().thenAccept(channels -> {
            List<Channel> list = new ArrayList<>();
            for (Channel c : channels) {
                if (c.isPub()) {
                    list.add(c);
                }
            }
            bukkitScheduler.runTask(plugin, () -> {
                if (!list.isEmpty()) {
                    sender.sendMessage(NCCUtil.formatChannelList(list));
                } else {
                    sender.sendMessage(Component.text("There are no public channels yet.", NamedTextColor.RED));
                }
            });
        });
    }

/*
                                                   Add Bulletins
------------------------------------------------------------------------------------------------------------------------
 */

    private void addBulletin(CommandSender sender, String[] args) {

        String channelName = args[1].toLowerCase();
        String message = NCCUtil.joinArray(" ", Arrays.copyOfRange(args, 2, args.length));

        CompletableFuture<Boolean> isManagerFuture = this.senderIsManager(sender, channelName, false);
        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName);
        CompletableFuture<List<Bulletin>> bulletinsFuture = plugin.channelCache.getBulletins(channelName);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(isManagerFuture, channelFuture, bulletinsFuture);

        allFutures.thenRun(() -> {
            try {
                boolean isManager = isManagerFuture.get();
                Channel channel = channelFuture.get();
                List<Bulletin> bulletins = bulletinsFuture.get();

                if (!isManager) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Sorry, you have to" +
                            " be a manager to do that!", NamedTextColor.RED)));
                    return;
                }

                Bulletin nb = new Bulletin(channelName, message);
                bulletins.add(nb);
                plugin.bulletinsTable.save(nb);
                plugin.channelCache.updateBulletins(channelName, bulletins);

                bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Bulletin added" +
                        " successfully.", NamedTextColor.BLUE)));

                TextComponent msg = Component.text("");

                // Figure out which colour system is being used
                TextColor channelColor = NCCUtil.color(channel.getColor());
                TextColor channelAlertColor = NCCUtil.color(channel.getAlert_color());

                msg = msg.append(Component.text("[" + channelName + "] ", channelColor))
                        .append(Component.text(message, channelAlertColor));

                TextComponent finalMsg = msg;
                bukkitScheduler.runTask(plugin, () -> this.sendRawMessage(channelName, finalMsg));

            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error adding your bulletin.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

/*
                                                  Remove Bulletin
------------------------------------------------------------------------------------------------------------------------
 */

    private void removeBulletin(CommandSender sender, String channelName, String number) {

        CompletableFuture<Boolean> isManagerFuture = this.senderIsManager(sender, channelName, false);
        CompletableFuture<List<Bulletin>> bulletinsFuture = plugin.channelCache.getBulletins(channelName);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(isManagerFuture, bulletinsFuture);

        allFutures.thenRun(() -> {
            try {

                boolean isManager = isManagerFuture.get();
                List<Bulletin> bulletins = bulletinsFuture.get();

                if (!isManager) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Sorry, you have to" +
                            " be a manager to do that!", NamedTextColor.RED)));
                    return;
                }

                int index = Integer.parseInt(number);

                if (index < 1) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("The bulletin index" +
                            " specified must be a non-zero integer", NamedTextColor.RED)));
                    return;
                }

                if (index > bulletins.size()) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("There is no bulletin" +
                            " at that index", NamedTextColor.RED)));
                    return;
                }

                Bulletin rb = bulletins.get(index - 1);
                bulletins.remove(rb);
                plugin.bulletinsTable.delete(rb);
                plugin.channelCache.updateBulletins(channelName, bulletins);

                bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Bulletin successfully" +
                        " removed", NamedTextColor.BLUE)));
            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error removing your bulletin.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

/*
                                                Subscribe to Bulletins
------------------------------------------------------------------------------------------------------------------------
 */

    private void subscribeToBulletins(CommandSender sender, String channelName) {

        if (!(sender instanceof Player player)) return;

        CompletableFuture<Boolean> isMemberFuture = this.senderIsMember(sender, channelName);
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(isMemberFuture, membersFuture);

        allFutures.thenRun(() -> {
            try {

                boolean isMember = isMemberFuture.get();
                HashMap<String, ChannelMember> members = membersFuture.get();

                if (!isMember) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You must be a member" +
                            " of " + channelName + " to subscribe to their bulletins", NamedTextColor.RED)));
                    return;
                }
                String UUID = player.getUniqueId().toString();
                ChannelMember member = members.get(UUID);

                if (member.isSubscribed()) {
                    bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You are already subscribed" +
                            " to that channel", NamedTextColor.RED)));
                    return;
                }

                member.setSubscribed(true);
                members.put(UUID, member);
                plugin.channelMembersTable.save(member);
                plugin.channelCache.updateChannelMembers(channelName, members);
                bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You are now subscribed" +
                        " to bulletins made in " + channelName, NamedTextColor.BLUE)));

            } catch(Exception exception) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error subscribing to the channel's bulletins.", NamedTextColor.RED));
                    plugin.log(exception.toString(), Level.SEVERE);
                });
                if(Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

/*
                                                Unsubscribe From Bulletins
------------------------------------------------------------------------------------------------------------------------
 */

    private void unsubscribeFromBulletins(CommandSender sender, String channelName) {

        if (!(sender instanceof Player player)) return;

        String UUID = player.getUniqueId().toString();
        plugin.channelCache.getChannelMembers(channelName).thenAccept(members -> {
            if (!members.containsKey(UUID) || !members.get(UUID).isSubscribed()) {
                bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You are not subscribed" +
                        " to that channel.", NamedTextColor.RED)));
                return;
            }

            try {
                ChannelMember member = members.get(UUID);
                member.setSubscribed(false);
                members.put(UUID, member);
                plugin.channelMembersTable.save(member);
                plugin.channelCache.updateChannelMembers(channelName, members);
                bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You are now unsubscribed" +
                        " from bulletins made in " + channelName, NamedTextColor.BLUE)));
            } catch (Exception ex) {
                bukkitScheduler.runTask(plugin, () -> {
                    sender.sendMessage(Component.text("There was an error unsubscribing from the channel's" +
                            " bulletins.", NamedTextColor.RED));
                    plugin.log(ex.toString(), Level.SEVERE);
                });
            }
        });
    }

/*
                                                  List Subscriptions
------------------------------------------------------------------------------------------------------------------------
 */

    private void listSubscriptions(CommandSender sender) {

        if (!(sender instanceof Player player)) return;

        String UUID = player.getUniqueId().toString();
        plugin.transientPlayerCache.getChannelsForPlayer(UUID).thenAccept(channels -> {
            List<String> subscribed = new ArrayList<>();

            for (ChannelMember channel : channels) {
                if (channel.isSubscribed()) {
                    subscribed.add(channel.getChannel());
                }
            }

            if (subscribed.isEmpty()) {
                bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("You have no current" +
                        " subscriptions.", NamedTextColor.BLUE)));
                return;
            }

            bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Current subscriptions: ",
                            NamedTextColor.BLUE).append(NCCUtil.formatList(subscribed, NamedTextColor.GRAY, NamedTextColor.GRAY))));
        });

    }

/*
                                                    Console Chat
------------------------------------------------------------------------------------------------------------------------
 */

    private void consoleChat(CommandSender sender, String[] args) {

        String channelName = args[1].toLowerCase();
        String message = NCCUtil.joinArray(" ", Arrays.copyOfRange(args, 2, args.length));
        plugin.channelCache.getChannel(channelName).thenAccept(channel -> {
            if (channel != null) {
                // Figure out which colour system is being used
                TextColor channelColor = NCCUtil.color(channel.getColor());
                TextColor channelTextColor = NCCUtil.color(channel.getText_color());

                TextComponent msg = Component.text("[" + channelName + "] ", channelColor)
                        .append(Component.text("<", NamedTextColor.GRAY))
                        .append(Component.text("~console", NamedTextColor.RED))
                        .append(Component.text("> ", NamedTextColor.GRAY))
                        .append(Component.text(message, channelTextColor));
                bukkitScheduler.runTask(plugin, () -> this.sendRawMessage(channelName, msg));
            } else {
                bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Invalid channel", NamedTextColor.RED)));
            }
        });
    }

/*
                                                       Set Flags
------------------------------------------------------------------------------------------------------------------------
 */

    private void setFlags(CommandSender sender, String[] args) {

        String channelName = args[1].toLowerCase();
        String flagKey = args[2].toLowerCase();
        String flagValue = args[3].toLowerCase();

        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName);
        CompletableFuture<Boolean> isOwnerFuture = this.senderIsOwner(sender, channelName, false);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(channelFuture, isOwnerFuture);

        allFutures.thenRun(() -> {
           try {

               Channel channel = channelFuture.get();
               boolean isOwner = isOwnerFuture.get();

               if (!isOwner) {
                   bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("This command is only" +
                           " available to the channel owner", NamedTextColor.RED)));
                   return;
               }

               if (!flagValue.equals("true") && !flagValue.equals("false")) {
                   bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Invalid command, type" +
                           " /clanchat flags for help (invalid boolean)", NamedTextColor.RED)));
                   return;
               }

               if (flagKey.equals("public")) {
                   channel.setPub(Boolean.parseBoolean(flagValue));
               }
               else if (flagKey.equals("secret")) {
                   channel.setSecret(Boolean.parseBoolean(flagValue));
               }
               else {
                   bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Invalid command, type" +
                           " /clanchat flags for help (invalid flag)", NamedTextColor.RED)));
                   return;
               }

               plugin.channelsTable.save(channel);
               plugin.channelCache.updateChannel(channelName, channel);
               bukkitScheduler.runTask(plugin, () -> sender.sendMessage(Component.text("Flag " + flagKey +
                       " has been set to " + flagValue + " for " + channelName, NamedTextColor.BLUE)));

           } catch(Exception exception) {
               bukkitScheduler.runTask(plugin, () -> {
                   sender.sendMessage(Component.text("There was an error updating the channel flags.", NamedTextColor.RED));
                   plugin.log(exception.toString(), Level.SEVERE);
               });
               if(Thread.currentThread().isInterrupted()) {
                   Thread.currentThread().interrupt();
               }
           }
        });
    }

/*
                                                   Sender Is Member
------------------------------------------------------------------------------------------------------------------------
 */

    private CompletableFuture<Boolean> senderIsMember(CommandSender sender, String channelName) {
        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName.toLowerCase());
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName.toLowerCase());

        Player player = (Player) sender;

        return channelFuture.thenCombine(membersFuture, (channel, members) -> {
            if (channel == null) return false;
            return members.containsKey(player.getUniqueId().toString());
        });
    }

/*
                                                   Sender is Manager
------------------------------------------------------------------------------------------------------------------------
 */

    private CompletableFuture<Boolean> senderIsManager(CommandSender sender, String channelName, boolean allowConsole) {
        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(channelName.toLowerCase());
        CompletableFuture<HashMap<String, ChannelMember>> membersFuture = plugin.channelCache.getChannelMembers(channelName.toLowerCase());

        return channelFuture.thenCombine(membersFuture, (channel, members) -> {
            if(allowConsole && !(sender instanceof Player)) return true;
            if(!(sender instanceof Player player)) return false;
            if(channel == null) return false;

            String playerUUID = player.getUniqueId().toString();

            if(!(members.containsKey(playerUUID))) return false;
            return (members.get(playerUUID).isManager() || channel.getOwner().equals(playerUUID));
        });
    }

/*
                                                    Sender is Owner
------------------------------------------------------------------------------------------------------------------------
 */

    private CompletableFuture<Boolean> senderIsOwner(CommandSender sender, String channelName, boolean allowConsole) {
        return plugin.channelCache.getChannel(channelName.toLowerCase()).thenApply(channel -> {
            if (allowConsole && !(sender instanceof Player)) return true;
            if (!(sender instanceof Player player)) return false;
            if (channel == null) return false;
            String UUID = player.getUniqueId().toString();
            return channel.getOwner().equals(UUID);
        });
    }

/*
                                                    Send Raw Message
------------------------------------------------------------------------------------------------------------------------
 */

    private void sendRawMessage(String channelName, TextComponent message) {
        plugin.channelCache.getChannelMembers(channelName.toLowerCase()).thenAccept(members -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (members.containsKey(player.getUniqueId().toString())) {
                    bukkitScheduler.runTask(plugin, () -> player.sendMessage(message));
                }
            }
        });
    }


    private void printHelpText(CommandSender sender) {

        sender.sendMessage(Component.text("ClanChat usage:", NamedTextColor.RED));
        sender.sendMessage(Component.text("/clanchat create <channel>", NamedTextColor.BLUE)
                .append(Component.text(" - Creates a new channel, with you as the owner.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat join <channel>", NamedTextColor.BLUE)
                .append(Component.text(" - Joins a channel that you've already been invited to.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/c [#<channel>] <message>", NamedTextColor.BLUE)
                .append(Component.text(" - Sends a message to the channel. You must be a member of the channel." +
                        " If you do not include a channel (prefixed by '#'), it will default to the last channel used.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/c #<channel>", NamedTextColor.BLUE)
                .append(Component.text(" Set your default channel without sending a message.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/cq #<channel> <message>", NamedTextColor.BLUE)
                .append(Component.text(" - Sends a quick message to the specified channel. This does not" +
                        " change your default channel.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/ca [#<channel>] <message>", NamedTextColor.BLUE)
                .append(Component.text(" - Sends an alert message to the channel. You must be an owner/manager" +
                        " of the channel. If a channel name is not included it will alert your current channel.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/cme [#<channel>] <message>", NamedTextColor.BLUE)
                .append(Component.text(" - Sends a \"/me\" type message to your current, or specified, channel.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/cr <message>", NamedTextColor.BLUE)
                .append(Component.text(" - Sends a message to the last channel that you received a message" +
                        " from, regardless of what your default channel is.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/cb [#<channel>]", NamedTextColor.BLUE)
                .append(Component.text(" - List bulletins for all channels you are a member of, or specify a" +
                        " channel to receive its bulletins.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/cm [#<channel>]", NamedTextColor.BLUE)
                .append(Component.text(" - Lists all members in your default channel. If a channel is given," +
                        " lists members in that channel.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Type ", NamedTextColor.GRAY)
                .append(Component.text("/clanchat more", NamedTextColor.BLUE))
                .append(Component.text(" for additional commands.", NamedTextColor.GRAY)));
    }


    private void printMoreHelpText(CommandSender sender) {

        sender.sendMessage(Component.text("/cs [#<channel>] <message>", NamedTextColor.BLUE)
                .append(Component.text(" - Sends a \"/s\" type sarcasm message to your current, or specified," +
                        " channel.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat delete <channel>", NamedTextColor.BLUE)
                .append(Component.text(" - Deletes a channel. Owner only.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat color <channel> <color>", NamedTextColor.BLUE)
                .append(Component.text(" - Sets the channel color. Set by channel owner/managers.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat textcolor <channel> <color>", NamedTextColor.BLUE)
                .append(Component.text(" - Sets the channel text color. Set by channel owner/managers.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat alertcolor <channel> <color>", NamedTextColor.BLUE)
                .append(Component.text(" - Sets the channel alert color. Set by channel owner/managers.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat members <channel>", NamedTextColor.BLUE)
                .append(Component.text(" - Lists all the members in a channel.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat invite <channel> <player>", NamedTextColor.BLUE)
                .append(Component.text(" - Invites a player to the channel. You must be a manager to invite.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat uninvite <channel> <player>", NamedTextColor.BLUE)
                .append(Component.text(" - Uninvites a previously invited player. You must be a manager.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat changeowner <channel> <player>", NamedTextColor.BLUE)
                .append(Component.text(" - Changes the owner on the given channel. Only the owner can set this. ", NamedTextColor.WHITE))
                .append(Component.text("Careful! This can't be undone.", NamedTextColor.RED)));
        sender.sendMessage(Component.text("/clanchat addmanager <channel> <player>", NamedTextColor.BLUE)
                .append(Component.text(" - Adds a manager to the channel. You must be the owner to run this.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat removemanager <channel> <player>", NamedTextColor.BLUE)
                .append(Component.text(" - Removes a manager from the channel. You must be the owner to run this.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat listmanagers <channel>", NamedTextColor.BLUE)
                .append(Component.text(" - List all managers in the channel.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat remove <channel> <player>", NamedTextColor.BLUE)
                .append(Component.text(" - Removes a player from the channel. You must be a manager to do this.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat leave <channel>", NamedTextColor.BLUE)
                .append(Component.text(" - Leaves a channel that you're in.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat list", NamedTextColor.BLUE)
                .append(Component.text(" - Lists all the channels you are in.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat public", NamedTextColor.BLUE)
                .append(Component.text(" - Lists all public channels.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat flags", NamedTextColor.BLUE)
                .append(Component.text(" - Sets channel flags. Type /clanchat flags for more information.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat addbulletin <channel> <bulletin>", NamedTextColor.BLUE)
                .append(Component.text(" - Add bulletin to the channel.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat removebulletin <channel> <number>", NamedTextColor.BLUE)
                .append(Component.text(" - Remove the bulletin from the channel, <number> starts at 1 with the" +
                        " top bulletin.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat subscribe <channel>", NamedTextColor.BLUE)
                .append(Component.text(" - Subscribe to a channel's bulletins.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat unsubscribe <channel>", NamedTextColor.BLUE)
                .append(Component.text(" - Unsubscribe from a channel's bulletins.", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/clanchat subscriptions", NamedTextColor.BLUE)
                .append(Component.text(" - List your current bulletin subscriptions.", NamedTextColor.WHITE)));

        if (sender.hasPermission("nerdclanchat.admin")) {

            sender.sendMessage(Component.text("/clanchat channels", NamedTextColor.BLUE)
                    .append(Component.text(" - Lists all the channels and their owners.", NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/clanchat reloadconfig", NamedTextColor.BLUE)
                    .append(Component.text(" - Reload the plugin configuration.", NamedTextColor.WHITE)));

        }
        if (!(sender instanceof Player)) {

            sender.sendMessage(Component.text("/clanchat chat <channel> <message>", NamedTextColor.BLUE)
                    .append(Component.text(" - Chats to an arbitrary channel. Only available to console.", NamedTextColor.WHITE)));

        }
    }

    public void sendAllowedColours(CommandSender sender) {
        sender.sendMessage(Component.text("Only a hex code (#FFFFFF) or one of the following colors" +
                " can be used:", NamedTextColor.RED));
        sender.sendMessage(NCCUtil.formatColorList(NCCUtil.colorList()));
    }


}
