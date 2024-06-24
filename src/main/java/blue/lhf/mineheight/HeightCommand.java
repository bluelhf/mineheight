package blue.lhf.mineheight;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.util.Collections.emptyList;

public class HeightCommand extends Command {

    protected HeightCommand() {
        super("height", "Allows players to set their in-game height", "/height", emptyList());
        setPermission("mineheight.height");
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String alias, @NotNull final String[] args) throws IllegalArgumentException {
        if (!sender.hasPermission("mineheight.height.others")) {
            return emptyList();
        }

        if (args.length == 1) return sender.getServer().matchPlayer(args[0]).stream().map(Player::getName).toList();
        return emptyList();
    }

    @Override
    public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, @NotNull final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage("Only players can use this command");
            return true;
        }

        switch (args.length) {
            case 0 -> new HeightGUI(player).openFor(player);
            default -> {

                final Player target = player.getServer().getPlayer(args[0]);

                if (target == null) {
                    player.sendMessage("A player with the name '" + args[0] + "' or a similar one could not be found.");
                    return true;
                }

                if (!player.hasPermission("mineheight.height.others") && !player.equals(target)) {
                    player.sendMessage("You do not have permission to set other players' heights");
                    return true;
                }

                new HeightGUI(target).openFor(player);
            }
        }

        return false;
    }
}
