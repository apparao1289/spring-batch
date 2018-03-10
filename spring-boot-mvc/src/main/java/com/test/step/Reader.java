package com.test.step;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class Reader implements ItemReader<String> {

	private String[] messages = { "Wlcome to spring boot example",
			"Welcome to Spring Batch Example",
			"We are using H2 Database for this example" };

	private int count = 0;

	public String read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {

		if (count < messages.length) {
			return messages[count++];
		} else {
			count = 0;
		}
		return null;
	}

}