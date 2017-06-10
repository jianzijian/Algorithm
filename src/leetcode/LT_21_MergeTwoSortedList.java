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
		ListNode newList = new ListNode(-1); // 头部节点
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
		// 之后就不用循环了，直接拼接剩余的节点
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
			// 方法传参相当于新建了一个引用变量指向了l1.next
			l1.next = mergeTwoListsRecursive(l1.next, l2);
			return l1;
		}
		l2.next = mergeTwoListsRecursive(l1, l2.next);
		return l2;
	}

}
