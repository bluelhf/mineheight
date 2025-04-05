package blue.lhf.mineheight.gui;

import blue.lhf.mineheight.MineHeight;
import blue.lhf.mineheight.model.Height;
import blue.lhf.mineheight.model.HeightUnit;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public class HeightGUI extends GUI<HeightGUI.Data> {
    private final Player target;
    private static final ItemStack BACKGROUND = new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS_PANE);

    private static final ItemStack METRIC;
    private static final ItemStack IMPERIAL;
    static {
        BACKGROUND.set(DataComponents.CUSTOM_NAME, literal(" "));

        METRIC = new ItemStack(Items.PLAYER_HEAD);
        METRIC.set(DataComponents.CUSTOM_NAME, literal("Metric System").withStyle(style -> style.withItalic(false)));
        METRIC.set(DataComponents.PROFILE, new ResolvableProfile(Optional.empty(), Optional.of(new UUID(1024377729289046070L, -7478599100526189515L)),
                    new PropertyMap() {{ put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjU0ODUwMzFiMzdmMGQ4YTRmM2I3ODE2ZWI3MTdmMDNkZTg5YTg3ZjZhNDA2MDJhZWY1MjIyMWNkZmFmNzQ4OCJ9fX0=")); }}));

        METRIC.set(DataComponents.LORE, new ItemLore(List.of(
                literal("Click ").append(translatable("key.mouse.left")).append(" to use the Metric System").withStyle(style -> style.withItalic(false))
        )));

        IMPERIAL = new ItemStack(Items.PLAYER_HEAD);
        IMPERIAL.set(DataComponents.CUSTOM_NAME, literal("Imperial System").withStyle(style -> style.withItalic(false)));
        IMPERIAL.set(DataComponents.PROFILE, new ResolvableProfile(Optional.empty(), Optional.of(new UUID(4337045925816126718L, -8582199142832821171L)),
                    new PropertyMap() {{ put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNhYzk3NzRkYTEyMTcyNDg1MzJjZTE0N2Y3ODMxZjY3YTEyZmRjY2ExY2YwY2I0YjM4NDhkZTZiYzk0YjQifX19")); }}));

        IMPERIAL.set(DataComponents.LORE, new ItemLore(List.of(
                literal("Click ").append(translatable("key.mouse.left")).append(" to use the Imperial System").withStyle(style -> style.withItalic(false))
        )));
    }

    public HeightGUI(final org.bukkit.entity.Player target) {
        this.target = ((CraftPlayer) target).getHandle();
    }

    @Override
    public Rows rows() {
        return Rows.THREE;
    }

    @Override
    protected ItemStack getItem(final DataContext<Data> dataContext, final int slotIndex) {
        return switch (slotIndex) {
            case  3 -> computeMetricActivation(dataContext);
            case  4 -> METRIC;
            case 21 -> computeImperialActivation(dataContext);
            case 22 -> IMPERIAL;
            case 13 -> computePlayerHead(dataContext);
            default -> BACKGROUND;
        };
    }

    private ItemStack computeImperialActivation(final DataContext<Data> dataContext) {
        final ItemStack imperialActivation = new ItemStack(dataContext.data().isImperial() ? Items.LIME_DYE : Items.GRAY_DYE);
        imperialActivation.set(DataComponents.CUSTOM_NAME, literal(" "));
        return imperialActivation;
    }

    private ItemStack computeMetricActivation(final DataContext<Data> dataContext) {
        final ItemStack metricActivation = new ItemStack(dataContext.data().isImperial() ? Items.GRAY_DYE : Items.LIME_DYE);
        metricActivation.set(DataComponents.CUSTOM_NAME, literal(" "));
        return metricActivation;
    }

    @NotNull
    private ItemStack computePlayerHead(final DataContext<Data> dataContext) {
        final ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
        playerHead.set(DataComponents.CUSTOM_NAME, literal(dataContext.data().getHeight().toString()).withStyle(style -> style.withItalic(false)));
        playerHead.set(DataComponents.PROFILE, new ResolvableProfile(target.getGameProfile()));
        final boolean imperial = dataContext.data().isImperial();
        playerHead.set(DataComponents.LORE, new ItemLore(List.of(
                translatable("key.mouse.left")
                        .append(": ").append(literal("+1 " + (imperial ? "inch" : "cm")).withColor(0x00FF00))
                        .withColor(0xFFFFFF).withStyle(style -> style.withItalic(false)),
                translatable("key.mouse.right")
                        .append(": ").append(literal("-1 " + (imperial ? "inch" : "cm")).withColor(0xFF00FF))
                        .withColor(0xFFFFFF).withStyle(style -> style.withItalic(false)),
                translatable("key.keyboard.left.shift").append(" + ")
                        .append(translatable("key.mouse.left")).append(": ")
                        .append(literal("+" + (imperial ? "1 foot" : "10 cm")).withColor(0x00FF00))
                        .withColor(0xFFFFFF).withStyle(style -> style.withItalic(false)),
                translatable("key.keyboard.left.shift").append(" + ")
                        .append(translatable("key.mouse.right")).append(": ")
                        .append(literal("-" + (imperial ? "1 foot" : "10 cm")).withColor(0xFF00FF))
                        .withColor(0xFFFFFF).withStyle(style -> style.withItalic(false))
        )));

        return playerHead;
    }

    @Override
    protected void onClick(final DataContext<Data> dataContext, final int slotIndex, final int button, @NotNull final ClickType actionType, @NotNull final Player player) {
        if (actionType != ClickType.PICKUP && actionType != ClickType.QUICK_MOVE) return;
        switch (slotIndex) {
            case 3, 4 -> {
                if (dataContext.data().isImperial()) player.playSound(SoundEvents.UI_BUTTON_CLICK.value());

                dataContext.data().setImperial(false);
                final int revision = dataContext.menu().incrementStateId();
                dataContext.menu().setItem(3, revision, computeMetricActivation(dataContext));
                dataContext.menu().setItem(21, revision, computeImperialActivation(dataContext));
            }
            case 21, 22 -> {
                if (!dataContext.data().isImperial()) player.playSound(SoundEvents.UI_BUTTON_CLICK.value());

                dataContext.data().setImperial(true);
                final int revision = dataContext.menu().incrementStateId();
                dataContext.menu().setItem(3, revision, computeMetricActivation(dataContext));
                dataContext.menu().setItem(21, revision, computeImperialActivation(dataContext));
            }
            case 13 -> {
                final Height<?> height = dataContext.data().getHeight();
                final boolean isImperial = dataContext.data().isImperial();

                final double newHeight = height.metres() +
                        (button == 0 ? 1 : -1)
                                * (actionType == ClickType.QUICK_MOVE
                                ? isImperial ? 0.3048 : 0.1
                                : isImperial ? 0.0254 : 0.01
                        );

                player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, (float) (1.0 + ((newHeight - height.metres()) / 30.48)));
                dataContext.data().setHeight(height.withMetres(newHeight));
            }
        }

        dataContext.menu().setItem(13, dataContext.menu().incrementStateId(), computePlayerHead(dataContext));
    }

    @Override
    protected Component getDisplayName(final Player player) {
        return player.equals(target) ? literal("Set your height") : literal("Set ").append(target.getName()).append("'s height");
    }

    @Override
    protected Data defaultData(final Context context) {
        return new Data();
    }

    protected class Data {
        private final MineHeight plugin = MineHeight.getPlugin(MineHeight.class);

        public boolean isImperial() {
            return plugin.getHeight(target).unit() instanceof HeightUnit.FeetAndInches;
        }

        public void setImperial(final boolean imperial) {
            plugin.setHeight(target, imperial
                    ? plugin.getHeight(target).withUnit(HeightUnit.FEET_AND_INCHES)
                    : plugin.getHeight(target).withUnit(HeightUnit.METRES)
            );
        }

        public Height<?> getHeight() {
            return plugin.getHeight(target);
        }

        public void setHeight(final Height<?> height) {
            plugin.setHeight(target, height);
        }
    }
}
