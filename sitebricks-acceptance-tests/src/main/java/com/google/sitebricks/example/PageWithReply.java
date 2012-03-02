package com.google.sitebricks.example;

import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.http.Get;

public class PageWithReply {

	@Get
	public Object get() {
		return Reply.saying().status(678);
		
	}
}
