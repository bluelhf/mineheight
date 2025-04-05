package blue.lhf.mineheight;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static net.kyori.adventure.text.Component.text;

public enum Messages {
    NO_PERMISSION_OTHER("<red>You do not have permission to set other players' heights"),
    NOT_A_VALID_PLAYER("<white><c_input:0> <red>is not the name of an online player."),
    NOT_A_VALID_PLAYER_NOR_HEIGHT("<white><c_input:0> <red>is not a valid height nor the name of an online player."),
    NOT_A_VALID_HEIGHT("<white><c_input:0> <red>is not a valid height."),
    EXTRA_NOT_A_VALID_HEIGHT("<grey>Furthermore, <white><c_input:0> <grey>is not a valid height."),
    EXTRA_NOT_A_VALID_HEIGHT_EITHER("<grey>Furthermore, <white><c_input:0> <grey>is not a valid height either."),
    EXTRA_WOULD_NEED_PLAYER("<grey>Furthermore, you would not be able to set <c_input:0> height using the GUI because you are not a player."),
    DO_NEED_PLAYER("<red>You cannot set <c_input:0> height using the GUI because you are not a player."),
    EXTRA_WOULD_NEED_HEIGHT("<grey>You would have to specify a height in the command: <white>/<c_input:0> <c_input:1> <height>"),
    HEIGHT_SET("<yellow>Set <c_input:0> height to <white><c_input:1><yellow>!"),
    WARN_CLAMPED_HEIGHT("<gold>Warning: Could not set <c_input:0> height to <white><c_input:1> <gold>because that height was too small or too large, so it was limited to <white><c_input:2><gold>.");

    private final String message;
    Messages(final String message) {
        this.message = message;
    }

    public Component getMessage() {
        return MiniMessage.miniMessage().deserialize(message);
    }

    public Component formatted(final Object... args) {
        return MiniMessage.miniMessage().deserialize(message.formatted(args), TagResolver.builder().tag("c_input", new BiFunction<>() {
            final Map<ArgumentQueue, AtomicInteger> count = new HashMap<>();

            @Override
            public Tag apply(final ArgumentQueue argumentQueue, final Context context) {
                return Tag.inserting(text(String.valueOf(args[argumentQueue.pop().asInt().orElseThrow()])));
            }
        }).build());
    }
}
