package org.processmining.tests.framework;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.processmining.framework.util.collection.HashMultiSet;
import org.processmining.framework.util.collection.LinkedHashMultiSet;
import org.processmining.framework.util.collection.TreeMultiSet;

public class FrameworkUtilCollectionsTest {

	/*
	 * 
	 *  HashMultiSet
	 *  
	 */
	@Test
	public void test_HashMultiSet() {
		
		// create two identical multisets
		HashMultiSet<Integer> hms1 = new HashMultiSet<Integer>();
		hms1.add(1);
		hms1.add(2, 2);
		hms1.add(3, 3);
		
		HashMultiSet<Integer> hms2 = new HashMultiSet<Integer>();
		hms2.add(1);
		hms2.add(2);
		hms2.add(3);
		hms2.add(2);
		hms2.add(3);
		hms2.add(3);

		// test for equality
		Assert.assertEquals("Equality1: "+hms1+" equals "+hms2, hms1, hms2);

		// remove some elments
		hms1.remove(3);
		hms1.remove(3);
		hms2.remove(3);
		hms2.remove(3);

		// test for equality
		Assert.assertEquals("Equality2: "+hms1+" equals "+hms2, hms1, hms2);

		// test constructors
		Integer hms2_arr [] = hms2.toArray(new Integer[hms2.size()]);
		HashMultiSet<Integer> hms3 = new HashMultiSet<Integer>(hms2_arr);
		Assert.assertEquals("Equality3: "+hms1+" equals "+hms3, hms1, hms3);
		
		List<Integer> hms2_list = hms2.toList();
		HashMultiSet<Integer> hms4 = new HashMultiSet<Integer>(hms2_list);
		Assert.assertEquals("Equality4: "+hms1+" equals "+hms4, hms1, hms4);
		
		hms3.add(17, 17);
		hms3.removeAll(hms2);
		Assert.assertTrue("Member 1: "+hms3+"(17) == 17", hms3.occurrences(17) == 17);
		Assert.assertTrue("Member 2: "+hms3.baseSet()+" has one member", hms3.baseSet().size() == 1);
		
		HashMultiSet<Integer> hms5 = new HashMultiSet<Integer>(); 
		hms5.add(17, 0);
		Assert.assertFalse("Member 3: "+hms5+" does not contain 17", hms5.contains(17));
		Assert.assertTrue("Member 4: "+hms5+" is empty", hms5.isEmpty());
	}
	
	@Test
	public void test_HashMultiSet_contains() {

		// create two multisets
		HashMultiSet<Integer> hms1 = new HashMultiSet<Integer>();
		hms1.add(1, 12);
		hms1.add(2, 3);

		HashMultiSet<Integer> hms2 = new HashMultiSet<Integer>(hms1);
		hms2.add(3, 1);
		
		Assert.assertTrue(hms2+" contains all "+hms1, hms2.containsAll(hms1));
		
		hms2.remove(3);
		
		Assert.assertTrue(hms2+" contains all "+hms1, hms2.containsAll(hms1));

		hms2.remove(2);

		Assert.assertFalse(hms2+" does not contain all "+hms1, hms2.containsAll(hms1));
	}
	
	@Test()
	public void test_HashMultiSet_retainAll() {
		
		// create two identical multisets
		HashMultiSet<Integer> hms1 = new HashMultiSet<Integer>();
		hms1.add(1);
		hms1.add(2, 2);
		hms1.add(3, 3);

		HashMultiSet<Integer> hms2 = new HashMultiSet<Integer>();
		hms2.add(3, 1);
		hms2.add(4, 1);
		
		hms1.retainAll(hms2);
		Assert.assertEquals(hms2.toString(), "[(3,1) (4,1)]");
		Assert.assertEquals(hms1.toString(), "[(3,1)]");
	}
	
	@Test
	public void test_HashMultiSet_addAll_Collection() {
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.add(1);
		list.add(1);
		list.add(2);
		list.add(1);
		list.add(2);
		
		HashMultiSet<Integer> set = new HashMultiSet<Integer>(list);
		Assert.assertTrue(set+" should have 3x1 and 2x2", set.occurrences(1) == 3 && set.occurrences(2) == 2);
	}
	
	@Test
	public void test_HashMultiSet_removeAll_Collection() {
		
		HashMultiSet<Integer> set1 = new HashMultiSet<Integer>();
		set1.add(1,3);
		set1.add(2,2);
		
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.add(2);
		list.add(1);
		list.add(2);
		list.add(3);
		
		set1.removeAll(list);
		Assert.assertTrue(set1+" should have 2x1, no 2 and no 3", set1.occurrences(1) == 2 && set1.occurrences(2) == 0 && !set1.contains(3));
	}
	
	/*
	 * 
	 *  LinkedHashMultiSet
	 *  
	 */

