package blue.lhf.mineheight.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.IntStream;

public class EveryNth<C> {
    private final int nth;
    private final List<List<C>> lists = new ArrayList<>();
    private int next = 0;

    private EveryNth(final int nth) {
        this.nth = nth;
        IntStream.range(0, nth).forEach(i -> lists.add(new ArrayList<>()));
    }

    private void accept(final C item) {
        lists.get(next++ % nth).add(item);
    }

    private EveryNth<C> combine(final EveryNth<C> other) {
        other.lists.forEach(l -> lists.get(next++ % nth).addAll(l));
        next += other.next;
        return this;
    }

    private List<C> getResult() {
        return lists.get(0);
    }

    public static <C> Collector<C, EveryNth<C>, List<C>> collector(final int nth) {
        return Collector.of(() -> new EveryNth<>(nth), EveryNth::accept, EveryNth<C>::combine, EveryNth::getResult);
    }
}
