package com.github.ydoc.core.store;

import com.github.ydoc.core.consts.Constans;
import com.github.ydoc.core.kv.Kv;
import com.github.ydoc.core.kv.KvFactory;

import java.util.HashSet;

/**
 * @author nobugboy
 **/
public class RefSet extends HashSet<String> {
    private static final RefSet REF_SET;
    static {
	REF_SET = new RefSet();
    }

    public static RefSet get() {
	return REF_SET;
    }

    public void flushRef(Kv kv, String refName, String simpleName) {
	if (!contains(refName)) {
	    Kv empty = KvFactory.get().empty();
	    empty.put("type", Constans.Type.OBJECT);
	    empty.putReference(refName, simpleName);
	    if (kv != null) {
		kv.put("items", empty);
	    }
	    add(refName);
	}
    }
}
