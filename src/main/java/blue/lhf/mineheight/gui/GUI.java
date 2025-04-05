package blue.lhf.mineheight.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static net.minecraft.world.inventory.ClickType.*;
import static net.minecraft.world.inventory.MenuType.*;

public abstract class GUI<T> implements Function<org.bukkit.entity.Player, MenuProvider> {
    public final void openFor(final org.bukkit.entity.Player player) {
        ((CraftPlayer) player).getHandle().openMenu(this.apply(player));
    }
    
    public enum Rows {
        ONE(GENERIC_9x1), TWO(GENERIC_9x2), THREE(GENERIC_9x3),
        FOUR(GENERIC_9x4), FIVE(GENERIC_9x5), SIX(GENERIC_9x6);

        private final MenuType<ChestMenu> type;
        Rows(final MenuType<ChestMenu> type) {
            this.type = type;
        }

        public MenuType<ChestMenu> getType() {
            return type;
        }

        public int getRowCount() {
            return ordinal() + 1;
        }
    }

    public record DataContext<T>(Context sub, T data) implements Context {
        @Override
        public Player player() {
            return sub.player();
        }

        @Override
        public AbstractContainerMenu menu() {
            return sub.menu();
        }
    }

    public record InitContext(Player player, AbstractContainerMenu menu) implements Context {}

    public interface Context {
        Player player();
        AbstractContainerMenu menu();
    }

    protected abstract Rows rows();
    protected abstract ItemStack getItem(DataContext<T> dataContext, final int slotIndex);
    protected abstract void onClick(DataContext<T> dataContext, final int slotIndex, final int button, final @NotNull ClickType actionType, final @NotNull Player player);
    public static final List<ClickType> BOTTOM_WHITELISTED_CLICKS = List.of(PICKUP, CLONE, SWAP, THROW, QUICK_CRAFT, PICKUP_ALL);

    @Override
    public MenuProvider apply(final org.bukkit.entity.Player player) {
        final Rows rows = rows();

        return new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return GUI.this.getDisplayName(((CraftPlayer) player).getHandle());
            }

            @Override
            public AbstractContainerMenu createMenu(final int syncId, final @NotNull Inventory playerInventory, final @NotNull Player player) {
                final SimpleContainer container = new SimpleContainer(rows.getRowCount() * 9);
                final var menu = new ChestMenu(rows.getType(), syncId, playerInventory, container, rows.getRowCount()) {
                    final InitContext initContext = new InitContext(player, this);
                    final DataContext<T> context = new DataContext<>(initContext, defaultData(initContext));

                    @Override
                    public void clicked(final int slotIndex, final int button, final @NotNull ClickType actionType, final @NotNull Player player) {
                        // Allow clicks except quick move when the click is in the player's inventory
                        if (BOTTOM_WHITELISTED_CLICKS.contains(actionType) && (slotIndex < 0 || slotIndex >= getContainer().getContainerSize())) {
                            super.clicked(slotIndex, button, actionType, player);
                        } else if (slotIndex >= 0 && slotIndex < getContainer().getContainerSize()) {
                            onClick(context, slotIndex, button, actionType, player);
                        }
                    }
                };

                for (int i = 0; i < container.getContainerSize(); i++)
                    container.setItem(i, getItem(menu.context, i));

                return menu;
            }
        };
    }

    protected abstract Component getDisplayName(final Player player);

    protected T defaultData(final Context context) {
        return null;
    }
}
