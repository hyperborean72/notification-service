package com.cis.sys101_notifications.support;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class TestDateFormatter {
	public static void main(String[] args) {

		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(new Locale("ru"));

		String eventDtoDateTime = LocalDateTime.now().toString();

		LocalDateTime localDateTime = LocalDateTime.parse(eventDtoDateTime);

		String parsedDate =  formatter.format(localDateTime);

		System.out.println(parsedDate);
	}
}