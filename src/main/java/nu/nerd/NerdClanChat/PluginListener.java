package nu.nerd.NerdClanChat;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import nu.nerd.NerdClanChat.database.Bulletin;
import nu.nerd.NerdClanChat.database.Channel;
import nu.nerd.NerdClanChat.database.ChannelMember;
import nu.nerd.NerdClanChat.database.PlayerMeta;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PluginListener implements Listener {


    private final NerdClanChat plugin;


    public PluginListener(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.updateStoredPlayerName(event);
        this.printBulletins(event);
    }


    public void updateStoredPlayerName(PlayerJoinEvent event) {

        String UUID = event.getPlayer().getUniqueId().toString();
        String name = event.getPlayer().getName();
        plugin.playerMetaCache.getPlayerMeta(UUID).thenAccept(meta -> {

            boolean isNewPlayer = false;

            if ( meta.getName() != null && !meta.getName().equals(name) ) {
                plugin.channelMembersTable.updateChannelMemberNames(UUID, name);
            }

            if (meta.getName() == null || meta.getName().isEmpty()) {
                isNewPlayer = true;
            }

            meta.setName(name);
            plugin.playerMetaCache.updatePlayerMeta(UUID, meta);

            if (isNewPlayer) {
                plugin.playerMetaTable.save(meta);
                plugin.playerMetaCache.setMetaPersisted(UUID, true);
            }
        });
    }


    public void printBulletins(PlayerJoinEvent event) {
        String UUID = event.getPlayer().getUniqueId().toString();
        CompletableFuture<List<ChannelMember>> channelsFuture = plugin.transientPlayerCache.getChannelsForPlayer(UUID);
        Integer limit = plugin.config.BULLETIN_LIMIT;

        channelsFuture.thenAccept(channels -> {
            if (channels != null && !channels.isEmpty()) {
                for (ChannelMember cm : channels) {
                    if (cm.isSubscribed()) {
                        CompletableFuture<List<Bulletin>> bulletinsFuture = plugin.channelCache.getBulletins(cm.getChannel());
                        CompletableFuture<Channel> channelFuture = plugin.channelCache.getChannel(cm.getChannel());

                        CompletableFuture.allOf(bulletinsFuture, channelFuture)
                                .thenAccept(v -> {
                                    List<Bulletin> bulletins = bulletinsFuture.join();
                                    Channel channel = channelFuture.join();

                                    if (bulletins != null && !bulletins.isEmpty()) {
                                        TextComponent tag = Component.text("[" + channel.getName() + "] ",
                                                NCCUtil.color(channel.getColor()));

                                        List<Bulletin> bulletinsToShow = bulletins;
                                        if (limit > 0 && bulletins.size() > limit) {
                                            bulletinsToShow = bulletins.subList(bulletins.size() - limit, bulletins.size());
                                        }

                                        for (Bulletin bulletin : bulletinsToShow) {
                                            event.getPlayer().sendMessage(tag.append(Component.text(bulletin.getMessage(), NCCUtil.color(channel.getAlertColor()))));
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }


}
