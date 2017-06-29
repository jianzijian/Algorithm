package redis.skiplist;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class SkipList<S extends IMergeScore<S>, V> {

	private static final int DEFAULT_MIN_MAX_LEVEL = 1;
	private static final int DEFAULT_MAX_MAX_LEVEL = 31;

	private static class SkipListLevel {
		// ǰ��ָ��
		SkipListNode forward;
		// ���
		long span;
	}

	private static class SkipListNode {
		// ǰ��ָ���
		final SkipListLevel[] levels;
		// ����ָ��
		SkipListNode backward;
		// ��ֵ
		final Object score;
		// ֵ
		final Object value;

		private SkipListNode(int level, Object score, Object value) {
			levels = new SkipListLevel[level];
			backward = null;
			this.score = score;
			this.value = value;
		}
	}

	private static class SkipListRoot {
		// ��ͷ�ڵ㣬������洢
		SkipListNode head;
		// ��β�ڵ㣬����洢
		SkipListNode tail;
		int curMaxLevel;
		long size;

		private SkipListRoot(int maxLevel) {
			// ��ͷ�ڵ�����̶��������������������
			head = new SkipListNode(maxLevel, 0, null);
		}
	}

	// �������������Ԫ�ظ���cnt������Ĺ�ϵӦ������2^(maxLevel+1)~=cnt
	private final int maxLevel;
	private final SkipListRoot root;
	// ����random��ز���
	private int[] powers;
	private Random rd = new Random();

	SkipList(int maxLevel) {
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
	 * ��Ծ������۰����+������Ļ���ʵ�֣�Ԫ�س����ڵ�n��ĸ���ӦΪ1/2^(n-1)
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

	@SuppressWarnings({ "unchecked" })
	private S getSKey(SkipListNode node) {
		return (S) node.score;
	}

	@SuppressWarnings("unchecked")
	private V getValue(SkipListNode node) {
		return (V) node.value;
	}

	/**
	 * �����½ڵ㲢����������ע��÷����������ظ��ڵ㣩��T=O(logN)
	 */
	long add(S score, V value) {
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
		long[] ranks = new long[root.curMaxLevel];
		SkipListNode curNode = root.head;
		SkipListNode forward = null;
		for (int i = root.curMaxLevel - 1; i >= 0; i--) {
			// ��ȸ����ۼ�
			ranks[i] = i == root.curMaxLevel - 1 ? 0 : ranks[i + 1];
			// ��������ͬ�ڵ㣨���½ڵ�Ӧ����ɾ�������²��룩����ͬ��ֵ�Ȳ���������ǰ
			while ((forward = curNode.levels[i].forward) != null && (this.getSKey(forward).compareTo(score) < 0
					|| this.getSKey(forward).compareTo(score) == 0 && !this.getValue(forward).equals(value))) {
				ranks[i] += curNode.levels[i].span;
				curNode = forward;
			}
			updates[i] = curNode;
		}
		// ���벢�������
		SkipListNode tarNode = new SkipListNode(level, score, value);
		for (int i = level - 1; i >= 0; i--) {
			tarNode.levels[i] = new SkipListLevel();
			tarNode.levels[i].forward = updates[i].levels[i].forward;
			updates[i].levels[i].forward = tarNode;
			tarNode.levels[i].span = tarNode.levels[i].forward == null ? 0
					: updates[i].levels[i].span - (ranks[0] - ranks[i]) + 1;
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
		// ���������ڵ��ǰ��ָ���Լ����
		for (int i = 0; i < level; i++) {
			updates[i].levels[i].forward = tarNode.levels[i].forward;
			if (updates[i].levels[i].forward != null) {
				updates[i].levels[i].span += tarNode.levels[i].span - 1;
			} else {
				updates[i].levels[i].span = 0;
			}
		}
		// ����δֱ�������Ľڵ�Ŀ�ȣ�����˵Ľڵ㣩
		for (int i = level; i < root.curMaxLevel; i++) {
			if (updates[i].levels[i].forward != null) {
				updates[i].levels[i].span--;
			}
		}
		// ����βָ��or����ָ��
		if (root.tail == tarNode) {
			root.tail = tarNode.backward;
		} else {
			tarNode.levels[0].forward.backward = tarNode.backward;
		}
		// ������Ծ�����߶�
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
	 * T=O(logN+M)��M��Ҫ��ȡ��Ԫ������
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
	 * T=O(logN+M)��M��Ҫ��ȡ��Ԫ������
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
