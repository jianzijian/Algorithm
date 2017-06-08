package leetcode;

class LT_24_SwapNodesInPairs {

	public class ListNode {
		int val;
		ListNode next;

		ListNode(int x) {
			val = x;
		}
	}

	public ListNode swapPairs(ListNode head) {
		ListNode tmpHead = head;
		while (tmpHead != null && tmpHead.next != null) {
			int tmpVal = tmpHead.val;
			tmpHead.val = tmpHead.next.val;
			tmpHead.next.val = tmpVal;
			tmpHead = tmpHead.next.next;
		}
		return head;
	}

	public ListNode swapPairsRecursive(ListNode head) {
		if (head == null || head.next == null) {
			return head;
		}
		int tmpVal = head.val;
		head.val = head.next.val;
		head.next.val = tmpVal;
		this.swapPairsRecursive(head.next.next);
		return head;
	}

	public LT_24_SwapNodesInPairs() {
		ListNode head = new ListNode(1);
		ListNode tmpHead = head;
		for (int i = 2; i <= 4; i++) {
			tmpHead = tmpHead.next = new ListNode(i);
		}
		this.swapPairsRecursive(head);
		while (head != null) {
			System.out.println(head.val);
			head = head.next;
		}
	}

	public static void main(String[] args) {
		new LT_24_SwapNodesInPairs();
	}

}
