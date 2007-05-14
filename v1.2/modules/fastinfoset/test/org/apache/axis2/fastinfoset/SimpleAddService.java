package org.apache.axis2.fastinfoset;

public class SimpleAddService {
	
	public int addInts(int val1, int val2) {
		System.out.println("Received " + val1 + " & " + val2);
		return val1 + val2;
	}

	public float addFloats(float val1, float val2) {
		System.out.println("Received " + val1 + " & " + val2);
		return val1 + val2;
	}
	
	public String addStrings(String val1, String val2) {
		System.out.println("Received " + val1 + " & " + val2);
		return val1 + val2;
	}
}
