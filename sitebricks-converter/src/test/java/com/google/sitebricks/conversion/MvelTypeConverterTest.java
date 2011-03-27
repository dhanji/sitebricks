package com.google.sitebricks.conversion;

import org.testng.annotations.Test;

import com.google.sitebricks.conversion.MvelTypeConverter;

public class MvelTypeConverterTest
{
	@Test 
	public void simpleConversions() {
		MvelTypeConverter converter = new MvelTypeConverter();
		assert converter.convert(45, String.class).equals("45");
	}
}
