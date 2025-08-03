package nu.nerd.NerdClanChat.database;


import com.j256.ormlite.dao.Dao;
import nu.nerd.NerdClanChat.NerdClanChat;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class ChannelsTable {


    NerdClanChat plugin;
    Dao<Channel, Integer> channelsDao;


    /**
     * Constructor to create a new channels table instance.
     * @param plugin The instance of the plugin.
     */
    public ChannelsTable(NerdClanChat plugin) {
        this.plugin = plugin;
        this.channelsDao = plugin.getChannelsDao();
    }

    /**
     * Get a channel from its name.
     * @param name the name of the channel.
     * @return the channel object.
     */
    public CompletableFuture<Channel> getChannel(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return channelsDao.queryBuilder().where().eq("name", name).queryForFirst();
            } catch(SQLException exception) {
                plugin.log("Failed to fetch channel: " + name, Level.SEVERE);
                exception.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Get all channels that currently exist.
     * @return a list of all channels.
     */
    public CompletableFuture<List<Channel>> getAllChannels() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return channelsDao.queryBuilder().where().ne("name", "").query();
            } catch(SQLException exception) {
                plugin.log("Failed to fetch all channels", Level.SEVERE);
                exception.printStackTrace();
            }
            return Collections.emptyList();
        });
    }


    /**
     * Check if a channel exists.
     * @param name the name of the channel.
     * @return true if the channel exists, false if not.
     */
    public CompletableFuture<Boolean> channelExists(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return channelsDao.queryBuilder().where().eq("name", name).countOf() > 0;
            } catch(SQLException exception) {
                plugin.log("Failed to check if channel exists: " + name, Level.SEVERE);
                exception.printStackTrace();
            }
            return null;
        });
    }


    /**
     * Delete a channel from the database.
     * @param channel the channel being deleted.
     */
    public void delete(Channel channel) {
        CompletableFuture.runAsync(() -> {
            try {
                channelsDao.delete(channel);
            } catch(Exception exception) {
                plugin.log("Failed to delete channel: " + channel.getName(), Level.SEVERE);
                exception.printStackTrace();
            }
        });
    }


    /**
     * Save a channel in the database.
     * @param channel the channel being saved.
     */
    public void save(Channel channel) {
        CompletableFuture.runAsync(() -> {
            try {
                channelsDao.createOrUpdate(channel);
            } catch(SQLException exception) {
                plugin.log("Failed to save/update channel: " + channel.getName(), Level.SEVERE);
                exception.printStackTrace();
            }
        });
    }


}
