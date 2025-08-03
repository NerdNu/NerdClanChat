/**
 * This is a memory-first cache that player commands can write to, and then
 * is persisted to the database on a schedule.
 */

package nu.nerd.NerdClanChat.caching;


import nu.nerd.NerdClanChat.NerdClanChat;
import nu.nerd.NerdClanChat.database.PlayerMeta;
import nu.nerd.NerdClanChat.database.PlayerMetaTable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PlayerMetaCache {


    public NerdClanChat plugin;
    private ConcurrentHashMap<String, PlayerMeta> playerMeta;
    private ConcurrentHashMap<String, Boolean> persisted;
    private PlayerMetaTable playerMetaTable;

    /**
     * The constructor to create the playermeta cache.
     * @param plugin the instance of the plugin.
     */
    public PlayerMetaCache(NerdClanChat plugin) {
        this.plugin = plugin;
        this.playerMeta = new ConcurrentHashMap<>();
        this.persisted = new ConcurrentHashMap<>();
        this.playerMetaTable = plugin.getPlayerMetaTable();
    }

    /**
     * Fetches a player's playermeta from cache. If not in cache, fetches from the database and caches it.
     * @param UUID the UUID of the player whose playermeta is being fetched.
     * @return the player's playermeta.
     */
    public CompletableFuture<PlayerMeta> getPlayerMeta(String UUID) {
        if (this.playerMeta.containsKey(UUID)) {
            return CompletableFuture.completedFuture(this.playerMeta.get(UUID)); //cache hit
        } else {
            return plugin.playerMetaTable.getPlayerMeta(UUID).thenApply(meta -> { //load from database on cache miss
                if (meta == null) { //if there isn't an entry for a player, create it
                    meta = new PlayerMeta(UUID);
                    try {
                        plugin.playerMetaTable.save(meta);
                    } catch (Exception ex) {
                        plugin.getLogger().warning(ex.toString());
                    }
                }
                this.playerMeta.put(UUID, meta);
                this.persisted.put(UUID, true);
                return meta;
            });
        }
    }

    /**
     * Check if the provided player's playermeta is persisted to the database.
     * @param UUID the player's UUID.
     * @return true if it is, false if not.
     */
    public boolean isMetaPersisted(String UUID) {
        if (!(this.persisted.containsKey(UUID))) {
            return false;
        }
        else {
            return this.persisted.get(UUID);
        }
    }

    /**
     * Sets whether a player's playermeta is persisted or not.
     * @param UUID the UUID of the player being checked.
     * @param isPersisted true if persisted, false if not.
     */
    public void setMetaPersisted(String UUID, boolean isPersisted) {
        if (this.persisted.containsKey(UUID)) {
            this.persisted.remove(UUID);
        }
        this.persisted.put(UUID, isPersisted);
    }

    /**
     * Updates a player's playermeta.
     * @param UUID the UUID of the player whose playermeta is being updated.
     * @param meta the playermeta.
     */
    public void updatePlayerMeta(String UUID, PlayerMeta meta) {
        if (this.playerMeta.containsKey(UUID)) {
            this.playerMeta.remove(UUID);
        }
        this.playerMeta.put(UUID, meta);
        this.setMetaPersisted(UUID, false);
    }

    /**
     * Fetches a player's playermeta by username from cache. If not in cache, fetches from the database and caches it.
     * @param name the username of the player whose playermeta is being fetched.
     * @return the player's playermeta.
     */
    public CompletableFuture<PlayerMeta> getPlayerMetaByName(String name) {
        CompletableFuture<PlayerMeta> retVal = null;
        for (PlayerMeta entry : this.playerMeta.values()) {
            if (entry.getName().equalsIgnoreCase(name)) {
                retVal = CompletableFuture.completedFuture(entry);
            }
        }
        if (retVal == null) {
            retVal = plugin.playerMetaTable.getPlayerMetaByName(name.toLowerCase());
        }
        return retVal;
    }

    /**
     * Writes the cache to the database.
     */
    public void persistCache() {
        try {
            for(Map.Entry<String, Boolean> entry : persisted.entrySet()) {
                if (!entry.getValue()) {
                    this.getPlayerMeta(entry.getKey()).thenAccept(meta -> {
                        playerMetaTable.save(meta);
                        entry.setValue(true);
                    });
                }
            }
        } catch (Exception ex) {
            plugin.log("Failed to persist cache.", Level.SEVERE);
            ex.printStackTrace();
        }
    }


}
