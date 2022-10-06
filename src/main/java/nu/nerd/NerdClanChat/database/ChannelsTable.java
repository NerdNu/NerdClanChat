package nu.nerd.NerdClanChat.database;


import io.ebean.Query;
import nu.nerd.NerdClanChat.NerdClanChat;

import java.util.ArrayList;
import java.util.List;

public class ChannelsTable {


    NerdClanChat plugin;


    public ChannelsTable(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    public Channel getChannel(String name) {
        Channel ch = null;
        Query<Channel> query = plugin.getDatabase().find(Channel.class).where().ieq("name", name).query();
        if (query != null) {
            ch = query.findOne();
        }
        return ch;
    }


    public List<Channel> getAllChannels() {
        List<Channel> channels = new ArrayList<Channel>();
        Query<Channel> query = plugin.getDatabase().find(Channel.class).where().ne("name", "").query();
        if (query != null) {
            for (Channel channel : query.findList()) {
                channels.add(channel);
            }
        }
        return channels;
    }


    public boolean channelExists(String name) {
        Query<Channel> query = plugin.getDatabase().find(Channel.class).where().ieq("name", name).query();
        return query != null;
    }


    public void delete(Channel channel) {
        plugin.getDatabase().delete(channel);
    }


    public void save(Channel channel) {
        plugin.getDatabase().save(channel);
    }


    public void update(Channel channel) {
        plugin.getDatabase().update(channel);
    }


}
