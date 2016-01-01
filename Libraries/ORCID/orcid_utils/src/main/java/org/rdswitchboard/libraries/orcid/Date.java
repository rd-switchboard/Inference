package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Date {
	private String year;
	private String month;
	private String day;

	public String getYear() {
		return year;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setYear(String year) {
		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setMonth(String month) {
		this.month = month;
	}

	public String getDay() {
		return day;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setDay(String day) {
		this.day = day;
	}
	
	public String getDate() {
		StringBuilder sb = new StringBuilder();
		
		if (null != year && !year.isEmpty()) 
			sb.append(year);
		if (null != month && !month.isEmpty()) {
			if (sb.length() > 0)
				sb.append("-");
			sb.append(month);
		}
		if (null != day && !day.isEmpty()) {
			if (sb.length() > 0)
				sb.append("-");
			sb.append(day);
		}	

		if (sb.length() > 0)
			return sb.toString();
		else
			return null;
	}

	@Override
	public String toString() {
		return "Date [year=" + year + ", month=" + month + ", day=" + day + "]";
	}	
}
