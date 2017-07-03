package redis.skiplist;

import java.util.TreeMap;

public class Test {

	public static void main(String[] args) {
		int cnt = 1000000;
		doTestSortedSet(cnt);
		doTestTreeMap(cnt);
	}

	private static int log(double value, double base) {
		return (int) Math.ceil(Math.log(value) / Math.log(base));
	}

	private static void doTestSortedSet(int cnt) {
		long startTime = System.currentTimeMillis();
		int maxLevel = log(cnt, 2);
		System.out.println(maxLevel);
		SkipList<TestMergeScore, Integer> skipList = new SkipList<>(maxLevel);
		for (int i = 0; i < cnt; i++) {
			skipList.add(new TestMergeScore(i), i);
		}
		System.out.println(System.currentTimeMillis() - startTime);
	}

	private static void doTestTreeMap(int cnt) {
		long startTime = System.currentTimeMillis();
		TreeMap<TestMergeScore, Integer> treeMap = new TreeMap<>((s1, s2) -> s1.score - s2.score);
		for (int i = 0; i < cnt; i++) {
			treeMap.put(new TestMergeScore(i), i);
		}
		System.out.println(System.currentTimeMillis() - startTime);
	}

	private static class TestMergeScore implements IMergeScore<TestMergeScore> {

		private final int score;

		private TestMergeScore(int score) {
			this.score = score;
		}

		@Override
		public int compareTo(TestMergeScore o) {
			return this.score - o.score;
		}

		@Override
		public TestMergeScore merge(TestMergeScore original) {
			return new TestMergeScore(this.score + original.score);
		}

	}

}
