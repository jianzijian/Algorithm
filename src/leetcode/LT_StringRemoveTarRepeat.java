package leetcode;

/**
 * 面试题，去重特定的重复字符+去重被其他字符包围的特殊字符
 * 
 * @author jianzijian
 *
 */
class LT_StringRemoveTarRepeat {

	public String remove(String str, char tarChar) {
		if (str == null || str.equals("") || str.length() == 1) {
			return str;
		}
		char[] array = str.toCharArray();
		StringBuilder builder = new StringBuilder(array[0]);
		for (int i = 1; i < array.length; i++) {
			if (array[i] == array[i - 1] && array[i] == tarChar) {
				continue;
			}
			builder.append(array[i]);
		}
		return builder.toString();
	}

	/**
	 * 去重被其他字符包围的重复字符
	 */
	public String removeBetween(String str, char tarChar) {
		if (str == null || str.equals("") || str.length() == 1) {
			return str;
		}
		char[] array = str.toCharArray();
		StringBuilder builder = new StringBuilder();
		int begin = 0, end = array.length - 1;
		for (; begin <= end; begin++) {
			builder.append(array[begin]);
			if (array[begin] != tarChar) {
				break;
			}
		}
		StringBuilder tailBuilder = new StringBuilder();
		for (; end > begin; end--) {
			tailBuilder.append(array[end]);
			if (array[end] != tarChar) {
				break;
			}
		}
		for (int index = begin + 1; index < end; index++) {
			if (array[index] == array[index - 1] && array[index] == tarChar) {
				continue;
			}
			builder.append(array[index]);
		}
		builder.append(tailBuilder.toString());
		return builder.toString();
	}

	public LT_StringRemoveTarRepeat() {
		String str = "__________a_________B";
		System.out.println(this.removeBetween(str, '_'));
	}

	public static void main(String[] args) {
		new LT_StringRemoveTarRepeat();
	}
}
