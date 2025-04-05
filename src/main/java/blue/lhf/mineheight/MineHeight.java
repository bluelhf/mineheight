package blue.lhf.mineheight;

import blue.lhf.mineheight.model.Height;
import blue.lhf.mineheight.model.HeightUnit;
import blue.lhf.mineheight.util.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineHeight extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getCommandMap().register("height", new HeightCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public final NamespacedKey HEIGHT_DATA = new NamespacedKey(this, "height");

    private CraftPersistentDataContainer getPDC(final Player player) {
        return player.getBukkitEntity().getPersistentDataContainer();
    }

    public Height<?> getHeight(final Player player) {
        final Height<HeightUnit.Metres> heightFromAttributes = Height.fromMetres(getHeightAttr(player));
        try {
            return getPDC(player).getOrDefault(HEIGHT_DATA, HeightPersistentDataType.INSTANCE, heightFromAttributes);
        } catch (final IllegalArgumentException e) {
            return heightFromAttributes;
        }
    }

    public boolean setHeight(final Player player, final Height<?> height) {
        if (getHeight(player).metres() < 0.5 || getHeight(player).metres() > 3.0) {
            getPDC(player).set(HEIGHT_DATA, HeightPersistentDataType.INSTANCE, height);
            setHeightAttrRaw(player, height.metres()); // if we're already out of bounds, don't validate
            return true;
        }

        final double clampedMetres = Math.min(Math.max(height.metres(), 0.50), 3.0);
        getPDC(player).set(HEIGHT_DATA, HeightPersistentDataType.INSTANCE, height.withMetres(clampedMetres));
        setHeightAttrRaw(player, clampedMetres);
        return clampedMetres == height.metres(); // no change
    }

    public double getHeightAttr(final Player player) {
        final AttributeInstance attribute = player.getAttribute(Attributes.SCALE);
        return (attribute == null ? 1 : attribute.getValue()) * 1.8;
    }

    public void setHeightAttrRaw(final Player player, final double height) {
        final AttributeInstance attribute = player.getAttribute(Attributes.SCALE);
        if (attribute != null) {
            attribute.setBaseValue(height / 1.8);
        }
    }
}
