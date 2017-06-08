package leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class LT_3Sum {

	public List<List<Integer>> threeSum(int[] nums) {
		List<List<Integer>> results = new ArrayList<>();
		// 排序一是为了固定第1个值后用头尾逼近法(二分查找变形)查找2、3值，同时也是为了方便排除重复解
		Arrays.sort(nums);
		// i + 2 < nums.length,这是因为得有3个数（3Sum）
		for (int i = 0; i + 2 < nums.length; i++) {
			// 理论上只有第一个解才是不可能有重复的（i>0），其他的每一个解都有可能与之前的解重复</BR>
			// 相等证明效果等同，可以当成验证过了
			if (i > 0 && nums[i] == nums[i - 1]) {
				continue;
			}
			int j = i + 1, k = nums.length - 1;
			while (j < k) {
				if (nums[j] + nums[k] == -nums[i]) {
					results.add(Arrays.asList(nums[i], nums[j], nums[k]));
					// 下一波2、3值
					j++;
					k--;
					// 相等证明效果等同，可以当成验证过了
					while (j < k && nums[j] == nums[j - 1])
						j++;
					// 相等证明效果等同，可以当成验证过了
					while (j < k && nums[k] == nums[k + 1])
						k--;
				} else if (nums[j] + nums[k] < -nums[i]) {
					j++;
				} else {
					k--;
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

	public LT_3Sum() {
		int[] nums = new int[] { -1, 0, 1, 2, -1, -4 };
		List<List<Integer>> results = this.threeSum(nums);
		for (List<Integer> result : results) {
			System.err.println(this.asString(result));
		}
	}

	public static void main(String[] args) {
		new LT_3Sum();
	}

}
