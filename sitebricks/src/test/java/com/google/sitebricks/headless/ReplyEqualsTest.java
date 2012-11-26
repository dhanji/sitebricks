package com.google.sitebricks.headless;

import static org.junit.Assert.assertTrue;

import org.testng.annotations.Test;

public class ReplyEqualsTest {

	@Test
	public void forbidden() {
		assertTrue(Reply.saying().forbidden().equals(Reply.saying().forbidden()));
	}
	
}
