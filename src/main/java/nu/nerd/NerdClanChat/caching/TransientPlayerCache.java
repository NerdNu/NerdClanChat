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

public class TransientPlayerCache {


    public NerdClanChat plugin;
    private HashMap<String, List<ChannelMember>> memberOfChannels;


    public TransientPlayerCache(NerdClanChat plugin) {
        this.plugin = plugin;
        this.memberOfChannels = new HashMap<String, List<ChannelMember>>();
    }


    public void clearCache() {
        this.memberOfChannels.clear();
    }


    public void clearPlayerCache(String UUID) {
        if (this.memberOfChannels.containsKey(UUID)) this.memberOfChannels.remove(UUID);
    }


    public void clearPlayerCache(Collection<ChannelMember> channelMembers) {
        for (ChannelMember member : channelMembers) {
            this.clearPlayerCache(member.getUUID());
        }
    }


    public List<ChannelMember> getChannelsForPlayer(String UUID) {
        if (this.memberOfChannels.containsKey(UUID)) {
            return this.memberOfChannels.get(UUID); //cache hit
        } else {
            List<ChannelMember> channels = plugin.channelMembersTable.getChannelsForPlayer(UUID);
            if (channels != null) {
                this.memberOfChannels.put(UUID, channels);
            }
            return channels;
        }
    }


}
