package leetcode;

class LT_23_MergeKSortedList {

	public class ListNode {
		int val;
		ListNode next;

		ListNode(int x) {
			val = x;
		}
	}

	// �ظ�һ��һ����merge�ᵼ��newListԽ��Խ����ÿ��merge�ڵ�Ҳ��Խ�ࣨ�ظ�����Ľڵ�̫�ࣩ
	// public ListNode mergeKLists(ListNode[] lists) {
	// if (lists == null || lists.length <= 0) {
	// return null;
	// }
	// ListNode newList = lists[0];
	// for (int i = 1; i < lists.length; i++) {
	// newList = mergeTwoLists(newList, lists[i]);
	// }
	// return newList;
	// }

	public ListNode mergeKLists(ListNode[] lists) {
		if (lists == null || lists.length <= 0) {
			return null;
		}
		// �����沢
		while (lists.length != 1) {
			ListNode[] tmpLists = new ListNode[(int) Math.ceil((double) lists.length / 2)];
			for (int i = 0; i < lists.length; i += 2) {
				int tmpIndex = i / 2;
				if (i == lists.length - 1) {
					tmpLists[tmpIndex] = lists[i];
					break;
				}
				tmpLists[tmpIndex] = this.mergeTwoLists(lists[i], lists[i + 1]);
			}
			lists = tmpLists;
		}
		return lists[0];
	}

	private ListNode mergeTwoLists(ListNode l1, ListNode l2) {
		if (l1 == null) {
			return l2;
		}
		if (l2 == null) {
			return l1;
		}
		if (l1.val < l2.val) {
			// ���������൱���½���һ�����ñ���ָ����l1.next
			l1.next = mergeTwoLists(l1.next, l2);
			return l1;
		}
		l2.next = mergeTwoLists(l1, l2.next);
		return l2;
	}

}
