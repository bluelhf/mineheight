package blue.lhf.mineheight.util;

import java.util.*;
import java.util.stream.*;

/**
 * Select N evenly spaced items from a collection.
 * */
public class SparseSelector {
    private SparseSelector() {}

    public static <T> Collector<T, ?, Iterator<T>> collectSparsely(final int count) {
        // Collect <count> elements from the stream, evenly spaced
        // We must collect to a list because we need to know the size of the stream
        return Collectors.collectingAndThen(Collectors.<T>toList(), list -> {
            if (list.size() <= count) {
                return list.iterator();
            }
            return select(list, count);
        });
    }

    public static <T> Iterator<T> select(final Collection<T> collection, final int count) {
        if (count < 0 || count >= collection.size()) {
            return collection.iterator();
        }

        final double step = Math.max(1D, (double) (collection.size() - 1) / (count - 1));
        return new Iterator<>() {
            private final Iterator<T> backingIterator = collection.iterator();
            private int taken = 0;     // How many elements have been taken so far
            private int index = 0;     // Our own index in the backing iterator
            private double target = 0; // The next index at which an element should be taken

            @Override
            public boolean hasNext() {
                return taken < count && backingIterator.hasNext();
            }

            @Override
            public T next() {
                // Step the backing iterator until we reach the next target index
                T value;
                do {
                    value = backingIterator.next();
                    index++;
                } while (index <= Math.round(target) && backingIterator.hasNext());
                taken++;

                // Move the target index forward by the decimal step
                target += step;
                return value;
            }
        };
    }
}
