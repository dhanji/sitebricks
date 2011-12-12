package com.google.sitebricks.example;

import java.util.Calendar;
import java.util.Date;

import com.google.sitebricks.At;

@At("/conversion")
public class Conversion {
	private Date date;
	private Calendar calendar;
	private String message;
	private Double dbl;
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Calendar getCalendar() {
		return calendar;
	}
	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public Double getDbl() {
		return dbl;
	}
	
	public void setDbl(Double dbl) {
		this.dbl = dbl;
	}
	
	

}
