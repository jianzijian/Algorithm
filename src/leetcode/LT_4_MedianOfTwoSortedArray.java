package leetcode;

class LT_4_MedianOfTwoSortedArray {

	public double findMedianSortedArrays(int[] nums1, int[] nums2) {
		int[] array = new int[nums1.length + nums2.length];
		int n1Index = 0, n2Index = 0;
		while (n1Index < nums1.length && n2Index < nums2.length) {
			if (nums1[n1Index] <= nums2[n2Index]) {
				array[n1Index + n2Index] = nums1[n1Index++];
			} else {
				array[n1Index + n2Index] = nums2[n2Index++];
			}
		}
		while (n1Index < nums1.length) {
			array[n1Index + n2Index] = nums1[n1Index++];
		}
		while (n2Index < nums2.length) {
			array[n1Index + n2Index] = nums2[n2Index++];
		}
		if (array.length % 2 == 0) {
			int middle = array.length / 2;
			return ((double) array[middle - 1] + array[middle]) / 2;
		} else {
			return array[array.length / 2];
		}
	}

	/**
	 * median,中位数，把数组切分成长度相等的两半，且有max(left)<=min(right)</BR>
	 * 假如从i，j位置切分数据nums1，nums2，并命名nums1[0~i-1]+nums2[0~j-1]==left,nums1[i~m]+
	 * nums2[j~n]==right</BR>
	 * 确保len(left)==len(right),且max(left)<=min(right)，即可确认left、
	 * right即为nums1和nums2被中位数切割的两部分</BR>
	 * 设m<=n，则有公式：i+j=m-i+n-j+1,i=0~m,j=(m+n+1)/2-i，条件：nums1[i-1]<=nums2[j],
	 * nums2[ j-1]<=nums2[i]</BR>
	 * 其中i有临界值0跟m，导致了j有临界值n跟0，i=0可以认为nums1恒大于nums2，i=m则相反，nums1恒小于nums2
	 */
	public double findMedianSortedArraysSpecial(int[] nums1, int[] nums2) {
		int[] A = nums1, B = nums2;
		int m = A.length, n = B.length;
		if (m > n) {
			A = nums2;
			B = nums1;
			m = A.length;
			n = B.length;
		}
		if (n == 0) {
			return 0;
		}
		int imin = 0, imax = m, halfLength = (m + n + 1) / 2;
		while (imin <= imax) {
			int i = (imin + imax) / 2;
			int j = halfLength - i;
			if (i > 0 && A[i - 1] > B[j]) {
				// i is too big
				imax = i - 1;
			} else if (i < m && B[j - 1] > A[i]) {
				// i is too small
				imin = i + 1;
			} else {
				int leftMax = 0, rightMin = 0;
				if (i == 0) {
					leftMax = B[j - 1];
				} else if (j == 0) {
					leftMax = A[i - 1];
				} else {
					leftMax = Math.max(B[j - 1], A[i - 1]);
				}
				if ((m + n) % 2 == 1) {
					return leftMax;
				}
				if (i == m) {
					rightMin = B[j];
				} else if (j == n) {
					rightMin = A[i];
				} else {
					rightMin = Math.min(B[j], A[i]);
				}
				return (rightMin + leftMax) / 2.0;
			}
		}
		return -1;
	}

	public LT_4_MedianOfTwoSortedArray() {
		int[] nums1 = new int[] { 1, 3 };
		int[] nums2 = new int[] { 2, 4 };
		System.out.println(this.findMedianSortedArraysSpecial(nums1, nums2));
	}

	public static void main(String[] args) {
		new LT_4_MedianOfTwoSortedArray();
	}

}
