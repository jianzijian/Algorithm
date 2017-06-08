package leetcode;

class LT_2_AddTwoNumbers {

	public class ListNode {
		int val;
		ListNode next;

		ListNode(int x) {
			val = x;
		}
	}

	public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
		int carry = 0, remainder = 0, sum = 0;// 进位,余数,和
		ListNode head = new ListNode(0), node = head; // 分配一个头指针方便后续操作
		while (l1 != null || l2 != null || carry != 0) {
			sum = (l1 == null ? 0 : l1.val) + (l2 == null ? 0 : l2.val) + carry;
			remainder = sum % 10;
			carry = sum / 10;
			node = node.next = new ListNode(remainder);
			l1 = l1 == null ? null : l1.next;
			l2 = l2 == null ? null : l2.next;
		}
		return head.next;
	}
}
