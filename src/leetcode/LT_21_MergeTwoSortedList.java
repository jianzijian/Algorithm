package leetcode;

class LT_21_MergeTwoSortedList {

	public class ListNode {
		int val;
		ListNode next;

		ListNode(int x) {
			val = x;
		}
	}

	public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
		ListNode newList = new ListNode(-1); // ͷ���ڵ�
		ListNode tmpList = newList;
		while (l1 != null && l2 != null) {
			if (l1.val < l2.val) {
				tmpList = tmpList.next = l1;
				l1 = l1.next;
				continue;
			}
			tmpList = tmpList.next = l2;
			l2 = l2.next;
		}
		// ֮��Ͳ���ѭ���ˣ�ֱ��ƴ��ʣ��Ľڵ�
		if (l1 != null) {
			tmpList.next = l1;
		}
		if (l2 != null) {
			tmpList.next = l2;
		}
		return newList.next;
	}

	public ListNode mergeTwoListsRecursive(ListNode l1, ListNode l2) {
		if (l1 == null) {
			return l2;
		}
		if (l2 == null) {
			return l1;
		}
		if (l1.val < l2.val) {
			// ���������൱���½���һ�����ñ���ָ����l1.next
			l1.next = mergeTwoListsRecursive(l1.next, l2);
			return l1;
		}
		l2.next = mergeTwoListsRecursive(l1, l2.next);
		return l2;
	}

}
