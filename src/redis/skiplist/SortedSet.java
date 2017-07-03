package redis.skiplist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SortedSet<S extends IMergeScore<S>, V> {

	private static final long DEFAULT_CAPACITY = 1000;

	private final long capacity;
	private final SkipList<S, V> skipList;
	private final Map<V, S> value2Score;

	public SortedSet(long capacity) {
		this.capacity = this.fixCapacity(capacity);
		skipList = new SkipList<>(this.log(capacity, 2));
		value2Score = new HashMap<>();
	}

	private int log(double value, double base) {
		return (int) Math.ceil(Math.log(value) / Math.log(base));
	}

	private long fixCapacity(long capacity) {
		return capacity <= 0 ? DEFAULT_CAPACITY : capacity;
	}

	public synchronized long add(S score, V value) {
		if (score == null || value == null) {
			throw new NullPointerException();
		}
		if (value2Score.containsKey(value)) {
			skipList.remove(value2Score.get(value), value);
			value2Score.put(value, score);
			return skipList.add(score, value);
		}
		if (value2Score.size() < capacity) {
			value2Score.put(value, score);
			return skipList.add(score, value);
		}
		if (value2Score.size() >= capacity && skipList.isBetterThanTail(score)) {
			skipList.remove(value2Score.get(skipList.getTail()), value);
			value2Score.put(value, score);
			return skipList.add(score, value);
		}
		return -1;
	}

	public synchronized void addBatch(Map<S, V> svMap) {
		// 后一次的插入会导致前一次插入返回的排名变得不准确，所以干脆不返回了
		for (Entry<S, V> entry : svMap.entrySet()) {
			this.add(entry.getKey(), entry.getValue());
		}
	}

	public synchronized long incr(S score, V value) {
		if (score == null || value == null) {
			throw new NullPointerException();
		}
		if (value2Score.containsKey(value)) {
			skipList.remove(value2Score.get(value), value);
			score = value2Score.get(value).merge(score);
			value2Score.put(value, score);
			return skipList.add(score, value);
		}
		if (value2Score.size() < capacity) {
			value2Score.put(value, score);
			return skipList.add(score, value);
		}
		if (value2Score.size() >= capacity && skipList.isBetterThanTail(score)) {
			skipList.remove(value2Score.get(skipList.getTail()), value);
			value2Score.put(value, score);
			return skipList.add(score, value);
		}
		return -1;
	}

	public synchronized void incrAll(Map<S, V> svMap) {
		// 后一次的插入会导致前一次插入返回的排名变得不准确，所以干脆不返回了
		for (Entry<S, V> entry : svMap.entrySet()) {
			this.incr(entry.getKey(), entry.getValue());
		}
	}

	public synchronized S remove(V value) {
		S score = value2Score.get(value);
		if (score == null) {
			return null;
		}
		value2Score.remove(value);
		skipList.remove(score, value);
		return score;
	}

	public synchronized Map<V, S> removeBatch(Set<V> values) {
		Map<V, S> results = new HashMap<>();
		for (V value : values) {
			S score = this.remove(value);
			if (score != null) {
				results.put(value, score);
			}
		}
		return results;
	}

	public synchronized Map<V, S> removeRange(long start, long end) {
		List<V> values = skipList.removeRange(start, end);
		Map<V, S> vsMap = new HashMap<>();
		values.forEach(value -> vsMap.put(value, value2Score.get(value)));
		return vsMap;
	}

	public synchronized Map<V, S> removeRangeByScore(S min, S max) {
		List<V> values = skipList.removeRangeByScore(min, max);
		Map<V, S> vsMap = new HashMap<>();
		values.forEach(value -> vsMap.put(value, value2Score.get(value)));
		return vsMap;
	}

	public synchronized long rank(V value) {
		if (!value2Score.containsKey(value)) {
			return -1;
		}
		return skipList.rank(value2Score.get(value), value);
	}

	public synchronized List<V> range(long start, long end) {
		return skipList.range(start, end);
	}

	public synchronized List<V> rangeByScore(S min, S max) {
		if (min == null || max == null) {
			throw new NullPointerException();
		}
		return skipList.rangeByScore(min, max);
	}

	public synchronized S getScore(V value) {
		return value2Score.get(value);
	}

	public synchronized long size() {
		return skipList.size();
	}

}
