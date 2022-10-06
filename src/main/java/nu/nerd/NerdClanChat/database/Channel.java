package nu.nerd.NerdClanChat.database;



import io.ebean.annotation.NotNull;
import javax.persistence.*;

@Entity()
@Table(name = "clanchat_channels")
public class Channel {

    @Id
    private Integer id;

    @NotNull
    @Column(unique=true)
    private String name;

    @NotNull
    private String owner;

    @NotNull
    private String color;

    @NotNull
    private String textColor;

    @NotNull
    private String alertColor;

    private boolean pub;

    private boolean secret;


    public Channel() {
    }

    public Channel(String name, String ownerUUID) {
        this.setName(name);
        this.setOwner(ownerUUID);
        this.setColor("BLUE");
        this.setTextColor("GRAY");
        this.setAlertColor("GRAY");
        this.setSecret(false);
        this.setPub(false);
    }


    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTextColor() {
        return this.textColor;
    }

    public void setTextColor(String color) {
        this.textColor = color;
    }

    public String getAlertColor() {
        return this.alertColor;
    }

    public void setAlertColor(String color) {
        this.alertColor = color;
    }

    public boolean isPub() {
        return this.pub;
    }

    public void setPub(boolean isPublic) {
        this.pub = isPublic;
    }

    public boolean isSecret() {
        return this.secret;
    }

    public void setSecret(boolean isSecret) {
        this.secret = isSecret;
    }


}
