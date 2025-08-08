/**
 * NerdClanChat
 * Java port of CHClanchat (https://github.com/NerdNu/CHClanChat)
 * by redwall_hp (http://github.com/redwallhp)
 */

package nu.nerd.NerdClanChat;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import net.kyori.adventure.text.Component;
import nu.nerd.NerdClanChat.caching.*;
import nu.nerd.NerdClanChat.database.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;


public final class NerdClanChat extends JavaPlugin {

    private static final String DATABASE_URL = "jdbc:sqlite:plugins/NerdClanChat/NerdClanChat.db";
    private ConnectionSource connectionSource;
    private Dao<Bulletin, Integer> bulletinsDao;
    private Dao<Channel, Integer> channelsDao;
    private Dao<ChannelMember, Integer> channelMembersDao;
    private Dao<Invite, Integer> invitesDao;
    private Dao<PlayerMeta, Integer> playerMetaDao;

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
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            TableUtils.createTableIfNotExists(connectionSource, Bulletin.class);
            TableUtils.createTableIfNotExists(connectionSource, Channel.class);
            TableUtils.createTableIfNotExists(connectionSource, ChannelMember.class);
            TableUtils.createTableIfNotExists(connectionSource, Invite.class);
            TableUtils.createTableIfNotExists(connectionSource, PlayerMeta.class);

            bulletinsDao = DaoManager.createDao(connectionSource, Bulletin.class);
            channelsDao = DaoManager.createDao(connectionSource, Channel.class);
            channelMembersDao = DaoManager.createDao(connectionSource, ChannelMember.class);
            invitesDao = DaoManager.createDao(connectionSource, Invite.class);
            playerMetaDao = DaoManager.createDao(connectionSource, PlayerMeta.class);

            return true;
        } catch(SQLException e) {
            log("Database setup failed!", Level.SEVERE);
            return false;
        }
    }


    public void logDebug(String msg) {
        if (config.DEBUG) {
            log(msg, Level.INFO);
        }
    }

    public void log(String message, Level level) {
        getComponentLogger().warn(Component.text(message), level);
    }

    public Dao<Bulletin, Integer> getBulletinsDao() {
        return bulletinsDao;
    }

    public Dao<Channel, Integer> getChannelsDao() {
        return channelsDao;
    }

    public Dao<ChannelMember, Integer> getChannelMembersDao() {
        return channelMembersDao;
    }

    public Dao<Invite, Integer> getInvitesDao() {
        return invitesDao;
    }

    public Dao<PlayerMeta, Integer> getPlayerMetaDao() {
        return playerMetaDao;
    }

    public PlayerMetaTable getPlayerMetaTable() {
        return playerMetaTable;
    }

}
