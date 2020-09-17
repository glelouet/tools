package fr.lelouet.tools.lambdaref;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class TestGC {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		WeakReference<Object[]> wr1;
		Consumer<Object> callwr1;
		WeakReference<Object[]> wr2;
		WeakReference<Object[]> wr3;
		SoftReference<Object[]> sr1;
		Consumer<Object> callsr1;
		SoftReference<Object[]> sr2;
		SoftReference<Object[]> sr3;
		{
			Object[] holdwr1 = new Object[1];
			wr1 = new WeakReference<>(holdwr1);
			callwr1 = o -> holdwr1[0] = o;
			Object[] holdwr2 = new Object[1];
			wr2 = new WeakReference<>(holdwr2);
			wr3 = new WeakReference<>(new Object[1]);
			Object[] holdsr1 = new Object[1];
			sr1 = new SoftReference<>(holdsr1);
			callsr1 = o -> holdsr1[0] = o;
			Object[] holdsr2 = new Object[1];
			sr2 = new SoftReference<>(holdsr2);
			sr3 = new SoftReference<>(new Object[1]);
		}
		GCManage.force();
		System.err.println("wr1= " + wr1.get() + " ref is in labmda");
		System.err.println("wr2= " + wr2.get() + " ref not in labmda");
		System.err.println("wr3= " + wr3.get() + " ref not named");
		System.err.println("sr1= " + sr1.get() + " ref is in labmda");
		System.err.println("sr2= " + sr2.get() + " ref not in labmda");
		System.err.println("sr3= " + sr3.get() + " ref not named");
	}

}
