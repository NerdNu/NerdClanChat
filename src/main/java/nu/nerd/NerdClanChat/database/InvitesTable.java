package nu.nerd.NerdClanChat.database;

import io.ebean.Query;
import io.ebean.SqlUpdate;
import nu.nerd.NerdClanChat.NerdClanChat;

public class InvitesTable {


    NerdClanChat plugin;


    public InvitesTable(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    public void deleteChannelInvites(String channel) {
        String query = "delete from clanchat_invites where channel=:channel";
        SqlUpdate update = plugin.getDatabase().createSqlUpdate(query).setParameter("channel", channel);
        update.execute();
    }


    public boolean alreadyInvited(String UUID, String channel) {
        Query<Invite> query = plugin.getDatabase().find(Invite.class).where()
                .ieq("uuid", UUID)
                .ieq("channel", channel)
                .query();
        return query != null && query.findCount() > 0;
    }


    public void closeInvitation(String UUID, String channel) {
        String query = "delete from clanchat_invites where uuid=:uuid and channel=:channel";
        SqlUpdate update = plugin.getDatabase().createSqlUpdate(query)
                .setParameter("uuid", UUID)
                .setParameter("channel", channel);
        update.execute();
    }


    public void save(Invite invite) {
        plugin.getDatabase().save(invite);
    }


}
