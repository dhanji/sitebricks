package com.google.sitebricks.example;

import com.google.sitebricks.rendering.Decorated;

@Decorated
public class DecoratedPage extends DecoratorPage {
	
	@Override
	public String getWorld() {
		return "This comes from the subclass";
	}
	
	public String getDescription() {
		return "very cool";
	}
}
