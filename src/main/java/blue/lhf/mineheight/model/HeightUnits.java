package blue.lhf.mineheight.model;

import org.jetbrains.annotations.Nullable;

public enum HeightUnits {
    METRES(HeightUnit.METRES),
    CENTIMETRES(HeightUnit.CENTIMETRES),
    FEET_AND_INCHES(HeightUnit.FEET_AND_INCHES);

    private final HeightUnit<?> unit;

    HeightUnits(final HeightUnit<?> unit) {
        this.unit = unit;
    }

    public static @Nullable Height<?> parse(final String input) {
        for (final HeightUnits unit : values()) {
            final Height<?> value = unit.getUnit().parse(input);
            if (value != null) return value;
        }
        return null;
    }

    public static HeightUnit<?> getOrDefault(final String id, final HeightUnit<?> defaultUnit) {
        for (final HeightUnits unit : values()) {
            if (unit.getUnit().id().equals(id)) return unit.getUnit();
        }
        return defaultUnit;
    }

    private HeightUnit<?> getUnit() {
        return unit;
    }
}
