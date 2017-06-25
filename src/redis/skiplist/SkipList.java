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
		// ֵ
		private final Object value;

		private SkipListNode(int level, double score, Object value) {
			levels = new SkipListLevel[level];
			backward = null;
			this.score = score;
			this.value = value;
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
			head = new SkipListNode(maxLevel, 0, null);
		}
	}

	// ��������������Ԫ�ظ���cnt������Ĺ�ϵӦ������2^(maxLevel+1)~=cnt
	private final int maxLevel;
	private final SkipListRoot root;
	// ����random��ز���
	private int[] powers;
	private Random rd = new Random();

	public SkipList(int maxLevel) {
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

	public int add(double score, Object value) {
		if (value == null) {
			throw new NullPointerException();
		}
		// ��ɾ���ظ��Ľڵ�
		this.deleteNode(score, value);
		return this.insertNode(score, value);
	}

	public int incr(double incrScore, Object value) {
		if (value == null) {
			throw new NullPointerException();
		}
		double score = incrScore;
		// ��ɾ���ظ��Ľڵ�
		SkipListNode tarNode = this.deleteNode(score, value);
		score = tarNode != null ? tarNode.score + score : score;
		return this.insertNode(score, value);
	}

	/**
	 * ��������,T=O(logN)
	 */
	private int insertNode(double score, Object value) {
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
			// ��ͬ��ֵ�Ȳ���Ľڵ���λ��ǰ����������ͬ�ڵ㣨���½ڵ�Ӧ����ɾ�������²��룩
			while ((forward = curNode.levels[i].forward) != null && forward.score <= score) {
				ranks[i] += curNode.levels[i].span;
				curNode = forward;
			}
			updates[i] = curNode;
		}
		// ���벢�������
		SkipListNode tarNode = new SkipListNode(level, score, value);
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
	 * ���ر��ɹ��Ƴ��Ľڵ���,T=M*O(logN)+O(N)
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
			// ���жϣ�ֱ���ҵ���������ڸýڵ��ǰ�ýڵ�
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
			// ���±�β�ڵ�or�����½ڵ��1��ǰ���ڵ�ĺ���ָ��
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