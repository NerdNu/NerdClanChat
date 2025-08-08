package nu.nerd.NerdClanChat.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "clanchat_playermeta")
public class PlayerMeta {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, unique = true)
    private String uuid;

    @DatabaseField
    private String name;

    @DatabaseField
    private String last_received;

    @DatabaseField
    private String default_channel;

    /**
     * Empty constructor required by ORMLite.
     */
    public PlayerMeta() {
    }

    /**
     * Constructor to create a new playermeta instance.
     * @param uuid the UUID of the player who this meta belongs to.
     */
    public PlayerMeta(String uuid) {
        this.setUuid(uuid);
    }

    /**
     * Returns the UUID of the player who the meta belongs to.
     * @return the UUID of the player who the meta belongs to.
     */
    public String getUuid() {
        return this.uuid;
    }

    /**
     * Sets the UUID of the player who the meta belongs to.
     * @param uuid the UUID of the player who the meta belongs to.
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
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
    public String getLast_received() {
        return this.last_received;
    }

    /**
     * sets the channel the player last received a message from.
     * @param last_received the channel the player last received a message from.
     */
    public void setLast_received(String last_received) {
        this.last_received = last_received;
    }

    /**
     * Returns the default channel this player has set.
     * @return the default channel this player has set.
     */
    public String getDefault_channel() {
        return this.default_channel;
    }

    /**
     * Set the default channel this player has set.
     * @param default_channel the default channel this player has set.
     */
    public void setDefault_channel(String default_channel) {
        this.default_channel = default_channel;
    }

}
