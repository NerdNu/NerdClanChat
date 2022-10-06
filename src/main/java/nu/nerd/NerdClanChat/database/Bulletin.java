package nu.nerd.NerdClanChat.database;

import io.ebean.annotation.NotNull;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity()
@Table(name = "clanchat_bulletins")
public class Bulletin {

    @Id
    private Integer id;

    @NotNull
    private String channel;

    @NotNull
    private String message;


    public Bulletin() {
    }

    public Bulletin(String channel, String message) {
        this.setChannel(channel);
        this.setMessage(message);
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

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
