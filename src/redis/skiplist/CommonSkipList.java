package redis.skiplist;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CommonSkipList {

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
		// // 值
		// private final Object value = null;

		private SkipListNode(int level, double score) {
			levels = new SkipListLevel[level];
			backward = null;
			this.score = score;
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
			head = new SkipListNode(maxLevel, 0);
		}
	}

	// 最大层数，合理的元素个数cnt与层数的关系应当符合2^(maxLevel+1)~=cnt
	private final int maxLevel;
	private final SkipListRoot root;
	// 层数random相关参数
	private int[] powers;
	private Random rd = new Random();

	public CommonSkipList(int maxLevel) {
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

	/**
	 * 插入并返回排名,T=O(logN)
	 */
	public long add(double score) {
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
			// 不考虑相同节点（更新节点应该先删除再重新插入）
			while ((forward = curNode.levels[i].forward) != null && forward.score < score) {
				ranks[i] += curNode.levels[i].span;
				curNode = forward;
			}
			// 节点存在直接返回排位
			if (forward != null && forward.score == score) {
				return ranks[i] + curNode.levels[i].span;
			}
			updates[i] = curNode;
		}
		// 插入并修正跨度
		SkipListNode tarNode = new SkipListNode(level, score);
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
	 * 查找排名,T=O(logN)
	 */
	public int rank(double score) {
		SkipListNode curNode = root.head;
		SkipListNode forward = null;
		int[] ranks = new int[root.curMaxLevel];
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			ranks[i] = i == root.curMaxLevel - 1 ? 0 : ranks[i + 1];
			while ((forward = curNode.levels[i].forward) != null && forward.score <= score) {
				ranks[i] += curNode.levels[i].span;
				curNode = forward;
			}
			if (curNode.score == score) {
				return ranks[i];
			}
		}
		return -1;
	}

	/**
	 * 删除,T=O(logN)
	 */
	public void remove(double score) {
		SkipListNode[] updates = new SkipListNode[root.curMaxLevel];
		SkipListNode curNode = root.head, forward = null, tarNode = null;
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			while ((forward = curNode.levels[i].forward) != null && forward.score < score) {
				curNode = forward;
			}
			if (forward != null && forward.score == score) {
				tarNode = forward;
			}
			updates[i] = curNode;
		}
		if (tarNode != null) {
			int level = tarNode.levels.length;
			for (int i = 0; i < level; i++) {
				updates[i].levels[i].forward = tarNode.levels[i].forward;
				if (updates[i].levels[i].forward != null) {
					updates[i].levels[i].span += tarNode.levels[i].span - 1;
				} else {
					updates[i].levels[i].span = 0;
				}
			}
			for (int i = level; i < root.curMaxLevel; i++) {
				if (updates[i].levels[i].forward != null) {
					updates[i].levels[i].span--;
				}
			}
			if (root.tail == tarNode) {
				root.tail = tarNode.backward;
			} else {
				tarNode.levels[0].forward.backward = tarNode.backward;
			}
			root.size--;
		}
	}

	public long size() {
		return root.size;
	}

	public List<Double> range(long start, long end) {
		List<Double> results = new ArrayList<>();
		start = start < 0 ? root.size - Math.abs(start) + 1 : start;
		start = Math.max(1, Math.min(start, root.size - 1));
		end = end < 0 ? root.size - Math.abs(end) + 1 : end;
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
		if (tarNode != null) {
			results.add(tarNode.score);
			for (int i = 0; i < end - start; i++) {
				results.add(tarNode.levels[0].forward.score);
				tarNode = tarNode.levels[0].forward;
			}
		}
		return results;
	}

	public List<Double> rangeByScore(double min, double max) {
		List<Double> results = new ArrayList<>();
		max = Math.max(min, max);
		SkipListNode tarMinNode = null, curNode = root.head, forward = null;
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			while ((forward = curNode.levels[i].forward) != null && forward.score <= min) {
				curNode = forward;
			}
			if (curNode.score == min) {
				tarMinNode = curNode;
				break;
			}
		}
		if (tarMinNode != null) {
			results.add(tarMinNode.score);
			while ((forward = tarMinNode.levels[0].forward) != null && forward.score <= max) {
				results.add(forward.score);
				tarMinNode = forward;
			}
		}
		return results;
	}

}
