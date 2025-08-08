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
    private String uuid;

    /**
     * Empty constructor required by ORMLite.
     */
    public Invite() {
    }

    /**
     * Contructor to create a new invite instance.
     * @param channel the channel the invite belongs to.
     * @param uuid the UUID of the player receiving this invite.
     */
    public Invite(String channel, String uuid) {
        this.setChannel(channel);
        this.setUuid(uuid);
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
    public String getUuid() {
        return this.uuid;
    }

    /**
     * Sets the UUID of the player who's receiving the invite.
     * @param uuid the UUID of the player who's receiving the invite.
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
