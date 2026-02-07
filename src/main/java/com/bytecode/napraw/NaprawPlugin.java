package com.bytecode.napraw;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

public class NaprawPlugin extends JavaPlugin implements CommandExecutor {
    private static final int ITEM_COST = 1;
    private static final int ARMOR_COST = 2;

    @Override
    public void onEnable() {
        if (getCommand("napraw") != null) {
            getCommand("napraw").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Tylko gracz moze uzyc tej komendy.");
            return true;
        }

        Player player = (Player) sender;
        List<ItemStack> toRepair = new ArrayList<>();
        int totalCost = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            if (!(item.getItemMeta() instanceof Damageable)) {
                continue;
            }

            Damageable damageable = (Damageable) item.getItemMeta();
            if (damageable.getDamage() <= 0) {
                continue;
            }

            totalCost += isArmor(item.getType()) ? ARMOR_COST : ITEM_COST;
            toRepair.add(item);
        }

        if (totalCost == 0) {
            player.sendMessage(ChatColor.YELLOW + "Nie masz nic do naprawy.");
            return true;
        }

        int currentXp = getTotalExperience(player);
        if (currentXp < totalCost) {
            player.sendMessage(ChatColor.RED + "Potrzebujesz " + totalCost + " XP, a masz tylko " + currentXp + " XP.");
            return true;
        }

        setTotalExperience(player, currentXp - totalCost);
        for (ItemStack item : toRepair) {
            Damageable damageable = (Damageable) item.getItemMeta();
            damageable.setDamage(0);
            item.setItemMeta(damageable);
        }

        player.sendMessage(ChatColor.GREEN + "Naprawiono ekwipunek za " + totalCost + " XP.");
        return true;
    }

    private boolean isArmor(Material material) {
        String name = material.name();
        return name.endsWith("_HELMET")
            || name.endsWith("_CHESTPLATE")
            || name.endsWith("_LEGGINGS")
            || name.endsWith("_BOOTS");
    }

    private int getTotalExperience(Player player) {
        int exp = Math.round(player.getExp() * player.getExpToLevel());
        int level = player.getLevel();
        return exp + getExpAtLevel(level);
    }

    private void setTotalExperience(Player player, int totalExp) {
        if (totalExp < 0) {
            totalExp = 0;
        }
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);
        player.giveExp(totalExp);
    }

    private int getExpAtLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        }
        if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        }
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }
}
