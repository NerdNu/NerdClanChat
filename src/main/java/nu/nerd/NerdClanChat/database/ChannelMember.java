package nu.nerd.NerdClanChat.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "clanchat_members")
public class ChannelMember {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String channel;

    @DatabaseField(canBeNull = false)
    private String UUID;

    @DatabaseField
    private String name;

    @DatabaseField
    private boolean manager;

    @DatabaseField
    private boolean subscribed;

    /**
     * Empty constructor required by ORMLite.
     */
    public ChannelMember() {
    }

    /**
     * Contructor to create a new channel member instance.
     * @param channel The channel this member will belong to.
     * @param UUID The UUID of this member.
     * @param name The name of this member.
     * @param isManager If this member is a manager.
     */
    public ChannelMember(String channel, String UUID, String name, boolean isManager) {
        this.setChannel(channel);
        this.setUUID(UUID);
        this.setName(name);
        this.setSubscribed(true);
        this.setManager(isManager);
    }

    /**
     * Returns the channel this member belongs to.
     * @return the channel this member belongs to.
     */
    public String getChannel() {
        return this.channel;
    }

    /**
     * Sets the channel this member belongs to.
     * @param channel the channel this member is being added to.
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Returns the UUID of this member.
     * @return the UUID of this member.
     */
    public String getUUID() {
        return this.UUID;
    }

    /**
     * Sets the UUID of this member.
     * @param UUID the UUID of this member.
     */
    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    /**
     * Returns the username of this member.
     * @return the username of this member.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the username of this member.
     * @param name the username of this member.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether this member is a manager of the channel or not.
     * @return true if they are, false if not.
     */
    public boolean isManager() {
        return this.manager;
    }

    /**
     * Sets whether this member is a manager of the channel or not.
     * @param isManager true if they are, false if not.
     */
    public void setManager(boolean isManager) {
        this.manager = isManager;
    }

    /**
     * Returns whether this member is subscribed to the channel or not.
     * @return true if they are, false if not.
     */
    public boolean isSubscribed() {
        return this.subscribed;
    }

    /**
     * Sets whether this member is a manager of the channel or not.
     * @param isSubscribed true if they are, false if not.
     */
    public void setSubscribed(boolean isSubscribed) {
        this.subscribed = isSubscribed;
    }
}
