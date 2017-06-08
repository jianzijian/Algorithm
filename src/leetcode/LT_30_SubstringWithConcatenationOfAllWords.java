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
		// Ҫ���ַ�����n���ַ��з֣���ʼ�±���0...n-1����
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
					// ��wordLength���ȵ�λ�ظ����ˣ�ֱ����left��ʼ���ַ����е�subWordLengthStr����<=word2Cnt.get(subWordLengthStr)
					while (tmpWord2Cnt.get(subWordLengthStr) > word2Cnt.get(subWordLengthStr)) {
						String subStrLeft = s.substring(left, left + wordLength);
						tmpWord2Cnt.put(subStrLeft, tmpWord2Cnt.get(subStrLeft) - 1);
						count--; // ������һ��wordLength���ȵ��ַ������ǵ�Ҫ��count-1
						left += wordLength;
					}
					if (count == words.length) {
						tarIndexs.add(left);
					}
				} else { // ���ֲ����ڵ��ַ���str������str���ַ����������ܳ�ΪĿ���ַ�����ֱ�Ӵ�str֮��ʼ
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
