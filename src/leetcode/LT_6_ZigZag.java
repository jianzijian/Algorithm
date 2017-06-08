package leetcode;

class LT_6_ZigZag {

	public String convert(String s, int numRows) {
		if (s.isEmpty() || s.length() <= numRows || numRows < 3) {
			return s;
		}
		int unit = 2 * numRows - 2;
		StringBuilder builder = new StringBuilder();
		for (int n = 0; n < numRows; n++) {
			// 第一行最后一行，相邻两个数下标差距2 * numRows - 2
			if (n == 0 || n == numRows - 1) {
				int tarIndex = n;
				while (tarIndex < s.length()) {
					builder.append(s.charAt(tarIndex));
					tarIndex += unit;
				}
			} else {
				// 第一个单独处理
				builder.append(s.charAt(n));
				// 其他的两两一组，对应第一行列标m=lasIndex/2，具体下标n0Index=m*(2*numRows-2)
				// 所以pre具体下标=n0Index-n（行数，0开始），las具体下标=n0Index+n
				for (int m = 1;; m += 2) {
					int n0Index = (m + 1) / 2 * unit;
					int preIndex = n0Index - n;
					if (preIndex >= s.length()) {
						break;
					}
					builder.append(s.charAt(preIndex));
					int lasIndex = n0Index + n;
					if (lasIndex >= s.length()) {
						break;
					}
					builder.append(s.charAt(lasIndex));
				}
			}
		}
		return builder.toString();
	}

	public LT_6_ZigZag() {
		String s = "PAYPALISHIRING";
		int numRows = 3;
		System.out.println(this.convert(s, numRows));
	}

	public static void main(String[] args) {
		new LT_6_ZigZag();
	}

}
