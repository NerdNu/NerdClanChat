package nu.nerd.NerdClanChat.database;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import nu.nerd.NerdClanChat.NerdClanChat;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BulletinsTable {


    NerdClanChat plugin;
    Dao<Bulletin, Integer> bulletinsDao;


    /**
     * Constructs a BulletinsTable instance.
     * @param plugin The plugin instance.
     */
    public BulletinsTable(NerdClanChat plugin) {
        this.plugin = plugin;
        this.bulletinsDao = plugin.getBulletinsDao();
    }


    /**
     * Returns an async list of a channel's bulletins.
     * @param channel The channel being searched.
     * @return an async list of a channel's bulletins.
     */
    public CompletableFuture<List<Bulletin>> getChannelBulletins(String channel) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryBuilder<Bulletin, Integer> queryBuilder = bulletinsDao.queryBuilder();
                queryBuilder.where().eq("channel", channel);
                return bulletinsDao.query(queryBuilder.prepare());
            } catch(SQLException exception) {
                plugin.log("Failed to fetch bulletins for channel: " + channel, Level.SEVERE);
                exception.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Deletes all of a channel's bulletins.
     * @param channel the channel to delete all bulletins from.
     */
    public void deleteChannelBulletins(String channel) {
        CompletableFuture.runAsync(() -> {
            try {
                DeleteBuilder<Bulletin, Integer> deleteBuilder = bulletinsDao.deleteBuilder();
                deleteBuilder.where().eq("channel", channel);
                deleteBuilder.delete();
            } catch(SQLException exception) {
                plugin.log("Failed to delete bulletins for channel: " + channel, Level.SEVERE);
                exception.printStackTrace();
            }
        });
    }

    /**
     * Delete a specific bulletin.
     * @param bulletin the bulletin being deleted.
     */
    public void delete(Bulletin bulletin) {
        CompletableFuture.runAsync(() -> {
            try {
                bulletinsDao.delete(bulletin);
            } catch(SQLException exception) {
                plugin.log("Failed to delete bulletin!", Level.SEVERE);
                exception.printStackTrace();
            }
        });
    }

    /**
     * Save a bulletin to the database.
     * @param bulletin the bulletin being saved.
     */
    public void save(Bulletin bulletin) {
        CompletableFuture.runAsync(() -> {
            try {
                bulletinsDao.createOrUpdate(bulletin);
            } catch(SQLException exception) {
                plugin.log("Failed to save bulletin!", Level.SEVERE);
            }
        });
    }


}
