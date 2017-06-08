package leetcode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class LT_1_TwoSum {

	public int[] twoSum(int[] nums, int target) {
		int[] result = new int[2];
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < nums.length; i++) {
			if (map.containsKey(target - nums[i])) {
				result[1] = i;
				result[0] = map.get(target - nums[i]);
				return result;
			}
			map.put(nums[i], i);
		}
		return result;
	}

	public int[] twoSumBinary(int[] nums, int target) {
		Arrays.sort(nums);
		for (int i = 0; i < nums.length; i++) {
			if (i > 0 && nums[i] == nums[i - 1]) {
				continue;
			}
			int tarNum = target - nums[i];
			int j = i + 1, k = nums.length - 1;
			while (j <= k) {
				int mid = (j + k) / 2;
				if (nums[mid] < tarNum) {
					j = mid + 1;
				} else if (nums[mid] > tarNum) {
					k = mid - 1;
				} else {
					return new int[] { i, mid };
				}
			}
		}
		return null;
	}

	public LT_1_TwoSum() {
		int[] nums = new int[] { 3, 2, 4 };
		int target = 6;
		int[] indexs = this.twoSum(nums, target);
		System.out.println(indexs == null ? "" : indexs[0] + " " + indexs[1]);
	}

	public static void main(String[] args) {
		new LT_1_TwoSum();
	}

}
