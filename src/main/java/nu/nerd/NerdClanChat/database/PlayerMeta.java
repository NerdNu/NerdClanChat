package nu.nerd.NerdClanChat.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "clanchat_playermeta")
public class PlayerMeta {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, unique = true)
    private String UUID;

    @DatabaseField
    private String name;

    @DatabaseField
    private String lastReceived;

    @DatabaseField
    private String defaultChannel;

    /**
     * Empty constructor required by ORMLite.
     */
    public PlayerMeta() {
    }

    /**
     * Constructor to create a new playermeta instance.
     * @param UUID the UUID of the player who this meta belongs to.
     */
    public PlayerMeta(String UUID) {
        this.setUUID(UUID);
    }

    /**
     * Returns the UUID of the player who the meta belongs to.
     * @return the UUID of the player who the meta belongs to.
     */
    public String getUUID() {
        return this.UUID;
    }

    /**
     * Sets the UUID of the player who the meta belongs to.
     * @param UUID the UUID of the player who the meta belongs to.
     */
    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    /**
     * Returns the username of the player who this meta belongs to.
     * @return the username of the player who this meta belongs to.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the username of the player who this meta belongs to.
     * @param name the username of the player who this meta belongs to.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the last channel the player received a message from.
     * @return the last channel the player received a message from.
     */
    public String getLastReceived() {
        return this.lastReceived;
    }

    /**
     * sets the channel the player last received a message from.
     * @param lastReceived the channel the player last received a message from.
     */
    public void setLastReceived(String lastReceived) {
        this.lastReceived = lastReceived;
    }

    /**
     * Returns the default channel this player has set.
     * @return the default channel this player has set.
     */
    public String getDefaultChannel() {
        return this.defaultChannel;
    }

    /**
     * Set the default channel this player has set.
     * @param defaultChannel the default channel this player has set.
     */
    public void setDefaultChannel(String defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

}
