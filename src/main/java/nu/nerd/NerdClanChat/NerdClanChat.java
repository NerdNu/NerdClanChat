/**
 * NerdClanChat
 * Java port of CHClanchat (https://github.com/NerdNu/CHClanChat)
 * by redwall_hp (http://github.com/redwallhp)
 */

package nu.nerd.NerdClanChat;

import nu.nerd.NerdClanChat.caching.*;
import nu.nerd.NerdClanChat.database.*;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.PersistenceException;
import java.util.ArrayList;


public final class NerdClanChat extends JavaPlugin {


    public ChannelsTable channelsTable;
    public ChannelMembersTable channelMembersTable;
    public PlayerMetaTable playerMetaTable;
    public BulletinsTable bulletinsTable;
    public InvitesTable invitesTable;

    public ChannelCache channelCache;
    public PlayerMetaCache playerMetaCache;
    public TransientPlayerCache transientPlayerCache;

    public PlayerMetaPersistTask playerMetaPersistTask;
    public Configuration config;


    @Override
    public void onEnable() {

        // Config
        this.config = new Configuration(this);

        // Database
        this.setUpDatabase();
        this.channelsTable = new ChannelsTable(this);
        this.channelMembersTable = new ChannelMembersTable(this);
        this.playerMetaTable = new PlayerMetaTable(this);
        this.bulletinsTable = new BulletinsTable(this);
        this.invitesTable = new InvitesTable(this);

        // Cache
        this.channelCache = new ChannelCache(this);
        this.playerMetaCache = new PlayerMetaCache(this);
        this.transientPlayerCache = new TransientPlayerCache(this);

        // Event handlers
        getServer().getPluginManager().registerEvents(new PluginListener(this), this);

        // Scheduled Tasks
        this.playerMetaPersistTask = new PlayerMetaPersistTask(this);
        this.playerMetaPersistTask.runTaskTimer(this, 12000L, 12000L);

        // Commands
        ChatCommands chatCommands = new ChatCommands(this);
        this.getCommand("clanchat").setExecutor(new ClanChatCommand(this));
        this.getCommand("c").setExecutor(chatCommands);
        this.getCommand("cq").setExecutor(chatCommands);
        this.getCommand("crq").setExecutor(chatCommands);
        this.getCommand("cqr").setExecutor(chatCommands);
        this.getCommand("ca").setExecutor(chatCommands);
        this.getCommand("cme").setExecutor(chatCommands);
        this.getCommand("cs").setExecutor(chatCommands);
        this.getCommand("cr").setExecutor(chatCommands);
        this.getCommand("cm").setExecutor(chatCommands);
        this.getCommand("cb").setExecutor(chatCommands);

    }


    @Override
    public void onDisable() {
        this.playerMetaPersistTask.cancel();
        this.logDebug("Writing player meta to persistence database...");
        this.playerMetaCache.persistCache();
        this.logDebug("Done.");
    }


    public boolean setUpDatabase() {
        try {
            getDatabase().find(Channel.class).findRowCount();
        } catch (PersistenceException ex) {
            getLogger().info("Initializing database.");
            installDDL();
            return true;
        }
        return false;
    }


    @Override
    public ArrayList<Class<?>> getDatabaseClasses() {
        ArrayList<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Channel.class);
        list.add(ChannelMember.class);
        list.add(PlayerMeta.class);
        list.add(Bulletin.class);
        list.add(Invite.class);
        return list;
    }


    public void logDebug(String msg) {
        if (config.DEBUG) {
            getLogger().info(msg);
        }
    }


}
