package blue.lhf.mineheight.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

public interface HeightUnit<Self extends HeightUnit<Self>> extends Serializable {
    /**
     * The ID of this height unit. This is used to identify the unit when serialising.
     * If the ID is not recognised when deserialising, metres will be used.
     *
     * @return The ID of this height unit.
     */
    @NotNull String id();
    /**
     * Parse <code>input</code> into a height using the format for this unit.
     * @return The {@link Height<Self>} object if the input is valid, or null if it is not.
     * */
    @Nullable Height<Self> parse(@NotNull String input);

    /**
     * Converts a height into a string using the format for this unit.
     * It should be the case that <code>parse(toString(height)).metres() == height.metres()</code>.
     * @return A string representation of the given height in this unit.
     * */
    String toString(double height);

    @Serial
    default Object writeReplace() {
        return id();
    }

    @Serial
    default Object readResolve() {
        return HeightUnits.getOrDefault(id(), METRES);
    }

    Metres METRES = new Metres();
    Centimetres CENTIMETRES = new Centimetres();
    FeetAndInches FEET_AND_INCHES = new FeetAndInches();

    class FeetAndInches implements HeightUnit<FeetAndInches> {
        @Override
        public @NotNull String id() {
            return "feet_and_inches";
        }

        @Override
        public @Nullable Height<FeetAndInches> parse(final @NotNull String input) {
            try {
                final String[] parts = input.split("'");
                if (parts.length != 2) return null;
                final String feetPart = parts[0].trim();
                final String fullInchesPart = parts[1].trim();
                final String[] inchesParts = fullInchesPart.split("\"", -1);
                if (inchesParts.length != 2) return null;
                final String inchesPart = inchesParts[0].trim();

                final double feet = Double.parseDouble(feetPart);
                final double inches = Double.parseDouble(inchesPart);
                final double totalInches = feet * 12 + inches;
                final double metres = totalInches * 0.0254;
                return new Height<>(this, metres);
            } catch (final Exception ignored) {
                return null;
            }
        }

        @Override
        public String toString(final double height) {
            final double totalInches = Math.nextUp(height / 0.0254); // round up to avoid rounding errors (looking at you 7'5")
            final int feet = (int) (totalInches / 12);
            final int inches = (int) (totalInches % 12);
            return String.format("%d'%d\"", feet, inches);
        }
    }

    class Metres implements HeightUnit<Metres> {
        @Override
        public @NotNull String id() {
            return "metres";
        }

        @Override
        public @Nullable Height<Metres> parse(final @NotNull String input) {
            if (input.endsWith("cm")) return null;
            try {
                final double metres = Double.parseDouble((input.endsWith("m") ? input.substring(0, input.length() - 1) : input).trim());
                return new Height<>(this, metres);
            } catch (final NumberFormatException e) {
                return null;
            }
        }

        @Override
        public String toString(final double height) {
            return String.format("%.2f m", height);
        }
    }

    class Centimetres extends Metres {
        @Override
        public @NotNull String id() {
            return "centimetres";
        }

        @Override
        public @Nullable Height<Metres> parse(final @NotNull String input) {
            if (!input.endsWith("cm")) return null;
            try {
                final double centimetres = Double.parseDouble(input.replace("cm", "").trim());
                final double metres = centimetres / 100;
                return new Height<>(METRES, metres);
            } catch (final NumberFormatException e) {
                return null;
            }
        }
    }
}
