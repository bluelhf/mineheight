package blue.lhf.mineheight;

import blue.lhf.mineheight.gui.HeightGUI;
import blue.lhf.mineheight.model.Height;
import blue.lhf.mineheight.model.HeightUnits;
import blue.lhf.mineheight.util.EveryNth;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@SuppressWarnings("SameReturnValue")
public class HeightCommand extends Command {

    protected HeightCommand() {
        super("height", "Allows players to set their in-game height using a GUI", "/height", emptyList());
        setPermission("mineheight.height");
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String alias, @NotNull final String[] args) throws IllegalArgumentException {
        if (!sender.hasPermission("mineheight.height.others")) {
            return super.tabComplete(sender, alias, args);
        }

        if (args.length == 1) {
            return sender.getServer().matchPlayer(args[0]).stream()
                    .map(Player::getName).filter(suggestion -> suggestion.startsWith(args[0])).toList();
        }

        if (args.length >= 1) {
            final Deque<String> process = new ArrayDeque<>(Arrays.asList(args));
            assert process.peek() != null;

            if (sender.getServer().getPlayer(process.peek()) != null) {
                process.poll();
            }

            final String height = process.peek();
            if (height == null) return emptyList();
            if (args.length > 2) return emptyList();
            final List<String> suggestions = new ArrayList<>();

            if (height.matches("^\\d{0,3}")) {
                suggestions.addAll(Stream.iterate(150, i -> i + 10).limit(7)
                        .map(i -> i + " cm").filter(suggestion -> suggestion.startsWith(height)).collect(EveryNth.collector(3)));
            }
            if (height.matches("^[45678]?'?\\d?\"?")) {
                suggestions.addAll(Stream.of("5", "6", "7")
                        .flatMap(ft -> Stream.iterate(0, in -> in <= 9, i -> i + 1).map(in -> ft + "'" + in + "\""))
                        .filter(suggestion -> suggestion.startsWith(height)).collect(EveryNth.collector(5)));
            }
            if (height.matches("[12]?\\.?\\d{0,2}m?")) {
                suggestions.addAll(Stream.iterate(15, i -> i + 1).limit(7)
                        .map(i -> String.format("%.1f m", i / 10.0))
                        .filter(suggestion -> suggestion.startsWith(height)).collect(EveryNth.collector(3)));
            }

            return suggestions;
        }

        return emptyList();
    }

    @Override
    public boolean execute(@NotNull final CommandSender sender, @NotNull final String label, @NotNull final String @NotNull [] args) {
        // attempt to make heights from both all the arguments and everything except the first argument
        // this is because the first argument may be a player name

        final String argument = String.join(" ", args).trim();
        final Height<?> height = HeightUnits.parse(argument);

        final String[] rest = new String[Math.max(args.length - 1, 0)];
        if (args.length != 0) System.arraycopy(args, 1, rest, 0, args.length - 1);
        final String restArgument = String.join(" ", rest).trim();
        final Height<?> restHeight = HeightUnits.parse(restArgument);

        // player takes priority over height
        final Player target = args.length != 0 ? sender.getServer().getPlayer(args[0]) : null;

        // Cases:
        // 1. /height
        // 2. /height <valid player name>
        // 3. /height <valid player name> <valid height>
        // 4. /height <valid player name> <invalid height>
        // 5. /height <valid height>
        // 6. /height <invalid height>
        // 7. /height <invalid player name> <valid height>
        // 8. /height <invalid player name> <invalid height>
        // 9. /height <invalid player name>

        if (args.length == 0) return executeCase1(sender);
        else if (target != null) { // case 2, 3, or 4
            if (args.length == 1) return executeCase2(sender, target);
            else if (restHeight == null) return executeCase4(sender, restArgument);
            else return executeCase3(sender, target, restHeight);
        } else if (height == null) { // case 6, 7, 8 or 9
            if (args.length == 1) return executeCase69(sender, label, args);
            else if (restHeight == null) return executeCase68(sender, args, restArgument);
            else return executeCase7(sender, args);
        } else return executeCase5(sender, height);
    }

    private boolean executeCase1(@NotNull final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Messages.DO_NEED_PLAYER.formatted("your"));
            return true;
        }

        new HeightGUI(player).openFor(player);
        return true;
    }

    private boolean executeCase2(@NotNull final CommandSender sender, @NotNull final Player target) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Messages.DO_NEED_PLAYER.formatted(target.getName() + "'s"));
            return true;
        }

        if (!sender.hasPermission("mineheight.height.others") && !sender.equals(target)) {
            sender.sendMessage(Messages.NO_PERMISSION_OTHER.getMessage());
            return true;
        }

        new HeightGUI(target).openFor(player);
        return true;
    }

    private boolean executeCase3(@NotNull final CommandSender sender, final Player target, @NotNull final Height<?> height) {
        if (!sender.hasPermission("mineheight.height.others") && !sender.equals(target)) {
            sender.sendMessage(Messages.NO_PERMISSION_OTHER.getMessage());
            return true;
        }

        setHeight0(sender, target, height);
        return true;
    }

    private static boolean executeCase4(@NotNull final CommandSender sender, final String argument) {
        sender.sendMessage(Messages.NOT_A_VALID_HEIGHT.formatted(argument));
        return true;
    }

    private boolean executeCase5(@NotNull final CommandSender sender, @NotNull final Height<?> height) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Messages.DO_NEED_PLAYER.formatted("your"));
            return true;
        }

        setHeight0(sender, player, height);
        return true;
    }

    private static boolean executeCase68(@NotNull final CommandSender sender, @NotNull final String @NotNull [] args, final String restArgument) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.NOT_A_VALID_PLAYER.formatted(args[0]));
            sender.sendMessage(Messages.EXTRA_NOT_A_VALID_HEIGHT.formatted(restArgument));
            return true;
        }

        sender.sendMessage(Messages.NOT_A_VALID_PLAYER_NOR_HEIGHT.formatted(args[0]));
        sender.sendMessage(Messages.EXTRA_NOT_A_VALID_HEIGHT_EITHER.formatted(restArgument));
        return true;
    }

    private static boolean executeCase69(@NotNull final CommandSender sender, @NotNull final String label, @NotNull final String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.NOT_A_VALID_PLAYER.formatted(args[0]));
            sender.sendMessage(Messages.EXTRA_WOULD_NEED_PLAYER.formatted("their"));
            sender.sendMessage(Messages.EXTRA_WOULD_NEED_HEIGHT.formatted(label, args[0]));
            return true;
        }

        sender.sendMessage(Messages.NOT_A_VALID_PLAYER_NOR_HEIGHT.formatted(args[0]));
        return true;
    }

    private static boolean executeCase7(@NotNull final CommandSender sender, @NotNull final String @NotNull [] args) {
        sender.sendMessage(Messages.NOT_A_VALID_PLAYER.formatted(args[0]));
        return true;
    }

    private static void setHeight0(@NotNull final CommandSender sender, @NotNull final Player target, @NotNull final Height<?> height) {
        final ServerPlayer handle = ((CraftPlayer) target).getHandle();
        final MineHeight plugin = MineHeight.getPlugin(MineHeight.class);

        final String targetIndicator = target.equals(sender) ? "your" : (target.getName() + "'s");

        if (!plugin.setHeight(handle, height)) {
            sender.sendMessage(Messages.WARN_CLAMPED_HEIGHT.formatted(targetIndicator, height, plugin.getHeight(handle)));
        }
        sender.sendMessage(Messages.HEIGHT_SET.formatted(targetIndicator, plugin.getHeight(handle)));
    }
}