	@Test
	public void test_LinkedHashMultiSet() {
		
		// create two identical multisets
		LinkedHashMultiSet<Integer> hms1 = new LinkedHashMultiSet<Integer>();
		hms1.add(1);
		hms1.add(2, 2);
		hms1.add(3, 3);
		
		LinkedHashMultiSet<Integer> hms2 = new LinkedHashMultiSet<Integer>();
		hms2.add(1);
		hms2.add(2);
		hms2.add(3);
		hms2.add(2);
		hms2.add(3);
		hms2.add(3);

		// test for equality
		Assert.assertEquals("Equality1: "+hms1+" equals "+hms2, hms1, hms2);

		// remove some elments
		hms1.remove(3);
		hms1.remove(3);
		hms2.remove(3);
		hms2.remove(3);

		// test for equality
		Assert.assertEquals("Equality2: "+hms1+" equals "+hms2, hms1, hms2);

		// test constructors
		Integer hms2_arr [] = hms2.toArray(new Integer[hms2.size()]);
		LinkedHashMultiSet<Integer> hms3 = new LinkedHashMultiSet<Integer>(hms2_arr);
		Assert.assertEquals("Equality3: "+hms1+" equals "+hms3, hms1, hms3);
		
		List<Integer> hms2_list = hms2.toList();
		LinkedHashMultiSet<Integer> hms4 = new LinkedHashMultiSet<Integer>(hms2_list);
		Assert.assertEquals("Equality4: "+hms1+" equals "+hms4, hms1, hms4);
		
		hms3.add(17, 17);
		hms3.removeAll(hms2);
		Assert.assertTrue("Member 1: "+hms3+"(17) == 17", hms3.occurrences(17) == 17);
		Assert.assertTrue("Member 2: "+hms3.baseSet()+" has one member", hms3.baseSet().size() == 1);
		
		LinkedHashMultiSet<Integer> hms5 = new LinkedHashMultiSet<Integer>(); 
		hms5.add(17, 0);
		Assert.assertFalse("Member 3: "+hms5+" does not contain 17", hms5.contains(17));
		Assert.assertTrue("Member 4: "+hms5+" is empty", hms5.isEmpty());
	}
	
	@Test
	public void test_LinkedHashMultiSet_contains() {

		// create two multisets
		LinkedHashMultiSet<Integer> hms1 = new LinkedHashMultiSet<Integer>();
		hms1.add(1, 12);
		hms1.add(2, 3);

		LinkedHashMultiSet<Integer> hms2 = new LinkedHashMultiSet<Integer>(hms1);
		hms2.add(3, 1);
		
		Assert.assertTrue(hms2+" contains all "+hms1, hms2.containsAll(hms1));
		
		hms2.remove(3);
		
		Assert.assertTrue(hms2+" contains all "+hms1, hms2.containsAll(hms1));

		hms2.remove(2);

		Assert.assertFalse(hms2+" does not contain all "+hms1, hms2.containsAll(hms1));
	}
	
	@Test
	public void test_LinkedHashMultiSet_retainAll() {
		
		// create two identical multisets
		LinkedHashMultiSet<Integer> hms1 = new LinkedHashMultiSet<Integer>();
		hms1.add(1);
		hms1.add(2, 2);
		hms1.add(3, 3);

		LinkedHashMultiSet<Integer> hms2 = new LinkedHashMultiSet<Integer>();
		hms2.add(3, 1);
		hms2.add(4, 1);
		
		hms1.retainAll(hms2);
		Assert.assertEquals(hms2.toString(), "[(3,1) (4,1)]");
		Assert.assertEquals(hms1.toString(), "[(3,1)]");
	}
	
	@Test
	public void test_LinkedHashMultiSet_addAll_Collection() {
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.add(1);
		list.add(1);
		list.add(2);
		list.add(1);
		list.add(2);
		
		LinkedHashMultiSet<Integer> set = new LinkedHashMultiSet<Integer>(list);
		Assert.assertTrue(set+" should have 3x1 and 2x2", set.occurrences(1) == 3 && set.occurrences(2) == 2);
	}
	
	@Test
	public void test_LinkedHashMultiSet_removeAll_Collection() {
		
		LinkedHashMultiSet<Integer> set1 = new LinkedHashMultiSet<Integer>();
		set1.add(1,3);
		set1.add(2,2);
		
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.add(2);
		list.add(1);
		list.add(2);
		list.add(3);
		
		set1.removeAll(list);
		Assert.assertTrue(set1+" should have 2x1, no 2 and no 3", set1.occurrences(1) == 2 && set1.occurrences(2) == 0 && !set1.contains(3));
	}
	



	/*
	 * 
	 *  TreeMultiSet
	 *  
	 */

