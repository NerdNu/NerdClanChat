package nu.nerd.NerdClanChat.database;

import io.ebean.annotation.NotNull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity()
@Table(name = "clanchat_playermeta")
public class PlayerMeta {

    @Id
    private Integer id;

    @NotNull
    @Column(unique=true)
    private String UUID;

    private String name;

    private String lastReceived;

    private String defaultChannel;


    public PlayerMeta() {
    }

    public PlayerMeta(String UUID) {
        this.setUUID(UUID);
    }


    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUUID() {
        return this.UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastReceived() {
        return this.lastReceived;
    }

    public void setLastReceived(String lastReceived) {
        this.lastReceived = lastReceived;
    }

    public String getDefaultChannel() {
        return this.defaultChannel;
    }

    public void setDefaultChannel(String defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

}
