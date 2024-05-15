package me.leonrobi.keepinv;

import com.tcoded.folialib.FoliaLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public final class KeepInv extends JavaPlugin implements Listener {

    public FileConfiguration config = this.getConfig();
    private FileConfiguration dataConfig;
    private File dataFile;

    public final HashMap<Player, Long> timers = new HashMap<>();
    public final HashMap<Player, Integer> lives = new HashMap<>();

    private void severe(String msg) {
        getLogger().severe(msg + " Please contact leonrobi to fix!");
    }

    @Override
    public void onEnable() {
        config.addDefault("timer-seconds", 30 * 60);
        config.addDefault("death-limit", 3);
        config.addDefault("keep-inv-on-msg", "&eYou have You have %time%s or %lives% lives left until keep inventory is disabled.");
        config.addDefault("keep-inv-off-msg", "&cTime or lives have run out, keep inventory is off.");
        config.options().copyDefaults(true);
        this.saveConfig();

        File file = new File(getDataFolder() + "/data.yml");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    severe("Failed to create file.");
                    throw new RuntimeException();
                }
            } catch (IOException e) {
                severe("Failed to create file (e).");
                throw new RuntimeException(e);
            }
        }

        dataFile = file;
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        getServer().getPluginManager().registerEvents(this, this);

        startTimer();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new Placeholders(this).register();
        } else {
            getLogger().warning("No placeholderapi found");
        }
    }

    private void startTimer() {
        FoliaLib foliaLib = new FoliaLib(this);

        foliaLib.getImpl().runTimer(() -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                long currentTime = timers.get(p);
                if (currentTime > 0)
                    timers.put(p, currentTime - 1);
            }
        }, 20L, 20L);
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers())
            onQuit(p);

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            severe("Failed to save data.yml.");
            throw new RuntimeException(e);
        }
    }

    private String getTimeKey(Player player) {
        return player.getUniqueId() + "-time";
    }

    public String getLivesKey(Player player) {
        return player.getUniqueId() + "-lives";
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String timeKey = getTimeKey(player);

        long timeSecs;
        if (dataConfig.contains(timeKey)) {
            timeSecs = dataConfig.getLong(timeKey);
        } else {
            timeSecs = config.getLong("timer-seconds");
            dataConfig.set(timeKey, timeSecs);
        }

        timers.put(player, timeSecs);

        String livesKey = getLivesKey(player);

        int lives;
        if (dataConfig.contains(livesKey)) {
            lives = dataConfig.getInt(livesKey);
        } else {
            lives = config.getInt("death-limit");
            dataConfig.set(livesKey, lives);
        }

        this.lives.put(player, lives);
    }

    private void onQuit(Player player) {
        dataConfig.set(getTimeKey(player), timers.get(player));
        dataConfig.set(getLivesKey(player), lives.get(player));
        timers.remove(player);
        lives.remove(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        onQuit(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        int lives = this.lives.get(player);
        long timer = this.timers.get(player);

        if (lives == 0 || timer <= 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(config.getString("keep-inv-off-msg"))));
            event.setKeepInventory(false);
        } else {
            event.getDrops().clear();
            event.setKeepInventory(true);
            int newLives = this.lives.get(player) - 1;
            if (newLives >= 0) {
                this.lives.put(player, newLives);
            }

            String timeLeftMsg = Objects.requireNonNull(config.getString("keep-inv-on-msg"));
            timeLeftMsg = ChatColor.translateAlternateColorCodes('&', timeLeftMsg);
            timeLeftMsg = timeLeftMsg.
                    replaceAll("%time%", Placeholders.
                            convertSecondsToMinutes(timers.get(player)));
            timeLeftMsg = timeLeftMsg.replaceAll("%lives%", String.valueOf(newLives));

            player.sendMessage(timeLeftMsg);
        }
    }

}
