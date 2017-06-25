package redis.skiplist;

import java.util.Random;

public class CommonSkipList {

	private static final int DEFAULT_MIN_MAX_LEVEL = 1;
	private static final int DEFAULT_MAX_MAX_LEVEL = 31;

	private static class SkipListLevel {
		// ǰ��ָ��
		private SkipListNode forward;
		// ���
		private long span;
	}

	private static class SkipListNode {
		// ǰ��ָ���
		private final SkipListLevel[] levels;
		// ����ָ��
		private SkipListNode backward;
		// ��ֵ
		private final double score;
		// // ֵ
		// private final Object value = null;

		private SkipListNode(int level, double score) {
			levels = new SkipListLevel[level];
			backward = null;
			this.score = score;
		}
	}

	private static class SkipListRoot {
		// ��ͷ�ڵ㣬������洢
		private SkipListNode head;
		// ��β�ڵ㣬����洢
		private SkipListNode tail;
		private int curMaxLevel;
		private long size;

		private SkipListRoot(int maxLevel) {
			// ��ͷ�ڵ�����̶��������������������
			head = new SkipListNode(maxLevel, 0);
		}
	}

	// ��������������Ԫ�ظ���cnt������Ĺ�ϵӦ������2^(maxLevel+1)~=cnt
	private final int maxLevel;
	private final SkipListRoot root;
	// ����random��ز���
	private int[] powers;
	private Random rd = new Random();

	public CommonSkipList(int maxLevel) {
		this.maxLevel = this.fixMaxLevel(maxLevel);
		root = new SkipListRoot(this.maxLevel);
		this.choosePowers();
	}

	/**
	 * ����������1-31֮�䣬Ҫע�⵽������Ϊ1���˻���������
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
	 * ��Ծ�������۰����+�������Ļ���ʵ�֣�Ԫ�س����ڵ�n��ĸ���ӦΪ1/2^(n-1)
	 */
	private int randomLevel() {
		// �����������1-2^(maxLevel-1)֮�䣬��������Ĳ�����Ȼ��1-maxLevel֮��
		// �����ݴζ��ɣ�������Ĳ������ɱ�Ȼ���������м�࣬�����ò��������㹻���
		int r = rd.nextInt(powers[maxLevel - 1]) + 1;
		for (int i = maxLevel - 1; i >= 0; i--) {
			if (r >= powers[i]) {
				return i + 1;
			}
		}
		return 1; // �������ⷢ������֤Ԫ���ܲ����1��
	}

	/**
	 * ���벢��������,T=O(logN)
	 */
	public int add(double score) {
		int level = this.randomLevel();
		// ��ʼ����ͷ�ڵ�δʹ�ù��Ĳ�
		if (level > root.curMaxLevel) {
			for (int i = root.curMaxLevel; i < level; i++) {
				root.head.levels[i] = new SkipListLevel();
			}
			root.curMaxLevel = level;
		}
		// ÿһ���п�����Ҫ���µĽڵ�
		SkipListNode[] updates = new SkipListNode[root.curMaxLevel];
		// ÿһ���п�����Ҫ���µĽڵ����λ��֮���������㱻���½ڵ㡢�½ڵ�ĸ����ȣ�
		int[] ranks = new int[root.curMaxLevel];
		SkipListNode curNode = root.head;
		SkipListNode forward = null;
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			// ��ȸ����ۼ�
			ranks[i] = i == root.curMaxLevel - 1 ? 0 : ranks[i + 1];
			// ��������ͬ�ڵ㣨���½ڵ�Ӧ����ɾ�������²��룩
			while ((forward = curNode.levels[i].forward) != null && forward.score < score) {
				ranks[i] += curNode.levels[i].span;
				curNode = forward;
			}
			// �ڵ����ֱ�ӷ�����λ
			if (forward.score == score) {
				return ranks[i] + 1;
			}
			updates[i] = curNode;
		}
		// ���벢�������
		SkipListNode tarNode = new SkipListNode(level, score);
		for (int i = level; i >= 0; i--) {
			tarNode.levels[i].forward = updates[i].levels[i].forward;
			updates[i].levels[i].forward = tarNode;
			tarNode.levels[i].span = updates[i].levels[i].span - (ranks[0] - ranks[i]) + 1;
			updates[i].levels[i].span = ranks[0] - ranks[i] + 1;
		}
		// δֱ�ӽӴ���ֱ�ӿ���ˣ��Ľڵ���ҲҪ+1
		for (int i = level; i < root.curMaxLevel; i++) {
			if (updates[i].levels[i].forward != null) {
				updates[i].levels[i].span++;
			}
		}
		// ��������ָ��
		tarNode.backward = updates[0] == root.head ? null : updates[0];
		// ���±�β�ڵ�or�����½ڵ��1��ǰ���ڵ�ĺ���ָ�루1��Ԫ��ǰ����Ⱥ�=1��
		if (tarNode.levels[0].forward != null) {
			tarNode.levels[0].forward.backward = tarNode;
		} else {
			root.tail = tarNode;
		}
		root.size++;
		return ranks[0] + 1;
	}

	/**
	 * ��������,T=O(logN)
	 */
	public int rank(double score) {
		SkipListNode curNode = root.head;
		SkipListNode forward = null;
		int[] ranks = new int[root.curMaxLevel];
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			ranks[i] = i == root.curMaxLevel - 1 ? 0 : ranks[i + 1];
			while ((forward = curNode.levels[i].forward) != null && forward.score < score) {
				ranks[i] += curNode.levels[i].span;
				curNode = forward;
			}
			if (forward.score == score) {
				return ranks[i] + 1;
			}
		}
		return -1;
	}

	/**
	 * ɾ��,T=O(logN)
	 */
	public void remove(double score) {
		SkipListNode[] updates = new SkipListNode[root.curMaxLevel];
		SkipListNode curNode = root.head, forward = null, tarNode = null;
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			while ((forward = curNode.levels[i].forward) != null && forward.score < score) {
				curNode = forward;
			}
			if (forward.score == score) {
				tarNode = forward;
			}
			updates[i] = curNode;
		}
		if (curNode != null) {
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

	public double[] rangeByRank(int min, int max) {
		min = min < 0 ? 0 : min;

		return null;
	}

	public double[] rangeByScore(int min, int max) {

		return null;
	}

}