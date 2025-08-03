package nu.nerd.NerdClanChat.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents a bulletin in a clanchat channel.
 */
@DatabaseTable(tableName = "clanchat_bulletins")
public class Bulletin {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false)
    private String channel;

    @DatabaseField(canBeNull = false)
    private String message;


    /**
     * Empty constructor required by ORMLite.
     */
    public Bulletin() {
    }

    /**
     * Bulletin contructor.
     * @param channel The channel the bulletin belongs to.
     * @param message The message the bulletin contains.
     */
    public Bulletin(String channel, String message) {
        this.setChannel(channel);
        this.setMessage(message);
    }

    /**
     * Returns an instance of the channel this bulletin belongs to.
     * @return an instance of the channel this bulletin belongs to.
     */
    public String getChannel() {
        return this.channel;
    }

    /**
     * Set the instance of the channel this bulletin belongs to.
     * @param channel the instance of the channel this bulletin belongs to.
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Returns the message this bulletin contains.
     * @return the message this bulletin contains.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets the message this bulletin contains.
     * @param message the message this bulletin contains.
     */
    public void setMessage(String message) {
        this.message = message;
    }

}
