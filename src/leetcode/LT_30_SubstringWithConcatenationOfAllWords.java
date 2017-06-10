package leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		char[] chs = s.toCharArray();
		for (int i = 0; i <= chs.length - allWordLength; i++) {
			Set<Integer> wordIndexs = new HashSet<>();
			for (int j = i; j <= chs.length - wordLength; j += wordLength) {
				int tmpIndex = -1;
				String subString = String.copyValueOf(chs, j, wordLength);
				for (int k = 0; k < words.length; k++) {
					if (wordIndexs.contains(k) || !subString.equals(words[k])) {
						continue;
					}
					tmpIndex = k;
					break;
				}
				if (tmpIndex != -1) {
					wordIndexs.add(tmpIndex);
					continue;
				}
				break;
			}
			if (wordIndexs.size() == words.length) {
				tarIndexs.add(i);
			}
		}
		return tarIndexs;
	}

	public List<Integer> findSubstringWithMap(String s, String[] words) {
		List<Integer> tarIndexs = new ArrayList<>();
		if (s == null || words == null || words.length == 0) {
			return tarIndexs;
		}
		int wordLength = words[0].length();
		int allWordLength = wordLength * words.length;
		if (s.length() < allWordLength) {
			return tarIndexs;
		}
		char[] chs = s.toCharArray();
		Map<String, Integer> word2Cnt = new HashMap<>();
		for (String word : words) {
			Integer cnt = word2Cnt.get(word);
			cnt = cnt == null ? 1 : cnt + 1;
			word2Cnt.put(word, cnt);
		}
		for (int i = 0; i <= chs.length - allWordLength; i++) {
			Map<String, Integer> tmpWord2Cnt = new HashMap<>(word2Cnt);
			for (int j = i; j <= chs.length - wordLength; j += wordLength) {
				String subString = String.copyValueOf(chs, j, wordLength);
				Integer cnt = tmpWord2Cnt.get(subString);
				if (cnt == null) {
					break;
				} else if (cnt == 1) {
					tmpWord2Cnt.remove(subString);
				} else {
					tmpWord2Cnt.put(subString, --cnt);
				}
			}
			if (tmpWord2Cnt.isEmpty()) {
				tarIndexs.add(i);
			}
		}
		return tarIndexs;
	}

	public List<Integer> findSubstringWithPL(String s, String[] words) {
		List<Integer> tarIndexs = new ArrayList<>();
		if (s == null || words == null || words.length == 0) {
			return tarIndexs;
		}
		int wordLength = words[0].length();
		int allWordLength = wordLength * words.length;
		if (s.length() < allWordLength) {
			return tarIndexs;
		}
		char[] chs = s.toCharArray();
		Set<String> allWordPL = new HashSet<>();
		this.pl(Arrays.asList(words), new ArrayList<>(), allWordPL);
		for (int i = 0; i <= chs.length - allWordLength; i++) {
			String subString = String.copyValueOf(chs, i, allWordLength);
			if (allWordPL.contains(subString)) {
				tarIndexs.add(i);
			}
		}
		return tarIndexs;
	}

	public void pl(List<String> s, List<String> rs, Set<String> results) {
		if (s.size() == 1) {
			rs.add(s.get(0));
			results.add(this.listToString(rs));
			rs.remove(rs.size() - 1);
		} else {
			for (int i = 0; i < s.size(); i++) {
				rs.add(s.get(i));
				List<String> tmp = new ArrayList<>();
				for (String a : s)
					tmp.add(a);
				tmp.remove(i);
				pl(tmp, rs, results);
				rs.remove(rs.size() - 1);
			}
		}
	}

	public String listToString(List<String> rs) {
		StringBuilder sb = new StringBuilder();
		rs.forEach(str -> sb.append(str));
		return sb.toString();
	}

	public LT_30_SubstringWithConcatenationOfAllWords() {
		String s = "barfoothefoobarman";
		String[] words = new String[] { "foo", "bar" };
		List<Integer> tarIndexs = this.findSubstringWithPL(s, words);
		for (int index : tarIndexs) {
			System.out.println(index);
		}
	}

	public static void main(String[] args) {
		new LT_30_SubstringWithConcatenationOfAllWords();
	}

}
