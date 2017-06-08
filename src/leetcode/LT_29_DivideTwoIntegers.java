package leetcode;

class LT_29_DivideTwoIntegers {

	/**
	 * ������ʵ����nb<=a��ͨ��λ�Ƽ�����b*2^N<a��b*2^��N+1��>a����2^N<n<2^��N+1��
	 * �����κ�0<x<2^��N+1����������x,2^0...2^N�����ж������ҵ���Ӧ������ʹ�ú�=x��������ң�
	 */
	public int divide(int a, int b) {
		boolean neg = (a > 0) ^ (b > 0);
		long la = a, lb = b;
		if (a < 0) {
			la = -(long) a;
		}
		if (b < 0) {
			lb = -(long) b;
		}
		int msb = 0;
		long tmp = 0;
		// msd��¼������Ҫ���Ƶ�λ��
		for (msb = 0; msb < 32; msb++) {
			tmp = lb << msb;
			if (tmp >= la)
				break;
		}
		long q = 0; // ��¼ÿ�γ�������
		for (int i = msb; i >= 0; i--) {
			tmp = lb << i;
			if (tmp > la)
				continue;
			q |= (1L << i); // ������+�Ÿ�������⣬����������2^0...2^N����2^0|2^1...|2^N=2^0+2^1...+2^N�������һ����
			la -= (lb << i);
		}
		if(neg){
			q = -q;
		}
		if (q > Integer.MAX_VALUE || q < Integer.MIN_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) q;
	}

	public LT_29_DivideTwoIntegers() {
		System.out.println(this.divide(Integer.MIN_VALUE, 1));
	}

	public static void main(String[] args) {
		new LT_29_DivideTwoIntegers();
	}
}
