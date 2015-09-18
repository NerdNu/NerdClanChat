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


public class ChannelCache {


    public NerdClanChat plugin;
    private HashMap<String, Channel> channels;
    private HashMap<String, HashMap<String, ChannelMember>> members;
    private HashMap<String, List<Bulletin>> bulletins;


    public ChannelCache(NerdClanChat plugin) {
        this.plugin = plugin;
        this.channels = new HashMap<String, Channel>();
        this.members = new HashMap<String, HashMap<String, ChannelMember>>();
        this.bulletins = new HashMap<String, List<Bulletin>>();
    }


    public Channel getChannel(String name) {
        name = name.toLowerCase();
        if (this.channels.containsKey(name)) {
            return this.channels.get(name); //cache hit
        } else {
            Channel ch = plugin.channelsTable.getChannel(name); //load channel from database
            if (ch != null) {
                this.channels.put(name, ch);
            }
            return ch;
        }
    }


    public void updateChannel(String name, Channel channel) {
        if (name == null || name.length() < 1) return;
        name = name.toLowerCase();
        if (this.channels.containsKey(name)) {
            this.channels.remove(name);
        }
        this.channels.put(name, channel);
    }


    public HashMap<String, ChannelMember> getChannelMembers(String channel) {
        channel = channel.toLowerCase();
        if (this.members.containsKey(channel)) {
            return this.members.get(channel); //cache hit
        } else {
            HashMap<String, ChannelMember> chm = plugin.channelMembersTable.getChannelMembers(channel); //load from database
            if (chm != null) {
                this.members.put(channel, chm);
            }
            return chm;
        }
    }


    public List<Bulletin> getBulletins(String channel) {
        channel = channel.toLowerCase();
        if (this.bulletins.containsKey(channel)) {
            return this.bulletins.get(channel); //cache hit
        } else {
            List<Bulletin> bul = plugin.bulletinsTable.getChannelBulletins(channel); //load from database
            if (bul != null) {
                this.bulletins.put(channel, bul);
            }
            return bul;
        }
    }


    public void remove(String channel) {
        this.channels.remove(channel);
        this.members.remove(channel);
        this.bulletins.remove(channel);
    }


}
