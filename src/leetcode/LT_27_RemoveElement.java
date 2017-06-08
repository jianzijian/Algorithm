package leetcode;

/**
 * Given an array and a value, remove all instances of that value in place and
 * return the new length.<BR>
 * Do not allocate extra space for another array, you must do this in place with
 * constant memory.<BR>
 * The order of elements can be changed. It doesn't matter what you leave beyond
 * the new length.<BR>
 * 
 * 在指定数组中移除指定数，不能分配新数组（其实意思是把非指定数的元素移动到指定数之前）
 * 
 * @author jianzijian
 *
 */
public class LT_27_RemoveElement {

	public int removeElement(int[] nums, int val) {
		if (nums == null) {
			return 0;
		}
		int startIndex = 0, valIndex = nums.length - 1, count = 0;
		while (startIndex <= valIndex) {
			if (nums[startIndex] != val) {
				count++;
			} else {
				while (startIndex < valIndex && nums[valIndex] == val) {
					valIndex--;
				}
				if (startIndex != valIndex) {
					count++;
					nums[startIndex] = nums[valIndex];
					valIndex--;
				}
			}
			startIndex++;
		}
		return count;
	}

	/**
	 * 逐个插入（相当于舍弃掉所有val）
	 */
	public int removeElementSpecial(int[] nums, int val) {
		int insertIndex = 0;
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] != val) {
				nums[insertIndex++] = nums[i];
			}
		}
		return insertIndex;
	}

	public LT_27_RemoveElement() {
		int[] nums = new int[] { 3, 2, 3 };
		int val = 3;
		System.out.println(this.removeElementSpecial(nums, val));
	}

	public static void main(String[] args) {
		new LT_27_RemoveElement();
	}

}
