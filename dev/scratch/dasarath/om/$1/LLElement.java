/*
 * Created on Dec 30, 2004
 *
 */

/**
 * @author Dasarath
 *
 * @date Dec 30, 2004
 */
public class LLElement {
	LLElement nextSib, prevSib, firstChild;

	public LLElement() {
		firstChild= null;
		nextSib= null;
		prevSib= null;
	}

	public void addChild(LLElement child) {
		child.prevSib= null;		
		if (firstChild == null)
			child.nextSib= null;			
		else {		
			child.nextSib= firstChild;
			firstChild.prevSib= child;
		}
		firstChild= child;
	}
	
	public LLElement traverse(){
		LLElement e= firstChild;
		while (e.nextSib != null)
			e= e.nextSib;
		return e;		
	}
}
