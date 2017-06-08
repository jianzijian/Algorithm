package leetcode;

class LT_28_ImplementStr {

	public int strStr(String haystack, String needle) {
		if (haystack != null && needle != null && haystack.length() >= needle.length()) {
			char[] hArr = haystack.toCharArray();
			for (int i = 0; i <= hArr.length - needle.length(); i++) {
				if (String.copyValueOf(hArr, i, needle.length()).equals(needle)) {
					return i;
				}
			}
		}
		return -1;
	}

	public LT_28_ImplementStr() {
		String haystack = "mississippi";
		String needle = "ippi";
		System.out.println(this.strStr(haystack, needle));
	}

	public static void main(String[] args) {
		new LT_28_ImplementStr();
	}

}
