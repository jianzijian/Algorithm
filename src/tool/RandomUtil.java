package tool;

import java.util.Random;

class RandomUtil {

	/**
	 * ���������num�������ظ�</BR>
	 * �ռ临�Ӷ�1��ʱ�临�Ӷ�num*originalArr.length
	 */
	static int[] random(int num, int[] originalArr, int[] rates, int radix) {
		if (originalArr.length <= num) {
			return originalArr;
		}
		Random random = new Random();
		int[] targetArr = new int[num];
		int limit = originalArr.length - 1;
		for (int i = 0; i < num; i++) {
			int tarRate = random.nextInt(radix);
			for (int j = 0; j <= limit; j++) {
				if (rates[j] >= tarRate) {
					targetArr[i] = originalArr[j];
					radix -= rates[j];
					// �²�limitû�б����У���jλ�ý�����ʡ����j����limit�������Ѵ�ѡ����-1
					originalArr[j] = originalArr[limit];
					rates[j] = rates[limit];
					limit--;
					break;
				}
				tarRate -= rates[j];
			}
		}
		return targetArr;
	}

	public static void main(String[] args) {
		int num = 3;
		int[] originalArr = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		int[] rates = new int[] { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10 };
		int radix = 100;
		int[] tarArr = random(num, originalArr, rates, radix);
		for (int target : tarArr) {
			System.out.println(target);
		}
	}
}
