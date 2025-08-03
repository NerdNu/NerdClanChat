package nu.nerd.NerdClanChat.database;



import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "clanchat_channels")
public class Channel {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, unique = true)
    private String name;

    @DatabaseField(canBeNull = false)
    private String owner;

    @DatabaseField(canBeNull = false)
    private String color;

    @DatabaseField(canBeNull = false)
    private String textColor;

    @DatabaseField(canBeNull = false)
    private String alertColor;

    @DatabaseField
    private boolean pub;

    @DatabaseField
    private boolean secret;


    /**
     * Empty constructor required by ORMLite.
     */
    public Channel() {
    }

    /**
     * The constructor to make an instance of a channel.
     * @param name The name of the channel.
     * @param ownerUUID The UUID of the player who will own the channel.
     */
    public Channel(String name, String ownerUUID) {
        this.setName(name);
        this.setOwner(ownerUUID);
        this.setColor("BLUE");
        this.setTextColor("GRAY");
        this.setAlertColor("GRAY");
        this.setSecret(false);
        this.setPub(false);
    }

    /**
     * Returns the name of the channel.
     * @return the name of the channel.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the channel.
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the owner of the channel.
     * @return the owner of the channel.
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * Sets the owner of the channel as a string representation of their UUID.
     * @param owner the string UUID of the player.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Returns the colour of the channel.
     * @return the colour of the channel.
     */
    public String getColor() {
        return this.color;
    }

    /**
     * Sets the colour of the channel.
     * @param color the colour (either one of the main 16 vanilla ones or a hex code with a leading #)
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Returns the textcolor of the channel.
     * @return the textcolor of the channel.
     */
    public String getTextColor() {
        return this.textColor;
    }

    /**
     * Sets the textcolor of the channel.
     * @param color the colour (either one of the main 16 vanilla ones or a hex code with a leading #)
     */
    public void setTextColor(String color) {
        this.textColor = color;
    }

    /**
     * Returns the alertcolor of the channel.
     * @return the alertcolor of the channel.
     */
    public String getAlertColor() {
        return this.alertColor;
    }

    /**
     * Sets the alertcolor of the channel.
     * @param color the colour (either one of the main 16 vanilla ones or a hex code with a leading #)
     */
    public void setAlertColor(String color) {
        this.alertColor = color;
    }

    /**
     * Returns whether the channel is public or not.
     * @return whether the channel is public or not.
     */
    public boolean isPub() {
        return this.pub;
    }

    /**
     * Sets whether the channel is public or not.
     * @param isPublic true if public, false if not.
     */
    public void setPub(boolean isPublic) {
        this.pub = isPublic;
    }

    /**
     * Returns whether the channel is secret or not.
     * @return whether the channel is secret or not.
     */
    public boolean isSecret() {
        return this.secret;
    }

    /**
     * Sets whether the channel is secret or not.
     * @param isSecret true if secret, false if not.
     */
    public void setSecret(boolean isSecret) {
        this.secret = isSecret;
    }


}