	@Test
	public void test_TreeMultiSet() {
		
		// create two identical multisets
		TreeMultiSet<Integer> tms1 = new TreeMultiSet<Integer>();
		tms1.add(1);
		tms1.add(2, 2);
		tms1.add(3, 3);
		
		TreeMultiSet<Integer> tms2 = new TreeMultiSet<Integer>();
		tms2.add(1);
		tms2.add(2);
		tms2.add(3);
		tms2.add(2);
		tms2.add(3);
		tms2.add(3);

		// test for equality
		Assert.assertEquals("Equality1: "+tms1+" equals "+tms2, tms1, tms2);

		// remove some elments
		tms1.remove(3);
		tms1.remove(3);
		tms2.remove(3);
		tms2.remove(3);

		// test for equality
		Assert.assertEquals("Equality2: "+tms1+" equals "+tms2, tms1, tms2);

		// test constructors
		Integer hms2_arr [] = tms2.toArray(new Integer[tms2.size()]);
		TreeMultiSet<Integer> tms3 = new TreeMultiSet<Integer>(hms2_arr);
		Assert.assertEquals("Equality3: "+tms1+" equals "+tms3, tms1, tms3);
		
		List<Integer> hms2_list = tms2.toList();
		TreeMultiSet<Integer> hms4 = new TreeMultiSet<Integer>(hms2_list);
		Assert.assertEquals("Equality4: "+tms1+" equals "+hms4, tms1, hms4);
		
		tms3.add(17, 17);
		tms3.removeAll(tms2);
		Assert.assertTrue("Member 1: "+tms3+"(17) == 17", tms3.occurrences(17) == 17);
		Assert.assertTrue("Member 2: "+tms3.baseSet()+" has one member", tms3.baseSet().size() == 1);
		
		TreeMultiSet<Integer> tms4 = new TreeMultiSet<Integer>(); 
		tms4.add(17, 0);
		Assert.assertFalse("Member 3: "+tms4+" does not contain 17", tms4.contains(17));
		Assert.assertTrue("Member 4: "+tms4+" is empty", tms4.isEmpty());
	}
	
	@Test
	public void test_TreeMultiSet_contains() {

		// create two multisets
		TreeMultiSet<Integer> tms1 = new TreeMultiSet<Integer>();
		tms1.add(1, 12);
		tms1.add(2, 3);

		TreeMultiSet<Integer> tms2 = new TreeMultiSet<Integer>(tms1);
		tms2.add(3, 1);
		
		Assert.assertTrue(tms2+" contains all "+tms1, tms2.containsAll(tms1));
		
		tms2.remove(3);
		
		Assert.assertTrue(tms2+" contains all "+tms1, tms2.containsAll(tms1));

		tms2.remove(2);

		Assert.assertFalse(tms2+" does not contain all "+tms1, tms2.containsAll(tms1));
	}
	
	@Test
	public void test_TreeMultiSet_retainAll() {
		
		// create two identical multisets
		TreeMultiSet<Integer> tms1 = new TreeMultiSet<Integer>();
		tms1.add(1);
		tms1.add(2, 2);
		tms1.add(3, 3);

		TreeMultiSet<Integer> tms2 = new TreeMultiSet<Integer>();
		tms2.add(3, 1);
		tms2.add(4, 1);
		
		tms1.retainAll(tms2);
		Assert.assertEquals(tms2.toString(), "[(3,1) (4,1)]");
		Assert.assertEquals(tms1.toString(), "[(3,1)]");
	}

	@Test
	public void test_TreeMultiSet_addAll_Collection() {
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.add(1);
		list.add(1);
		list.add(2);
		list.add(1);
		list.add(2);
		
		TreeMultiSet<Integer> set = new TreeMultiSet<Integer>(list);
		Assert.assertTrue(set+" should have 3x1 and 2x2", set.occurrences(1) == 3 && set.occurrences(2) == 2);
	}

	@Test
	public void test_TreeMultiSet_removeAll_Collection() {
		
		TreeMultiSet<Integer> set1 = new TreeMultiSet<Integer>();
		set1.add(1,3);
		set1.add(2,2);
		
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.add(2);
		list.add(1);
		list.add(2);
		list.add(3);
		
		set1.removeAll(list);
		Assert.assertTrue(set1+" should have 2x1, no 2 and no 3", set1.occurrences(1) == 2 && set1.occurrences(2) == 0 && !set1.contains(3));
	}

	
	
	/*
	 * 
	 *  MultiSet Iterator
	 *  
	 */
	@Test
	public void test_MultiSetIterator() {

		TreeMultiSet<Integer> hms2 = new TreeMultiSet<Integer>();
		hms2.add(1);
		hms2.add(2);
		hms2.add(3);
		hms2.add(2);
		hms2.add(3);
		hms2.add(3);

		String actual = "";
		Iterator<Integer> it = hms2.iterator();
		while (it.hasNext()) {
			Integer i = it.next(); 
			actual += i.toString()+",";
		}
		
		String expected = "1,2,2,3,3,3,";
		Assert.assertEquals(expected+" equals "+actual, expected, actual);
		
		it = hms2.iterator();
		while (it.hasNext()) {
			it.next(); 
			it.remove();
		}
		
		Assert.assertTrue(hms2+" is empty", hms2.isEmpty());

	}

}
