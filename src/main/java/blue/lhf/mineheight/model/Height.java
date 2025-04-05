package blue.lhf.mineheight.model;

import java.io.*;

public record Height<Unit extends HeightUnit<Unit>>(Unit unit, double metres) implements Serializable {

    public static Height<HeightUnit.Metres> fromMetres(final double heightMetres) {
        return new Height<>(HeightUnit.METRES, heightMetres);
    }

    public Height<Unit> withMetres(final double newHeight) {
        return new Height<>(unit, newHeight);
    }

    public <T extends HeightUnit<T>> Height<T> withUnit(final T newUnit) {
        return new Height<>(newUnit, metres);
    }

    @Override
    public String toString() {
        return unit.toString(metres);
    }
}
