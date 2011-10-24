package ar.edu.itba.it.pdc.config;

import org.joda.time.DateTime;


public class TimeRange {
	private DateTime from;
	private DateTime to;
	
	public TimeRange(int fromH, int fromM, int fromS, int toH, int toM, int toS){
		DateTime now = DateTime.now();
		this.from = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), fromH, fromM, fromS, 0);
		this.to = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), toH, toM, toS, 0);
	}
	
	public boolean isInRange(DateTime date){
		DateTime d = new DateTime(to.getYear(), to.getMonthOfYear(), to.getDayOfMonth(), date.getHourOfDay(), date.getMinuteOfHour(), date.getSecondOfMinute(), 0);
		
		if (this.from.isAfter(this.to))
			return d.isAfter(this.from) || d.isBefore(this.to);
		else
			return d.isAfter(this.from) && d.isBefore(this.to);	
	}
}
