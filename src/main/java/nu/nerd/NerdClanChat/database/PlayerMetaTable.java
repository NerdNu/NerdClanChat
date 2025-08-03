package nu.nerd.NerdClanChat.database;

import com.j256.ormlite.dao.Dao;
import nu.nerd.NerdClanChat.NerdClanChat;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PlayerMetaTable {


    NerdClanChat plugin;
    Dao<PlayerMeta, Integer> playerMetaDao;


    /**
     * The constructor to create a new playermeta table.
     * @param plugin the instance of the plugin.
     */
    public PlayerMetaTable(NerdClanChat plugin) {
        this.plugin = plugin;
        this.playerMetaDao = plugin.getPlayerMetaDao();
    }

    /**
     * Get a specific player's playermeta by UUID.
     * @param UUID the UUID of the player whose playermeta is being fetched.
     * @return the player's playermeta.
     */
    public CompletableFuture<PlayerMeta> getPlayerMeta(String UUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return playerMetaDao.queryBuilder().where().eq("UUID", UUID).queryForFirst();
            } catch(SQLException exception) {
                plugin.log("Failed to fetch playermeta for " + UUID, Level.SEVERE);
                exception.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Get a specific player's playermeta by username.
     * @param name the username of the player whose playermeta is being fetched.
     * @return the player's playermeta.
     */
    public CompletableFuture<PlayerMeta> getPlayerMetaByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return playerMetaDao.queryBuilder().where().eq("name", name).queryForFirst();
            } catch(SQLException exception) {
                plugin.log("Failed to fetch playermeta for " + name, Level.SEVERE);
                exception.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Save a playermeta to the database.
     * @param playerMeta the playermeta being saved.
     */
    public void save(PlayerMeta playerMeta) {
        CompletableFuture.runAsync(() -> {
            try {
                playerMetaDao.createOrUpdate(playerMeta);
            } catch(SQLException exception) {
                plugin.log("Failed to save/update playermeta for " + playerMeta.getName(), Level.SEVERE);
                exception.printStackTrace();
            }
        });
    }


}
