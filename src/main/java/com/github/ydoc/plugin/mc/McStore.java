package com.github.ydoc.plugin.mc;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author nobugboy
 **/
public class McStore extends LinkedHashMap<Long, List<LinkedList<MethodPoint>>> {
    public static final McStore MC_STORE = new McStore();
    public static final LongAdder LONG_ADDER = new LongAdder();

    public synchronized void add(List<LinkedList<MethodPoint>> data) {
	LONG_ADDER.increment();
	if (LONG_ADDER.longValue() > 50) {
	    MC_STORE.remove(LONG_ADDER.longValue() - 50);
	}
	MC_STORE.put(LONG_ADDER.longValue(), data);
    }

}
