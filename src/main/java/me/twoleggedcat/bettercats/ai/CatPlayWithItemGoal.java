package me.twoleggedcat.bettercats.ai;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Item;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

public class CatPlayWithItemGoal implements Goal<Cat> {
    private final GoalKey<Cat> key;
    private final Cat cat;
    private final Material targetType;
    private Item targetItem;
    private int ticksPlayed;

    public CatPlayWithItemGoal(Plugin plugin, Cat cat, Material targetType) {
        this.key = GoalKey.of(Cat.class, new NamespacedKey(plugin, "play_with_item"));
        this.cat = cat;
        this.targetType = targetType;
    }

    @Override
    public boolean shouldActivate() {
        return findItem();
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        cat.getPathfinder().stopPathfinding();
        cat.setTarget(null);
    }

    @Override
    public void tick() {
        if (!findItem())
            return;
        cat.getPathfinder().moveTo(targetItem.getLocation(), 1.2);
        if (targetItem.getLocation().distanceSquared(cat.getLocation()) < 1) {
            if (ticksPlayed > 600 && targetItem.getItemStack().getType().isEdible()) {
                targetItem.remove();
                cat.getWorld().playSound(cat.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1, 1);
            } else {
                targetItem.setVelocity(new Vector(randomDouble(-0.25, 0.25), randomDouble(-0.25, 0.25), randomDouble(-0.25, 0.25)));
                ticksPlayed++;
            }
        }
    }

    @Override
    public @NotNull GoalKey<Cat> getKey() {
        return key;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
    }

    private Item getNearbyTargetItem() {
        Collection<Item> items = cat.getWorld().getNearbyEntitiesByType(Item.class, cat.getLocation(), 10, 5, 10,
                item -> item.getItemStack().getType() == targetType);
        if (items.size() == 0)
            return null;
        int rand = randomInt(0, items.size());
        int i = 0;
        for (Item item : items) {
            if (i == rand)
                return item;
            i++;
        }
        return null;
    }

    private boolean findItem() {
        if (targetItem == null || !targetItem.isValid()) {
            targetItem = getNearbyTargetItem();
            return targetItem != null;
        } else
            return true;
    }

    private int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    private double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
