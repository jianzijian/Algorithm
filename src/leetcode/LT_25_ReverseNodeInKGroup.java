package leetcode;

class LT_25_ReverseNodeInKGroup {

	public class ListNode {
		int val;
		ListNode next;

		ListNode(int x) {
			val = x;
		}
	}

	public ListNode reverseKGroup(ListNode head, int k) {
		if (head == null || head.next == null) {
			return head;
		}
		int count = 0;
		ListNode newListHead = new ListNode(-1);
		ListNode tmpHead = newListHead, tmpTail = null;
		ListNode e = head, next = null;
		do {
			next = e.next;
			if (count == 0) {
				tmpTail = e;
			}
			e.next = tmpHead.next;
			tmpHead.next = e;
			count++;
			if (count % k == 0) {
				tmpHead = tmpTail;
				count = 0;
			}
		} while ((e = next) != null);
		if (count != 0) {
			ListNode tmpNodes = tmpHead.next.next;
			tmpHead.next.next = null;
			ListNode tmpNext = null;
			while (tmpNodes != null) {
				tmpNext = tmpNodes.next;
				tmpNodes.next = tmpHead.next;
				tmpHead.next = tmpNodes;
				tmpNodes = tmpNext;
			}
		}
		return newListHead.next;
	}

	public LT_25_ReverseNodeInKGroup() {
		ListNode head = new ListNode(1);
		ListNode tmpHead = head;
		for (int i = 2; i <= 8; i++) {
			tmpHead = tmpHead.next = new ListNode(i);
		}
		head = this.reverseKGroup(head, 3);
		while (head != null) {
			System.out.println(head.val);
			head = head.next;
		}
	}

	public static void main(String[] args) {
		new LT_25_ReverseNodeInKGroup();
	}
}
