import java.util.ArrayList;
import java.util.Iterator;

/*
 * Created on Dec 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author Dasarath
 *
 * @date Dec 30, 2004
 */
public class ALElement {
	ArrayList al;
	public static final int S= 100;
	
	public ALElement() {
		al= new ArrayList(S);
	}

	public void addChild(ALElement child) {
		al.add(child);		
	}
	
	public ALElement traverse(){
		ALElement e= null;
		
		Iterator iter= al.iterator();
		while (iter.hasNext())
			e= (ALElement)iter.next();

/*		int n= al.size();
		for (int i= 0; i < n; i++)
			e= (ALElement)al.get(i);
*/		return e;
	}
}
