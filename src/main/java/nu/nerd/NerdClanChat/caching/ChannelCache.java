/**
 * This is a disk-first cache that lazy-loads from the database, to make
 * future reads faster. Writes should be done through the ChannelsTable class.
 */

package nu.nerd.NerdClanChat.caching;

import nu.nerd.NerdClanChat.NerdClanChat;
import nu.nerd.NerdClanChat.database.Bulletin;
import nu.nerd.NerdClanChat.database.Channel;
import nu.nerd.NerdClanChat.database.ChannelMember;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class ChannelCache {


    public NerdClanChat plugin;
    private final HashMap<String, Channel> channels;
    private final HashMap<String, HashMap<String, ChannelMember>> members;
    private final HashMap<String, List<Bulletin>> bulletins;

    /**
     * The constructor to create a new channel cache instance.
     * @param plugin the instance of the plugin.
     */
    public ChannelCache(NerdClanChat plugin) {
        this.plugin = plugin;
        this.channels = new HashMap<>();
        this.members = new HashMap<>();
        this.bulletins = new HashMap<>();
    }

    /**
     * Get a channel from cache. If not cached, get it from the database and cache it.
     * @param name the channel's name.
     * @return the channel
     */
    public CompletableFuture<Channel> getChannel(String name) {
        name = name.toLowerCase();
        if (this.channels.containsKey(name)) {
            return CompletableFuture.completedFuture(this.channels.get(name)); //cache hit
        }
        String finalName = name;
        return plugin.channelsTable.getChannel(finalName).thenApply(channel -> {
            if (channel != null) {
                this.channels.put(finalName, channel);
                return channel;
            }
            return null;
        });
    }

    /**
     * Updates a channel in the cache.
     * @param name the name of the channel being updated.
     * @param channel the channel being updated.
     */
    public void updateChannel(String name, Channel channel) {
        if (name == null || name.length() < 1) {
			return;
		}
        name = name.toLowerCase();
        if (this.channels.containsKey(name)) {
            this.channels.remove(name);
        }
        this.channels.put(name, channel);
    }

    /**
     * Fetches all members of a channel. Also adds them to cache.
     * @param channel the channel being fetched from.
     * @return a hashmap of the channel's name and its members.
     */
    public CompletableFuture<HashMap<String, ChannelMember>> getChannelMembers(String channel) {
        channel = channel.toLowerCase();
        if (this.members.containsKey(channel)) {
            return CompletableFuture.completedFuture(this.members.get(channel)); //cache hit
        }
        String finalChannel = channel;
        return plugin.channelMembersTable.getChannelMembers(channel).thenApply(chm -> { //load from database
            if (chm != null) {
                this.members.put(finalChannel, chm);
            }
            return chm;
        });
    }

    /**
     * Updates all members of the specified channel.
     * @param channel the channel having its members updated.
     * @param channelMembers the cached hashmap of members.
     */
    public void updateChannelMembers(String channel, HashMap<String, ChannelMember> channelMembers) {
        if (channel == null || channel.length() < 1) {
			return;
		}
        channel = channel.toLowerCase();
        if (this.members.containsKey(channel)) {
            this.members.remove(channel);
        }
        this.members.put(channel, channelMembers);
        plugin.transientPlayerCache.clearPlayerCache(channelMembers.values());
    }

    /**
     * Fetches all bulletins belonging to the specified channel.
     * @param channel the channel having its bulletins fetched.
     * @return a list of bulletins.
     */
    public CompletableFuture<List<Bulletin>> getBulletins(String channel) {
        channel = channel.toLowerCase();
        if (this.bulletins.containsKey(channel)) {
            return CompletableFuture.completedFuture(this.bulletins.get(channel)); //cache hit
        }
        String finalChannel = channel;
        plugin.bulletinsTable.getChannelBulletins(channel).thenApply(bulletinList -> { //load from database
            if (bulletinList != null) {
                this.bulletins.put(finalChannel, bulletinList);
            }
            return bulletinList;
        });
        return null;
    }

    /**
     * Updates all bulletins of the specified channel.
     * @param channel the channel having its bulletins updated.
     * @param bulletins the cached hashmap of bulletins.
     */
    public void updateBulletins(String channel, List<Bulletin> bulletins) {
        if (channel == null || channel.length() < 1) {
			return;
		}
        channel = channel.toLowerCase();
        if (this.bulletins.containsKey(channel)) {
            this.bulletins.remove(channel);
        }
        this.bulletins.put(channel, bulletins);
    }

    /**
     * Removes a channel and all of its cached data.
     * @param channel the channel being removed.
     */
    public void remove(String channel) {
        HashMap<String, ChannelMember> channelMembers = this.members.get(channel);
        plugin.transientPlayerCache.clearPlayerCache(channelMembers.values());
        this.channels.remove(channel);
        this.members.remove(channel);
        this.bulletins.remove(channel);
    }


}
