/**
 * This is a transient cache for miscellaneous player data, which is not persisted.
 * Mostly it's to prevent repetitive database queries if someone spams a command that reads
 * a data set that isn't typically in the other caches. (e.g. /cb and its channel list.)
 */

package nu.nerd.NerdClanChat.caching;


import nu.nerd.NerdClanChat.NerdClanChat;
import nu.nerd.NerdClanChat.database.ChannelMember;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TransientPlayerCache {


    public NerdClanChat plugin;
    private HashMap<String, List<ChannelMember>> memberOfChannels;

    /**
     * The constructor to create a transient player cache.
     * @param plugin the instance of the plugin.
     */
    public TransientPlayerCache(NerdClanChat plugin) {
        this.plugin = plugin;
        this.memberOfChannels = new HashMap<>();
    }

    /**
     * Clears all instances from the cache.
     */
    public void clearCache() {
        this.memberOfChannels.clear();
    }

    /**
     * Clears a specific player's cache using their UUID.
     * @param UUID the UUID of the player whose cache is being cleared.
     */
    public void clearPlayerCache(String UUID) {
        if (this.memberOfChannels.containsKey(UUID)) this.memberOfChannels.remove(UUID);
    }

    /**
     * Clears the cache of all channel members provided in a collection.
     * @param channelMembers a collection of channel members to be cleared from cache.
     */
    public void clearPlayerCache(Collection<ChannelMember> channelMembers) {
        for (ChannelMember member : channelMembers) {
            this.clearPlayerCache(member.getUUID());
        }
    }

    /**
     * Fetches all channels a player is a member of by using the channelmember object.
     * @param UUID the UUID of the player whose channels are being fetched.
     * @return A list of channelmember instances paired to a channel and the player.
     */
    public CompletableFuture<List<ChannelMember>> getChannelsForPlayer(String UUID) {
        if (this.memberOfChannels.containsKey(UUID)) {
            return CompletableFuture.completedFuture(this.memberOfChannels.get(UUID)); //cache hit
        } else {
            return plugin.channelMembersTable.getChannelsForPlayer(UUID).thenApply(channels -> {
                if (channels != null) {
                    this.memberOfChannels.put(UUID, channels);
                }
                return channels;
            });
        }
    }


}
