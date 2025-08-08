package nu.nerd.NerdClanChat.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import nu.nerd.NerdClanChat.NerdClanChat;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class InvitesTable {


    NerdClanChat plugin;
    Dao<Invite, Integer> invitesDao;

    /**
     * The constructor to create an invite table
     * @param plugin
     */
    public InvitesTable(NerdClanChat plugin) {
        this.plugin = plugin;
        this.invitesDao = plugin.getInvitesDao();
    }

    /**
     * Delete all invites belonging to a channel.
     * @param channel the channel having its invites deleted.
     */
    public void deleteChannelInvites(String channel) {
        CompletableFuture.runAsync(() -> {
            try {
                DeleteBuilder<Invite, Integer> deleteBuilder = invitesDao.deleteBuilder();
                deleteBuilder.where().eq("channel", channel);
                deleteBuilder.delete();
            } catch(SQLException exception) {
                plugin.log("Failed to delete invites for channel: " + channel, Level.SEVERE);
                exception.printStackTrace();
            }
        });
    }

    /**
     * Check if a player has already been invited to the specified channel.
     * @param UUID the UUID of the player.
     * @param channel the name of the channel.
     * @return true if they have been, false if not.
     */
    public CompletableFuture<Boolean> alreadyInvited(String UUID, String channel) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return invitesDao.queryBuilder().where().eq("UUID", UUID).and()
                        .eq("channel", channel).countOf() > 0;
            } catch(SQLException exception) {
                plugin.log("Unable to check if " + UUID + " is already invited to " + channel, Level.INFO);
                exception.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Closes an invitation and deletes it from the database.
     * @param UUID the UUID of the player whose invite is being deleted.
     * @param channel the channel the invite belongs to.
     */
    public void closeInvitation(String UUID, String channel) {
        CompletableFuture.runAsync(() -> {
            try {
                DeleteBuilder<Invite, Integer> deleteBuilder = invitesDao.deleteBuilder();
                deleteBuilder.where().eq("UUID", UUID).and().eq("channel", channel);
                deleteBuilder.delete();
            } catch(SQLException exception) {
                plugin.log("Failed to close invitation for " + UUID + " for channel " + channel, Level.SEVERE);
                exception.printStackTrace();
            }
        });
    }

    /**
     * Saves an invitation in the database.
     * @param invite the invite.
     */
    public void save(Invite invite) {
        CompletableFuture.runAsync(() -> {
            try {
                invitesDao.createOrUpdate(invite);
            } catch(SQLException exception) {
                plugin.log("Failed to save/update invite for " + invite.getUuid() + " in " +
                        invite.getChannel(), Level.SEVERE);
                exception.printStackTrace();
            }
        });
    }


}
