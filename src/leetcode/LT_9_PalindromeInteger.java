package leetcode;

class LT_9_PalindromeInteger {

	public boolean isPalindrome(int x) {
		return x == this.reverse(x);
	}

	private int reverse(int x) {
		if (x < 0) { // 负数肯定不是回文数
			return 0;
		}
		long reverse = 0;
		while (x > 0) {
			int remain = x % 10;
			x = x / 10;
			reverse = reverse * 10 + remain;
		}
		reverse = reverse > Integer.MAX_VALUE ? 0 : reverse;
		return (int) reverse;
	}

	public boolean isPalindromeByString(int x) {
		if (x < 0) {
			return false;
		}
		String str = String.valueOf(x);
		int head = 0, tail = str.length() - 1;
		while (head < tail) {
			if (str.charAt(head++) != str.charAt(tail--)) {
				return false;
			}
		}
		return true;
	}

	public boolean isPalindromeSpecial(int x) {
		// 负数和非零正整数但最后一位是0的肯定不是回文数
		if (x < 0 || (x != 0 && x % 10 == 0)) {
			return false;
		}
		int rev = 0;
		while (x > rev) { // 其实翻转一半就足够了
			rev = rev * 10 + x % 10;
			x /= 10;
		}
		return rev == x || rev / 10 == x; // 后面的情况是这个回文数的位数刚好是奇数
	}

}
