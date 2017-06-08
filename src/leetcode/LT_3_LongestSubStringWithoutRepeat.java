package leetcode;

import java.util.HashMap;
import java.util.Map;

class LT_3_LongestSubStringWithoutRepeat {

	public int lengthOfLongestSubstring(String s) {
		char[] array = s.toCharArray();
		// 无重复子串的开始下标，一旦重复，直接重复字符前一个位置+1（确保由开始位置到当前index的子串都是无重复的）
		int startIndex = 0, maxSubStrLength = 0;
		Map<Character, Integer> ch2Index = new HashMap<>();
		for (int index = 0; index < array.length; index++) {
			char ch = array[index];
			Integer preIndex = ch2Index.get(ch);
			if (preIndex != null && preIndex >= startIndex) {
				startIndex = preIndex + 1;
				ch2Index.put(ch, index);
			} else {
				ch2Index.put(ch, index);
				int length = index - startIndex + 1;
				if (length > maxSubStrLength) {
					maxSubStrLength = length;
				}
			}
		}
		return maxSubStrLength;
	}

	public int lengthOfLongestSubStringSpecial(String s) {
		int max = 0;
		Map<Character, Integer> ch2Index = new HashMap<>();
		char[] array = s.toCharArray();
		for (int index = 0, startIndex = 0; index < array.length; index++) {
			char ch = array[index];
			Integer preIndex = ch2Index.get(ch);
			if (preIndex != null) {
				startIndex = Math.max(startIndex, preIndex + 1);
			}
			ch2Index.put(ch, index);
			max = Math.max(max, index - startIndex + 1);
		}
		return max;
	}

	public LT_3_LongestSubStringWithoutRepeat() {
		String s = "tmmzuxt";
		System.out.println(this.lengthOfLongestSubStringSpecial(s));
	}

	public static void main(String[] args) {
		new LT_3_LongestSubStringWithoutRepeat();
	}

}
