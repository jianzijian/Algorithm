package leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class LT_4Sum {

	public List<List<Integer>> fourSum(int[] nums, int target) {
		List<List<Integer>> results = new ArrayList<>();
		Arrays.sort(nums);
		for (int i = 0; i + 3 < nums.length; i++) {
			if (i > 0 && nums[i] == nums[i - 1]) {
				continue;
			}
			int subTarget = target - nums[i];
			for (int ii = i + 1; ii + 2 < nums.length; ii++) {
				// 每个外层解的第一波肯定是不可能重复的，故ii > i + 1
				if (ii > i + 1 && nums[ii] == nums[ii - 1]) {
					continue;
				}
				int j = ii + 1, k = nums.length - 1;
				int ssubTarget = subTarget - nums[ii];
				while (j < k) {
					if (nums[j] + nums[k] == ssubTarget) {
						results.add(Arrays.asList(nums[i], nums[ii], nums[j], nums[k]));
						j++;
						k--;
						while (j < k && nums[j] == nums[j - 1])
							j++;
						while (j < k && nums[k] == nums[k + 1])
							k--;
					} else if (nums[j] + nums[k] < ssubTarget) {
						j++;
					} else {
						k--;
					}
				}
			}
		}
		return results;
	}

	private String asString(List<Integer> nums) {
		if (nums.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		nums.forEach(num -> builder.append(num + ","));
		return builder.substring(0, builder.length() - 1);
	}

	public LT_4Sum() {
		int[] nums = new int[] { -1, 0, 1, 2, -1, -4 };
		List<List<Integer>> results = this.fourSum(nums, -1);
		for (List<Integer> result : results) {
			System.err.println(this.asString(result));
		}
	}

	public static void main(String[] args) {
		new LT_4Sum();
	}

}
