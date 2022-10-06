package nu.nerd.NerdClanChat.database;

import io.ebean.annotation.NotNull;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity()
@Table(name = "clanchat_invites")
public class Invite {

    @Id
    private Integer id;

    @NotNull
    private String channel;

    @NotNull
    private String UUID;


    public Invite() {
    }

    public Invite(String channel, String UUID) {
        this.setChannel(channel);
        this.setUUID(UUID);
    }


    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getChannel() {
        return this.channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUUID() {
        return this.UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

}
