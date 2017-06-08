package leetcode;

class LT_5_LongestPalindromicString {

	private int startIndex = 0, maxLength = 0;

	public String longestPalindrome(String s) {
		if (s.length() < 2) {
			return s;
		}
		for (int i = 0; i < s.length() - 1; i++) {
			// 对于每一个字符有两种情况，1本身就是个中心点，2与后一个元素互为中心点
			this.tryEntendPalindrome(s, i, i);
			this.tryEntendPalindrome(s, i, i + 1);
		}
		return s.substring(startIndex, startIndex + maxLength);
	}

	private void tryEntendPalindrome(String s, int leftIndex, int rightIndex) {
		while (leftIndex >= 0 && rightIndex < s.length() && s.charAt(leftIndex) == s.charAt(rightIndex)) {
			leftIndex--;
			rightIndex++;
		}
		leftIndex++;
		rightIndex--;
		int tmpLength = rightIndex - leftIndex + 1;
		if (maxLength < tmpLength) {
			maxLength = tmpLength;
			startIndex = leftIndex;
		}
	}

	public String longestPalindromeSpecial(String s) {
		if (s.length() < 2) {
			return s;
		}
		int length = s.length();
		int startIndex = 0, endIndex = 0;
		boolean[][] dp = new boolean[length][length];
		for (int i = 0; i < length; i++) {
			for(int j=0;j<=i;j++){
				dp[i][j] = s.charAt(i) == s.charAt(j) && (i - j < 3 || dp[i - 1][j + 1]);
				if (dp[i][j] && i - j + 1 > endIndex - startIndex + 1) {
					endIndex = i;
					startIndex = j;
				}
			}
		}
		return s.substring(startIndex, endIndex + 1);
	}

	public LT_5_LongestPalindromicString() {
		String s = "abc";
		System.out.println(this.longestPalindromeSpecial(s));
	}

	public static void main(String[] args) {
		new LT_5_LongestPalindromicString();
	}

}
