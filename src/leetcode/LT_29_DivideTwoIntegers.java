package leetcode;

class LT_29_DivideTwoIntegers {

	/**
	 * 除法其实就是nb<=a，通过位移计算有b*2^N<a且b*2^（N+1）>a，则2^N<n<2^（N+1）
	 * 对于任何0<x<2^（N+1）的正整数x,2^0...2^N序列中都可以找到对应的序列使得和=x（倒序查找）
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
		// msd记录除数需要左移的位数
		for (msb = 0; msb < 32; msb++) {
			tmp = lb << msb;
			if (tmp >= la)
				break;
		}
		long q = 0; // 记录每次除法的商
		for (int i = msb; i >= 0; i--) {
			tmp = lb << i;
			if (tmp > la)
				continue;
			q |= (1L << i); // 这里用+号更容易理解，但对于序列2^0...2^N，有2^0|2^1...|2^N=2^0+2^1...+2^N，结果是一样的
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
