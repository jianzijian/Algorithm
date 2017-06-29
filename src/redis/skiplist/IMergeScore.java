package redis.skiplist;

public interface IMergeScore<S> extends Comparable<S> {

	public S merge(S original);

}
