package net.poweredbyawesome.moneyovertime.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Lax on 12/9/2017.
 */
public class PlayerInvoiceEvent extends Event {

    public static final HandlerList panHandlers = new HandlerList();
    private Player player;
    private double amount;
    private String category;

    public PlayerInvoiceEvent(Player player, double amount, String category) {
        this.player = player;
        this.amount = amount;
        this.category = category;
    }


    public static HandlerList getHandlerList() {
        return panHandlers;
    }

    @Override
    public HandlerList getHandlers() {
        return panHandlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public double getAmount() {
        return this.amount;
    }

    public String getCategory() {
        return this.category;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
