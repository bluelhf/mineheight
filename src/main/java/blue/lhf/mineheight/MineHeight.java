package blue.lhf.mineheight;

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
}
