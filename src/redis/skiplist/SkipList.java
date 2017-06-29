package redis.skiplist;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class SkipList<S extends IMergeScore<S>, V> {

	private static final int DEFAULT_MIN_MAX_LEVEL = 1;
	private static final int DEFAULT_MAX_MAX_LEVEL = 31;

	private static class SkipListLevel {
		// 前进指针
		SkipListNode forward;
		// 跨度
		long span;
	}

	private static class SkipListNode {
		// 前进指针层
		final SkipListLevel[] levels;
		// 后退指针
		SkipListNode backward;
		// 分值
		final Object score;
		// 值
		final Object value;

		private SkipListNode(int level, Object score, Object value) {
			levels = new SkipListLevel[level];
			backward = null;
			this.score = score;
			this.value = value;
		}
	}

	private static class SkipListRoot {
		// 表头节点，不参与存储
		SkipListNode head;
		// 表尾节点，参与存储
		SkipListNode tail;
		int curMaxLevel;
		long size;

		private SkipListRoot(int maxLevel) {
			// 表头节点层数固定最大层数，方便后续操作
			head = new SkipListNode(maxLevel, 0, null);
		}
	}

	// 最大层数，合理的元素个数cnt与层数的关系应当符合2^(maxLevel+1)~=cnt
	private final int maxLevel;
	private final SkipListRoot root;
	// 层数random相关参数
	private int[] powers;
	private Random rd = new Random();

	SkipList(int maxLevel) {
		this.maxLevel = this.fixMaxLevel(maxLevel);
		root = new SkipListRoot(this.maxLevel);
		this.choosePowers();
	}

	/**
	 * 修正层数在1-31之间，要注意到最大层数为1则退化到单链表
	 */
	private int fixMaxLevel(int maxLevel) {
		if (maxLevel < DEFAULT_MIN_MAX_LEVEL) {
			return DEFAULT_MIN_MAX_LEVEL;
		}
		if (maxLevel > DEFAULT_MAX_MAX_LEVEL) {
			return DEFAULT_MAX_MAX_LEVEL;
		}
		return maxLevel;
	}

	private void choosePowers() {
		powers = new int[maxLevel];
		for (int i = 0; i < maxLevel; i++) {
			powers[i] = 1 << i;
		}
	}

	/**
	 * 跳跃表基于折半查找+单链表的基础实现，元素出现在第n层的概率应为1/2^(n-1)
	 */
	private int randomLevel() {
		// 控制随机数在1-2^(maxLevel-1)之间，随机产生的层数必然在1-maxLevel之间
		// 基于幂次定律，随机数的产生规律必然是两端少中间多，尽量让层数产生足够随机
		int r = rd.nextInt(powers[maxLevel - 1]) + 1;
		for (int i = maxLevel - 1; i >= 0; i--) {
			if (r >= powers[i]) {
				return i + 1;
			}
		}
		return 1; // 如有意外发生，保证元素能插入第1层
	}

	@SuppressWarnings({ "unchecked" })
	private S getSKey(SkipListNode node) {
		return (S) node.score;
	}

	@SuppressWarnings("unchecked")
	private V getValue(SkipListNode node) {
		return (V) node.value;
	}

	/**
	 * 插入新节点并返回排名（注意该方法不处理重复节点），T=O(logN)
	 */
	long add(S score, V value) {
		int level = this.randomLevel();
		// 初始化表头节点未使用过的层
		if (level > root.curMaxLevel) {
			for (int i = root.curMaxLevel; i < level; i++) {
				root.head.levels[i] = new SkipListLevel();
			}
			root.curMaxLevel = level;
		}
		// 每一层有可能需要更新的节点
		SkipListNode[] updates = new SkipListNode[root.curMaxLevel];
		// 每一层有可能需要更新的节点的排位（之后用来计算被更新节点、新节点的各层跨度）
		long[] ranks = new long[root.curMaxLevel];
		SkipListNode curNode = root.head;
		SkipListNode forward = null;
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			// 跨度各层累加
			ranks[i] = i == root.curMaxLevel - 1 ? 0 : ranks[i + 1];
			// 不考虑相同节点（更新节点应该先删除再重新插入），相同分值先插入排序在前
			while ((forward = curNode.levels[i].forward) != null && (this.getSKey(forward).compareTo(score) < 0
					|| this.getSKey(forward).compareTo(score) == 0 && !this.getValue(forward).equals(value))) {
				ranks[i] += curNode.levels[i].span;
				curNode = forward;
			}
			updates[i] = curNode;
		}
		// 插入并修正跨度
		SkipListNode tarNode = new SkipListNode(level, score, value);
		for (int i = level - 1; i >= 0; i--) {
			tarNode.levels[i] = new SkipListLevel();
			tarNode.levels[i].forward = updates[i].levels[i].forward;
			updates[i].levels[i].forward = tarNode;
			tarNode.levels[i].span = tarNode.levels[i].forward == null ? 0
					: updates[i].levels[i].span - (ranks[0] - ranks[i]) + 1;
			updates[i].levels[i].span = ranks[0] - ranks[i] + 1;
		}
		// 未直接接触（直接跨过了）的节点跨度也要+1
		for (int i = level; i < root.curMaxLevel; i++) {
			if (updates[i].levels[i].forward != null) {
				updates[i].levels[i].span++;
			}
		}
		// 修正后退指针
		tarNode.backward = updates[0] == root.head ? null : updates[0];
		// 更新表尾节点or修正新节点的1层前进节点的后退指针（1层元素前进跨度恒=1）
		if (tarNode.levels[0].forward != null) {
			tarNode.levels[0].forward.backward = tarNode;
		} else {
			root.tail = tarNode;
		}
		root.size++;
		return ranks[0] + 1;
	}

	/**
	 * T=O(logN)
	 */
	boolean remove(S score, V value) {
		SkipListNode[] updates = new SkipListNode[root.curMaxLevel];
		SkipListNode curNode = root.head, forward = null, tarNode = null;
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			while ((forward = curNode.levels[i].forward) != null && (this.getSKey(forward).compareTo(score) < 0
					|| this.getSKey(forward).compareTo(score) == 0 && !this.getValue(forward).equals(value))) {
				curNode = forward;
			}
			if (forward != null && this.getSKey(forward).compareTo(score) == 0
					&& this.getValue(forward).equals(value)) {
				tarNode = forward;
			}
			updates[i] = curNode;
		}
		if (tarNode == null) {
			return false;
		}
		this.remoteNode(tarNode, updates);
		return true;
	}

	List<V> removeRange(long start, long end) {
		List<V> results = new ArrayList<>();
		start = start < 0 ? root.size + start + 1 : start;
		start = Math.max(1, Math.min(start, root.size - 1));
		end = end < 0 ? root.size + end + 1 : end;
		end = Math.max(1, Math.min(Math.max(start, end), root.size));
		int curRank = 0;
		SkipListNode tarNode = null, curNode = root.head, forward = null;
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			while ((forward = curNode.levels[i].forward) != null && curRank + curNode.levels[i].span <= start) {
				curRank += curNode.levels[i].span;
				curNode = forward;
			}
			if (curRank == start) {
				tarNode = curNode;
				break;
			}
		}
		return results;
	}

	List<V> removeRangeByScore(S min, S max) {

		return null;
	}

	private void remoteNode(SkipListNode tarNode, SkipListNode[] updates) {
		int level = tarNode.levels.length;
		// 修正相连节点的前进指针以及跨度
		for (int i = 0; i < level; i++) {
			updates[i].levels[i].forward = tarNode.levels[i].forward;
			if (updates[i].levels[i].forward != null) {
				updates[i].levels[i].span += tarNode.levels[i].span - 1;
			} else {
				updates[i].levels[i].span = 0;
			}
		}
		// 修正未直接相连的节点的跨度（跨过了的节点）
		for (int i = level; i < root.curMaxLevel; i++) {
			if (updates[i].levels[i].forward != null) {
				updates[i].levels[i].span--;
			}
		}
		// 修正尾指针or后退指针
		if (root.tail == tarNode) {
			root.tail = tarNode.backward;
		} else {
			tarNode.levels[0].forward.backward = tarNode.backward;
		}
		// 修正跳跃表最大高度
		while (root.curMaxLevel > 1 && root.head.levels[root.curMaxLevel - 1].forward == null) {
			root.curMaxLevel--;
		}
		root.size--;
	}

	/**
	 * T=O(1)
	 */
	boolean isBetterThanTail(S score) {
		if (root.tail == null) {
			return true;
		}
		return score.compareTo(this.getSKey(root.tail)) > 0;
	}

	/**
	 * T=O(1)
	 */
	V getTail() {
		if (root.tail == null) {
			return null;
		}
		return this.getValue(root.tail);
	}

	/**
	 * T=O(1)
	 */
	long size() {
		return root.size;
	}

	/**
	 * T=O(logN)
	 */
	long rank(S score, V value) {
		SkipListNode curNode = root.head;
		SkipListNode forward = null;
		int[] ranks = new int[root.curMaxLevel];
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			ranks[i] = i == root.curMaxLevel - 1 ? 0 : ranks[i + 1];
			while ((forward = curNode.levels[i].forward) != null && (this.getSKey(forward).compareTo(score) < 0
					|| this.getSKey(forward).compareTo(score) == 0 && !this.getValue(forward).equals(value))) {
				ranks[i] += curNode.levels[i].span;
				curNode = forward;
			}
			if (this.getSKey(forward).compareTo(score) == 0 && this.getValue(forward).equals(value)) {
				return ranks[i] + curNode.levels[i].span;
			}
		}
		return -1;
	}

	/**
	 * T=O(logN+M)，M：要获取的元素数量
	 */
	public List<V> range(long start, long end) {
		List<V> results = new ArrayList<>();
		start = start < 0 ? root.size + start + 1 : start;
		start = Math.max(1, Math.min(start, root.size - 1));
		end = end < 0 ? root.size + end + 1 : end;
		end = Math.max(1, Math.min(Math.max(start, end), root.size));
		int traversed = 0;
		SkipListNode tarNode = null, curNode = root.head, forward = null;
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			while ((forward = curNode.levels[i].forward) != null && traversed + curNode.levels[i].span < start) {
				traversed += curNode.levels[i].span;
				curNode = forward;
			}
			if (traversed == start) {
				tarNode = curNode;
				break;
			}
		}
		if (tarNode != null) {
			results.add(this.getValue(curNode));
			for (int i = 0; i < end - start; i++) {
				results.add(this.getValue(tarNode.levels[0].forward));
				tarNode = tarNode.levels[0].forward;
			}
		}
		return results;
	}

	/**
	 * T=O(logN+M)，M：要获取的元素数量
	 */
	public List<V> rangeByScore(S min, S max) {
		List<V> results = new ArrayList<>();
		max = max.compareTo(min) < 0 ? min : max;
		SkipListNode tarMinNode = null, curNode = root.head, forward = null;
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			while ((forward = curNode.levels[i].forward) != null && this.getSKey(forward).compareTo(min) <= 0) {
				curNode = forward;
			}
			if (this.getSKey(curNode).compareTo(min) == 0) {
				tarMinNode = curNode;
				break;
			}
		}
		if (tarMinNode != null) {
			results.add(this.getValue(tarMinNode));
			while ((forward = tarMinNode.levels[0].forward) != null && this.getSKey(forward).compareTo(max) <= 0) {
				results.add(this.getValue(forward));
				tarMinNode = forward;
			}
		}
		return results;
	}

}
