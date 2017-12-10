package net.poweredbyawesome.moneyovertime;

import net.milkbowl.vault.economy.Economy;
import net.poweredbyawesome.moneyovertime.events.PlayerInvoiceEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public final class MoneyOvertime extends JavaPlugin implements Listener {

    private static Economy econ = null;
    int time;
    public HashMap<UUID, Integer> invoice = new HashMap<>();
    public HashMap<String, Permission> perms = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        time = getConfig().getInt("Time") * 20;
        loadPerms();
        Bukkit.getPluginManager().registerEvents(this, this);
        if (!setupEconomy()) {
            getLogger().log(Level.WARNING, "No vault plugin found, only commands will work!");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        Player p = ev.getPlayer();
        maekMoneh(p, getBasicTime(p));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        Player p = ev.getPlayer();
        spendMoneh(p);
    }


    public void maekMoneh(Player p, int rtime) {
        if (!invoice.containsKey(p.getUniqueId())) {
            if (hasWildcard(p) || p.hasPermission("money.overtime.nope")) {
                getLogger().log(Level.INFO, p.getName() + " has a wildcard permission ¯\\_(ツ)_/¯");
                return;
            }
            invoice.put(p.getUniqueId(),Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                for (String s : getConfig().getConfigurationSection("Deposits").getKeys(false)) {
                    if (p.hasPermission(perms.get(s))) {
                        if (econ != null) {
                            double amount = getConfig().getInt("Deposits."+s+".amount");
                            PlayerInvoiceEvent playerInvoiceEvent = new PlayerInvoiceEvent(p, amount, s);
                            Bukkit.getPluginManager().callEvent(playerInvoiceEvent);
                            amount = playerInvoiceEvent.getAmount();
                            econ.depositPlayer(p, amount);
                        }
                        if (getConfig().getStringList("Deposits."+s+".commands") != null) {
                            for (String command : getConfig().getStringList("Deposits."+s+".commands")) {
                                getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%",p.getName()));
                            }
                        }
                    }
                }
            },rtime,rtime));
        }
    }

    @Deprecated
    public void spendMoneh(Player p) {
        removePlayer(p);
    }

    public int getBasicTime(Player p) {
        for (String s : getConfig().getConfigurationSection("Deposits").getKeys(false)) {
            if (p.hasPermission("money.overtime."+s)) {
                if (getConfig().getString("Deposits."+s+"time") != null) {
                    return getConfig().getInt("Deposits."+s+"time");
                }
            }
        }
        return time;
    }

    public boolean removePlayer(Player p) {
        if (invoice.containsKey(p.getUniqueId())) {
            Bukkit.getScheduler().cancelTask(invoice.get(p.getUniqueId()));
            invoice.remove(p.getUniqueId());
            return true;
        }
        return false;
    }

    public void loadPerms() {
        perms.put("ireallyknowwhatimdoingiswear", new Permission("I.Really.Know.What.Im.Doing.I.Swear", PermissionDefault.FALSE));
        for (String s : getConfig().getConfigurationSection("Deposits").getKeys(false)) {
            perms.put(s, new Permission("money.overtime."+s, PermissionDefault.FALSE));
        }
    }

    public boolean hasWildcard(Player p) { //so it has come to this, building around stupid people.
        return p.hasPermission(perms.get("ireallyknowwhatimdoingiswear"));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}