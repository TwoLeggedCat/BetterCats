package me.twoleggedcat.bettercats;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import me.twoleggedcat.bettercats.ai.CatPlayWithItemGoal;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {
    public final HashMap<UUID, Integer> purrTimes = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        // bStats
        new Metrics(this, 11499);
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onEntityLoad(EntityAddToWorldEvent e) {
        if (e.getEntityType() == EntityType.CAT) {
            Cat cat = (Cat) e.getEntity();
            Bukkit.getMobGoals().addGoal(cat, 8, new CatPlayWithItemGoal(this, cat, Material.SALMON));
            Bukkit.getMobGoals().addGoal(cat, 8, new CatPlayWithItemGoal(this, cat, Material.STRING));
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getType() == InventoryType.CHEST) {
            Collection<Entity> entities = e.getPlayer().getWorld().getNearbyEntities(e.getPlayer().getLocation(), 10, 10, 10);
            for (Entity entity : entities) {
                if (Math.random() < 0.15 && entity instanceof Cat cat) {
                    if (!cat.hasLineOfSight(inv.getLocation()))
                        return;
                    cat.lookAt(inv.getLocation());
                    cat.getWorld().playSound(cat.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1, 1);
                    inv.removeItem(new ItemStack(Material.SALMON, 1));
                    Item salmon = (Item) cat.getWorld().spawnEntity(cat.getLocation().add(cat.getLocation().getDirection()), EntityType.DROPPED_ITEM);
                    salmon.setItemStack(new ItemStack(Material.SALMON));
                }
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent e) {
        Entity rightClicked = e.getRightClicked();
        if (rightClicked.getType() != EntityType.CAT || purrTimes.containsKey(rightClicked.getUniqueId()))
            return;
        Cat cat = (Cat) rightClicked;
        double rand = Math.random();
        if (rand < 0.3) {
            purrTimes.put(cat.getUniqueId(), 10);
            new BukkitRunnable() {
                @Override
                public void run() {
                    cat.getWorld().playSound(cat.getLocation(), Sound.ENTITY_CAT_PURR, 10f, 1.75f);
                    if (Math.random() <= 0.25)
                        cat.getWorld().playSound(cat.getLocation(), Sound.ENTITY_CAT_PURREOW, 10f, 1.75f);
                    int timeLeft = purrTimes.get(cat.getUniqueId());
                    if (timeLeft <= 0) {
                        purrTimes.remove(cat.getUniqueId());
                        this.cancel();
                        return;
                    }
                    purrTimes.put(cat.getUniqueId(), timeLeft - 1);
                }
            }.runTaskTimer(this, 0, 30);
        } else if (rand < 0.6)
            cat.getWorld().playSound(cat.getLocation(), Sound.ENTITY_CAT_PURREOW, 2f, 1f);
        else if (rand < 0.99)
            cat.getWorld().playSound(cat.getLocation(), Sound.ENTITY_CAT_AMBIENT, 2f, 1f);
        else
            cat.getWorld().playSound(cat.getLocation(), Sound.ENTITY_CAT_HISS, 2f, 1f);
    }
}
