package me.leonrobi.keepinv;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Placeholders extends PlaceholderExpansion {

    private final KeepInv keepInv;

    public Placeholders(KeepInv keepInv) {
        this.keepInv = keepInv;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "keepinv";
    }

    @Override
    public @NotNull String getAuthor() {
        return "leonrobi";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    public static String convertSecondsToMinutes(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (!player.isOnline()) {
            return null;
        }
        Player p = (Player)player;

        if (params.equalsIgnoreCase("lives")) {
            int lives = keepInv.lives.get(p);
            if (lives == 0)
                return "";
            else
                return String.valueOf(lives);
        } else if (params.equalsIgnoreCase("lives_hearts")){
            return new String(new char[keepInv.lives.get(p)]).replace("\0", "❤");
        } else if (params.equalsIgnoreCase("timer_secs")) {
            long time = keepInv.timers.get(p);
            if (time == 0)
                return "";
            else
                return String.valueOf(time);
        } else if (params.equalsIgnoreCase("timer_mins")) {
            long mins = (keepInv.timers.get(p) / 60) + 1L;
            if (mins == 0)
                return "";
            else
                return String.valueOf(mins);
        } else if (params.equalsIgnoreCase("info")) {
            if (keepInv.timers.get(p) <= 0 || keepInv.lives.get(p) <= 0) {
                return "";
            } else {
                return ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(keepInv.config.getString("info-msg")));
            }
        } else if (params.equalsIgnoreCase("timer")) {
            long time = keepInv.timers.get(p);
            if (time <= 0 || keepInv.lives.get(p) <= 0) {
                return "";
            } else {
                return String.valueOf(convertSecondsToMinutes(keepInv.timers.get(p)));
            }
        }
        return null;
    }

}
