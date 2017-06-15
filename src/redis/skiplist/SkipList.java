package redis.skiplist;

import java.util.Random;

public class SkipList {

	private static final int DEFAULT_MIN_MAX_LEVEL = 1;
	private static final int DEFAULT_MAX_MAX_LEVEL = 31;

	private static class SkipListLevel {
		// 前进指针
		private SkipListNode forward;
		// 跨度
		private long span;
	}

	private static class SkipListNode {
		// 前进指针层
		private final SkipListLevel[] levels;
		// 后退指针
		private SkipListNode backward;
		// 分值
		private final double score;
		// 值
		private final Object value;

		private SkipListNode(int level, double score, Object value) {
			levels = new SkipListLevel[level];
			backward = null;
			this.score = score;
			this.value = value;
		}
	}

	private static class SkipListRoot {
		// 表头节点，不参与存储
		private SkipListNode head;
		// 表尾节点，参与存储
		private SkipListNode tail;
		private int curMaxLevel;
		private long size;

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

	public SkipList(int maxLevel) {
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

	public void insert(double score, Object value) {
		if (value == null) {
			throw new NullPointerException();
		}

	}

	private void insertNode(double score, Object value) {
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
		int[] ranks = new int[root.curMaxLevel];
		SkipListNode curNode = root.head;
		SkipListNode forward = null;
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			// 跨度各层累加
			ranks[i] = i == root.curMaxLevel - 1 ? 0 : ranks[i + 1];
			forward = curNode.levels[i].forward;
			while (forward != null
					&& (forward.score < score || (forward.score == score && !forward.value.equals(value)))) {
				ranks[i] += curNode.levels[i].span;
				curNode = forward;
			}
			updates[i] = curNode;
		}
		// 插入并修正跨度
		SkipListNode tarNode = new SkipListNode(level, score, value);
		for (int i = level; i >= 0; i--) {
			tarNode.levels[i].forward = updates[i].levels[i].forward;
			updates[i].levels[i].forward = tarNode;
			tarNode.levels[i].span = updates[i].levels[i].span - (ranks[0] - ranks[i]) + 1;
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
	}

	/**
	 * 分值查询，T=O(logN)
	 */
	private SkipListNode searchNodeByScore(double score) {

		return null;
	}

	/**
	 * 值查询，T=O(N)
	 */
	private SkipListNode searchNodeByValue(Object value) {

		return null;
	}

}
