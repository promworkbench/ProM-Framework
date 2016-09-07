package org.processmining.framework.util.collection;

import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class WeakKeyValueMap<K, V> extends AbstractMap<K, V> {

	private final Map<K, WeakReference<V>> map;

	public WeakKeyValueMap(int initialCapacity, float loadFactor) {
		map = new WeakHashMap<K, WeakReference<V>>(initialCapacity, loadFactor);
	}

	public WeakKeyValueMap(int initialCapacity) {
		map = new WeakHashMap<K, WeakReference<V>>(initialCapacity);
	}

	public WeakKeyValueMap() {
		map = new WeakHashMap<K, WeakReference<V>>();

	}

	public WeakKeyValueMap(Map<? extends K, ? extends V> m) {
		this();
		putAll(m);
	}

	public void clear() {
		map.clear();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		for (WeakReference<V> w : map.values()) {
			if (value.equals(w.get())) {
				return true;
			}
		}
		return false;
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Set<java.util.Map.Entry<K, V>> set = new HashSet<java.util.Map.Entry<K, V>>();
		for (java.util.Map.Entry<K, WeakReference<V>> entry : map.entrySet()) {
			set.add(new SimpleEntry<K, V>(entry.getKey(), entry.getValue().get()));
		}
		return set;
	}

	public V get(Object key) {
		WeakReference<V> val = map.get(key);
		return val == null ? null : val.get();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public V put(K key, V value) {
		V val = get(key);
		map.put(key, new WeakReference<V>(value));
		return val;
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	public V remove(Object key) {
		return map.remove(key).get();
	}

	public int size() {
		return map.size();
	}

	public Collection<V> values() {
		Set<V> set = new HashSet<V>();
		for (WeakReference<V> val : map.values()) {
			set.add(val.get());
		}
		return set;
	}

}
