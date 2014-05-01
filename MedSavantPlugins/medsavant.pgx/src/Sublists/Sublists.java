package Sublists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Use to get all possible sublists of an input List.
 * @author rammar
 */
public class Sublists {
	
	/**
	 * Testing.
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		List test1= new LinkedList();
		test1.addAll(Arrays.asList(new Integer[] {1,2,3,4,5}));
		
		System.out.println("Test list: " + test1);
		System.out.println(sublists(test1, 1));
		System.out.println(sublists(test1, 2));		
		/* unnecessarily large depth, returns same as depth=3 */
		System.out.println(sublists(test1, 100));
		/* empty list */
		System.out.println(sublists(new LinkedList(), 1));
		
		/* testing a larger set from 0-49. */
		List test2= new LinkedList();
		for (int i= 0; i != 50; ++i)
			test2.add(new Integer(i));
		System.out.println("Test list 2: " + test2);
		System.out.println(sublists(test2, 3));
		
		//System.out.println(sublists(test2, 3));
	}
	
	
	/**
	 * Return a unique set of all possible sublists of List after removing at least 1 object and up
	 * to the number of objects specified by the depth parameter.
	 * So, if depth=1 and the list has 4 objects, all possible sublists of 3 objects
	 * will be returned. If depth=4 and the list.size()=4, all possibly sublists of
	 * 3 or 2 objects will be returned.
	 * @param input The input List
	 * @param depth The maximum number of objects to remove from each sublist
	 * @return unique List of all possible sublists after removing the amount of objects specified by depth
	 */
	public static Set sublists(List input, int depth) {
		Set output= new HashSet();
		if (depth > 0) {
			for (int i= 0; i != input.size(); ++i) {
				List sub= mySubList(input,0, i);
				sub.addAll(mySubList(input, i+1, input.size())); // addALL() not add()
				
				if (!sub.isEmpty())
					output.add(sub);
				
				Set recursiveSub= sublists(sub, depth-1);
				// Since there is no addAll for Set, iteratively add to Set
				for (Object e : recursiveSub)
					output.add(e);
			}
		}
		
		return output;
	}
	
	
	/**
	 * Returns a new List object that is a portion of the input list between the specified
	 * fromIndex, inclusive, and toIndex, exclusive.
	 * @param original the input List
	 * @param fromIndex low endpoint (inclusive) of this sublist
	 * @param toIndex high endpoint (exclusive) of this sublist
	 * @return a new List object of the input original
	 */
	public static List mySubList(List original, int fromIndex, int toIndex) {
		List sub= new ArrayList();
		for (int i= fromIndex; i != toIndex; ++i) {
			sub.add(original.get(i));
		}
				
		return sub;
	}

}