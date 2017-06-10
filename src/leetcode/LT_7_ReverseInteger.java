package leetcode;

class LT_7_ReverseInteger {

	public int reverse(int x) {
		boolean isNegative = false;
		if (x < 0) {
			if (x == Integer.MIN_VALUE) {
				return 0;
			}
			isNegative = true;
			x = -x;
		}
		long reverse = 0;
		while (x > 0) {
			int remain = x % 10;
			x = x / 10;
			reverse = reverse * 10 + remain;
		}
		reverse = reverse > Integer.MAX_VALUE ? 0 : reverse;
		return (int) (isNegative ? -reverse : reverse);
	}

}
