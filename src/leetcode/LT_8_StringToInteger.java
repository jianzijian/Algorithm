package leetcode;

class LT_8_StringToInteger {

	public int myAtoi(String str) {
		int integer = 0;
		int startIndex = 0;
		boolean isNegative = false;
		// 先处理字符串前的空格
		for (; startIndex < str.length(); startIndex++) {
			if (str.charAt(startIndex) == ' ') {
				continue;
			}
			break;
		}
		if (startIndex < str.length()) {
			// 再处理正负号
			char ch = str.charAt(startIndex);
			if (ch == '+') {
				startIndex++;
			} else if (ch == '-') {
				isNegative = true;
				startIndex++;
			}
			// 最后处理数字
			for (; startIndex < str.length(); startIndex++) {
				char innerCh = str.charAt(startIndex);
				if (!isDigit(innerCh)) {
					break;
				}
				long tmp = (long) integer * 10 + Integer.parseInt(innerCh + "");
				if (tmp > Integer.MAX_VALUE) {
					return isNegative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
				}
				integer = (int) tmp;
			}
		}
		return isNegative ? -integer : integer;
	}

	private boolean isDigit(char ch) {
		return ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4' || ch == '5' || ch == '6' || ch == '7'
				|| ch == '8' || ch == '9';
	}

	public LT_8_StringToInteger() {
		System.out.println(this.myAtoi("2147483648"));
	}

	public static void main(String[] args) {
		new LT_8_StringToInteger();
	}

}
