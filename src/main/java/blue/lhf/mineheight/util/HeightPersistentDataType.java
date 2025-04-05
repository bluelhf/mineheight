package blue.lhf.mineheight.util;

import blue.lhf.mineheight.model.*;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.*;
import org.jetbrains.annotations.NotNull;

public class HeightPersistentDataType implements PersistentDataType<PersistentDataContainer, Height<?>> {

    public static final HeightPersistentDataType INSTANCE = new HeightPersistentDataType();

    public static final NamespacedKey VALUE_KEY = new NamespacedKey("mineheight", "value");
    public static final NamespacedKey UNIT_KEY = new NamespacedKey("mineheight", "unit");

    @Override
    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public @NotNull Class<Height<?>> getComplexType() {
        //noinspection unchecked
        return (Class<Height<?>>) (Class<?>) Height.class;
    }

    @Override
    public @NotNull PersistentDataContainer toPrimitive(@NotNull final Height<?> complex, @NotNull final PersistentDataAdapterContext context) {
        final PersistentDataContainer container = context.newPersistentDataContainer();
        container.set(UNIT_KEY, PersistentDataType.STRING, complex.unit().id());
        container.set(VALUE_KEY, PersistentDataType.DOUBLE, complex.metres());
        return container;
    }

    @Override
    public @NotNull Height<?> fromPrimitive(@NotNull final PersistentDataContainer primitive, @NotNull final PersistentDataAdapterContext context) {
        final double metres = primitive.getOrDefault(VALUE_KEY, PersistentDataType.DOUBLE, 1.8);
        final HeightUnit<?> unit = HeightUnits.getOrDefault(primitive.get(UNIT_KEY, PersistentDataType.STRING), HeightUnit.METRES);

        //noinspection rawtypes,unchecked - we don't know the type of the unit
        return (Height<?>) new Height(unit, metres);
    }
}
