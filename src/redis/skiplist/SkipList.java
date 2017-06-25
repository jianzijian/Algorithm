package redis.skiplist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

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

	public int add(double score, Object value) {
		if (value == null) {
			throw new NullPointerException();
		}
		// 先删除重复的节点
		this.deleteNode(score, value);
		return this.insertNode(score, value);
	}

	public int incr(double incrScore, Object value) {
		if (value == null) {
			throw new NullPointerException();
		}
		double score = incrScore;
		// 先删除重复的节点
		SkipListNode tarNode = this.deleteNode(score, value);
		score = tarNode != null ? tarNode.score + score : score;
		return this.insertNode(score, value);
	}

	/**
	 * 返回排名,T=O(logN)
	 */
	private int insertNode(double score, Object value) {
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
			// 相同分值先插入的节点排位在前，不考虑相同节点（更新节点应该先删除再重新插入）
			while ((forward = curNode.levels[i].forward) != null && forward.score <= score) {
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
		return ranks[0] + 1;
	}

	/**
	 * 返回被成功移除的节点数,T=M*O(logN)+O(N)
	 */
	public int remove(Object... values) {
		List<SkipListNode> nodes = this.searchNode(Arrays.stream(values).collect(Collectors.toSet()));
		for (SkipListNode node : nodes) {
			this.deleteNode(node.score, node.value);
		}
		return nodes.size();
	}

	/**
	 * T=O(logN)
	 */
	private SkipListNode deleteNode(double score, Object value) {
		SkipListNode tarNode = null;
		SkipListNode[] updates = new SkipListNode[root.curMaxLevel];
		SkipListNode curNode = root.head;
		SkipListNode forward = null;
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			while ((forward = curNode.levels[i].forward) != null
					&& (forward.score < score || (forward.score == score && !forward.value.equals(value)))) {
				curNode = forward;
			}
			// 不中断，直到找到各层相对于该节点的前置节点
			if (forward.score == score && forward.value.equals(value)) {
				tarNode = forward;
			}
			updates[i] = curNode;
		}
		if (tarNode != null) {
			int level = tarNode.levels.length;
			for (int i = 0; i < level; i++) {
				updates[i].levels[i].forward = tarNode.levels[i].forward;
				if (tarNode.levels[i].forward == null) {
					updates[i].levels[i].span = 0;
				} else {
					updates[i].levels[i].span += tarNode.levels[i].span - 1;
				}
			}
			for (int i = level; i < root.curMaxLevel; i++) {
				if (updates[i].levels[i].forward != null) {
					updates[i].levels[i].span--;
				}
			}
			// 更新表尾节点or修正新节点的1层前进节点的后退指针
			if (tarNode == root.tail) {
				root.tail = tarNode.backward;
			} else {
				tarNode.levels[0].forward.backward = tarNode.backward;
			}
		}
		return tarNode;
	}

	public int rank(Object value) {

		return -1;
	}

	public int score(Object value) {
		return -1;
	}

	/**
	 * T=O(N)
	 */
	private List<SkipListNode> searchNode(Set<Object> values) {
		List<SkipListNode> nodes = new ArrayList<>();
		SkipListNode curNode = root.head;
		SkipListNode forward = null;
		while ((forward = curNode.levels[0].forward) != null) {
			if (values.contains(forward.value)) {
				nodes.add(forward);
			}
			curNode = forward;
		}
		return nodes;
	}

}
