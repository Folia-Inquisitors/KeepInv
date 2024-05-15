package me.leonrobi.keepinv;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
            return keepInv.lives.get(p).toString();
        } else if (params.equalsIgnoreCase("lives_hearts")){
            return new String(new char[keepInv.lives.get(p)]).replace("\0", "❤");
        } else if (params.equalsIgnoreCase("timer_secs")) {
            return keepInv.timers.get(p).toString();
        } else if (params.equalsIgnoreCase("timer_mins")) {
            return String.valueOf((keepInv.timers.get(p) / 60));
        } else if (params.equalsIgnoreCase("timer")) {
            return String.valueOf(convertSecondsToMinutes(keepInv.timers.get(p)));
        }
        return null;
    }

}
