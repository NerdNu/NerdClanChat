package nu.nerd.NerdClanChat.database;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import nu.nerd.NerdClanChat.NerdClanChat;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class ChannelMembersTable {


    NerdClanChat plugin;
    Dao<ChannelMember, Integer> channelMembersDao;

    /**
     * Constructor that creates a new ChannelMembersTable instance.
     * @param plugin the instance of the plugin.
     */
    public ChannelMembersTable(NerdClanChat plugin) {
        this.plugin = plugin;
        this.channelMembersDao = plugin.getChannelMembersDao();
    }


    /**
     * Get a hashmap of the members of the specified channel.
     * @param channel the channel members are being fetched from.
     * @return the hashmap of UUIDs and members.
     */
    public CompletableFuture<HashMap<String, ChannelMember>> getChannelMembers(String channel) {
        return CompletableFuture.supplyAsync(() -> {
            HashMap<String, ChannelMember> members = new HashMap<>();
            try {
                List<ChannelMember> membersList = channelMembersDao.queryBuilder().where().eq("channel", channel).query();
                for(ChannelMember member : membersList) {
                    members.put(member.getUuid(), member);
                }
                return members;
            } catch(SQLException exception) {
                plugin.log("Failed to get members of channel: " + channel, Level.SEVERE);
                exception.printStackTrace();
            }
            return null;
        });
    }


    /**
     * Gets all channels the provided player is a member of.
     * @param UUID the UUID of the player being checked.
     * @return a list of channel member objects that have channels paired with them.
     */
    public CompletableFuture<List<ChannelMember>> getChannelsForPlayer(String UUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return channelMembersDao.queryBuilder().where().eq("UUID", UUID).query();
            } catch(SQLException exception) {
                plugin.log("Unable to get channels for user: " + UUID, Level.SEVERE);
                exception.printStackTrace();
            }
            return null;
        });
    }


    /**
     * Updates the username field in the database for a given UUID.
     * @param UUID the UUID whose username is being updated.
     * @param newName the new username.
     */
    public void updateChannelMemberNames(String UUID, String newName) {
        CompletableFuture.runAsync(() -> {
            try {
                UpdateBuilder<ChannelMember, Integer> updateBuilder = channelMembersDao.updateBuilder();
                updateBuilder.updateColumnValue("name", newName).where().eq("UUID", UUID);
                updateBuilder.update();
            } catch(SQLException exception) {
                plugin.log("Failed to update channel member name: " + UUID, Level.SEVERE);
                exception.printStackTrace();
            }
        });
    }


    /**
     * Delete all members of a channel.
     * @param channel the channel having its members deleted.
     */
    public void deleteChannelMembers(String channel) {
        CompletableFuture.runAsync(() -> {
            try {
                DeleteBuilder<ChannelMember, Integer> deleteBuilder = channelMembersDao.deleteBuilder();
                deleteBuilder.where().eq("channel", channel);
                deleteBuilder.delete();
            } catch(SQLException exception) {
                plugin.log("Failed to delete channel members for: " + channel, Level.SEVERE);
                exception.printStackTrace();
            }
        });
    }


    /**
     * Deletes a single channel member.
     * @param channelMember the member being deleted.
     */
    public void delete(ChannelMember channelMember) {
        CompletableFuture.runAsync(() -> {
            try {
                channelMembersDao.delete(channelMember);
            } catch(SQLException exception) {
                plugin.log("Failed to delete channel member: " + channelMember.getName(), Level.SEVERE);
                exception.printStackTrace();
            }
        });
    }


    /**
     * Saves a channel member to the database.
     * @param channelMember the channel member being saved.
     */
    public void save(ChannelMember channelMember) {
        try {
            channelMembersDao.createOrUpdate(channelMember);
        } catch(SQLException exception) {
            plugin.log("Failed to save/update channel member: " + channelMember.getName(), Level.SEVERE);
            exception.printStackTrace();
        }
    }


}
