package nu.nerd.NerdClanChat.database;


import io.ebean.Query;
import io.ebean.SqlUpdate;
import nu.nerd.NerdClanChat.NerdClanChat;

import java.util.ArrayList;
import java.util.List;

public class BulletinsTable {


    NerdClanChat plugin;


    public BulletinsTable(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    public List<Bulletin> getChannelBulletins(String channel) {
        List<Bulletin> bulletins = new ArrayList<Bulletin>();
        Query<Bulletin> query = plugin.getDatabase().find(Bulletin.class).where().ieq("channel", channel).query();
        if (query != null) {
            for (Bulletin bulletin : query.findList()) {
                bulletins.add(bulletin);
            }
        }
        return bulletins;
    }


    public void deleteChannelBulletins(String channel) {
        String query = "delete from clanchat_bulletins where channel=:channel";
        SqlUpdate update = plugin.getDatabase().createSqlUpdate(query).setParameter("channel", channel);
        update.execute();
    }


    public void delete(Bulletin bulletin) {
        plugin.getDatabase().delete(bulletin);
    }


    public void save(Bulletin bulletin) {
        plugin.getDatabase().save(bulletin);
    }


}
