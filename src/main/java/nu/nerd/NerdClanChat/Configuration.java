package nu.nerd.NerdClanChat;


public class Configuration {

    private final NerdClanChat plugin;
    public Integer BULLETIN_LIMIT;
    public boolean DEBUG;

    public Configuration(NerdClanChat instance) {
        plugin = instance;
        plugin.saveDefaultConfig();
        this.load();
    }

    public void save() {
        plugin.saveConfig();
    }

    public void load() {
        plugin.reloadConfig();
        BULLETIN_LIMIT = plugin.getConfig().getInt("bulletins.limit-on-login", 0);
        DEBUG = plugin.getConfig().getBoolean("debug", false);
    }

}