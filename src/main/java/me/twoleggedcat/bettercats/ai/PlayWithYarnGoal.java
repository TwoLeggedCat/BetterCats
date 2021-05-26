package me.twoleggedcat.bettercats.ai;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Item;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

public class PlayWithYarnGoal implements Goal<Cat> {
    private final GoalKey<Cat> key;
    private final Cat cat;
    private Item string;

    public PlayWithYarnGoal(Plugin plugin, Cat cat) {
        this.key = GoalKey.of(Cat.class, new NamespacedKey(plugin, "play_with_string"));
        this.cat = cat;
    }

    @Override
    public boolean shouldActivate() {
        return findString();
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
        if (!findString())
            return;
        cat.getPathfinder().moveTo(string.getLocation(), 1.2);
        if (string.getLocation().distanceSquared(cat.getLocation()) < 1) {
            string.setVelocity(new Vector(randomDouble(-0.25, 0.25), randomDouble(-0.25, 0.25), randomDouble(-0.25, 0.25)));
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

    private Item getNearbyStringItem() {
        Collection<Item> strings = cat.getWorld().getNearbyEntitiesByType(Item.class, cat.getLocation(), 10, 5, 10,
                item -> item.getItemStack().getType() == Material.STRING);
        if (strings.size() == 0)
            return null;
        int rand = randomInt(0, strings.size());
        int i = 0;
        for (Item string : strings) {
            if (i == rand)
                return string;
            i++;
        }
        return null;
    }

    private boolean findString() {
        if (string == null || !string.isValid()) {
            string = getNearbyStringItem();
            return string != null;
        } else return true;
    }

    private int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    private double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
