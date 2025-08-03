package nu.nerd.NerdClanChat.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "clanchat_invites")
public class Invite {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String channel;

    @DatabaseField(canBeNull = false)
    private String UUID;

    /**
     * Empty constructor required by ORMLite.
     */
    public Invite() {
    }

    /**
     * Contructor to create a new invite instance.
     * @param channel the channel the invite belongs to.
     * @param UUID the UUID of the player receiving this invite.
     */
    public Invite(String channel, String UUID) {
        this.setChannel(channel);
        this.setUUID(UUID);
    }

    /**
     * Returns the channel the invite belongs to.
     * @return the channel the invite belongs to.
     */
    public String getChannel() {
        return this.channel;
    }

    /**
     * Sets the channel the invite belongs to.
     * @param channel the channel the invite belongs to.
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Returns the UUID of the player who's receiving the invite.
     * @return the UUID of the player who's receiving the invite.
     */
    public String getUUID() {
        return this.UUID;
    }

    /**
     * Sets the UUID of the player who's receiving the invite.
     * @param UUID the UUID of the player who's receiving the invite.
     */
    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

}
