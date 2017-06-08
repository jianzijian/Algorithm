package leetcode;

class LT_6_ZigZag {

	public String convert(String s, int numRows) {
		if (s.isEmpty() || s.length() <= numRows || numRows < 3) {
			return s;
		}
		int unit = 2 * numRows - 2;
		StringBuilder builder = new StringBuilder();
		for (int n = 0; n < numRows; n++) {
			// ��һ�����һ�У������������±���2 * numRows - 2
			if (n == 0 || n == numRows - 1) {
				int tarIndex = n;
				while (tarIndex < s.length()) {
					builder.append(s.charAt(tarIndex));
					tarIndex += unit;
				}
			} else {
				// ��һ����������
				builder.append(s.charAt(n));
				// ����������һ�飬��Ӧ��һ���б�m=lasIndex/2�������±�n0Index=m*(2*numRows-2)
				// ����pre�����±�=n0Index-n��������0��ʼ����las�����±�=n0Index+n
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
