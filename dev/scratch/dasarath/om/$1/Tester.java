/*
 * Created on Dec 30, 2004
 *
 */

/**
 * @author Dasarath
 *
 * @date Dec 30, 2004
 */
public class Tester {
	public static final long N= 200;
	public static final long M= 10000;
	public static final long T= 100;
	void test1() throws Exception {
		for (int i= 0; i < N; i++) {
			LLElement e= new LLElement();
			for (int j= 0; j < M; j++)
				e.addChild(new LLElement());
			for (int k= 0; k < T; k++)
				e.traverse();			
		}
	}

	void test2() throws Exception {
		for (int i= 0; i < N; i++) {
			ALElement e= new ALElement();
			for (int j= 0; j < M; j++)
				e.addChild(new ALElement());
			for (int k= 0; k < T; k++)
				e.traverse();
		}
	}

	public static void main(String[] args) {
		try {
			long t1= System.currentTimeMillis();
			new Tester().test2();
			long t2= System.currentTimeMillis();
			System.out.println((t2 - t1)/N);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
