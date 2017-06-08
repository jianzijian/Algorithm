package leetcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LT_30_SubstringWithConcatenationOfAllWords {

	public List<Integer> findSubstring(String s, String[] words) {
		List<Integer> tarIndexs = new ArrayList<>();
		if (s == null || words == null || words.length == 0) {
			return tarIndexs;
		}
		int wordLength = words[0].length();
		int allWordLength = wordLength * words.length;
		if (s.length() < allWordLength) {
			return tarIndexs;
		}
		Map<String, Integer> word2Cnt = new HashMap<>();
		for (String word : words) {
			Integer cnt = word2Cnt.get(word);
			cnt = cnt == null ? 1 : cnt + 1;
			word2Cnt.put(word, cnt);
		}
		// 要把字符串按n个字符切分，开始下标在0...n-1即可
		for (int i = 0; i < wordLength; i++) {
			int left = i, count = 0;
			Map<String, Integer> tmpWord2Cnt = new HashMap<>();
			for (int j = i; j <= s.length() - wordLength; j += wordLength) {
				String subWordLengthStr = s.substring(j, j + wordLength);
				if (word2Cnt.containsKey(subWordLengthStr)) {
					Integer cnt = tmpWord2Cnt.get(subWordLengthStr);
					if (cnt == null) {
						cnt = 0;
					}
					tmpWord2Cnt.put(subWordLengthStr, cnt + 1);
					count++;
					// 按wordLength长度单位重复后退，直到以left开始的字符串中的subWordLengthStr数量<=word2Cnt.get(subWordLengthStr)
					while (tmpWord2Cnt.get(subWordLengthStr) > word2Cnt.get(subWordLengthStr)) {
						String subStrLeft = s.substring(left, left + wordLength);
						tmpWord2Cnt.put(subStrLeft, tmpWord2Cnt.get(subStrLeft) - 1);
						count--; // 减掉了一个wordLength长度的字符串，记得要把count-1
						left += wordLength;
					}
					if (count == words.length) {
						tarIndexs.add(left);
					}
				} else { // 出现不存在的字符串str，包含str的字符串都不可能成为目标字符串，直接从str之后开始
					left = j + wordLength;
					count = 0;
					tmpWord2Cnt.clear();
				}
			}
		}
		return tarIndexs;
	}

	public LT_30_SubstringWithConcatenationOfAllWords() {
		String s = "barfoofoobarthefoobarman";
		String[] words = new String[] { "bar", "foo", "the" };
		for (int index : this.findSubstring(s, words)) {
			System.out.println(index);
		}
	}

	public static void main(String[] args) {
		new LT_30_SubstringWithConcatenationOfAllWords();
	}

}
