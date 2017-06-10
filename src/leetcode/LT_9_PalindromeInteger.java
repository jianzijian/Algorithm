package leetcode;

class LT_9_PalindromeInteger {

	public boolean isPalindrome(int x) {
		return x == this.reverse(x);
	}

	private int reverse(int x) {
		if (x < 0) { // �����϶����ǻ�����
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
		// �����ͷ��������������һλ��0�Ŀ϶����ǻ�����
		if (x < 0 || (x != 0 && x % 10 == 0)) {
			return false;
		}
		int rev = 0;
		while (x > rev) { // ��ʵ��תһ����㹻��
			rev = rev * 10 + x % 10;
			x /= 10;
		}
		return rev == x || rev / 10 == x; // ���������������������λ���պ�������
	}

}
