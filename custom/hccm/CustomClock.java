package hccm;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.jaamsim.Graphics.OverlayClock;

import com.jaamsim.input.Keyword;
import com.jaamsim.input.StringInput;

/**
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 */
public class CustomClock extends OverlayClock {
	/**
	 * startMonth
	 * startDay
	 * startTime
	 * 
	 */
	@Keyword(description = "Month the clock starts.",
			 exampleList = {"Jun", "Jul", "Sep"})
	private final StringInput startMonth;
	
	@Keyword(description = "Day the clock starts.",
			 exampleList = {"1", "21"})
	private final StringInput startDay;

	@Keyword(description = "Time (24 hr clock) the clock starts.",
			 exampleList = {"00:12", "13:57"})
	private final StringInput startTime;
	
	private Calendar calendar;
	private SimpleDateFormat dateFormat;

	{					
		startMonth = new StringInput("StartingMonth", KEY_INPUTS, null);
		startMonth.setRequired(true);
		this.addInput(startMonth);

		startDay = new StringInput("StartingDay", KEY_INPUTS, null);
		startDay.setRequired(true);
		this.addInput(startDay);

		startTime = new StringInput("StartingTime", KEY_INPUTS, null);
		startTime.setRequired(true);
		this.addInput(startTime);
	}

	/**
	 * Sets up the calendar and date format based on the specific time zone
	 */
	public CustomClock() {
		calendar = Calendar.getInstance();
		calendar.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
		dateFormat = new SimpleDateFormat(dateFormatInput.getValue());
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Overrides the OverlayClock() function and renders the date and time text as a string with the specific date and time formats
	 * 
	 * @param simTime, the current time of the simulation
	 * @return String, the rendered text (or the failed text if caught exception)
	 */
	@Override
	public String getRenderText(double simTime) {

		double time = simTime; // Time in s

		String format = (String)super.getInput("DateFormat").getValue();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		LocalDateTime sdt = LocalDateTime.parse(super.getRenderText(0.0), formatter); // Get the starting time for the overlay clock
		int year = sdt.getYear();
		
		String yStr = Integer.toString(year), mStr = startMonth.getValue(), dStr = startDay.getValue(), tStr = startTime.getValue();
		if ( (mStr != null) && (mStr.length() == 1) ) mStr = "0" + mStr;
		if ( (dStr != null) && (dStr.length() == 1) ) dStr = "0" + dStr;		
		String dtStr = yStr + "-" + mStr + "-" + dStr + " " + tStr;
		DateTimeFormatter iformatter = DateTimeFormatter.ofPattern("yyyy-MMM-d H:mm");
		LocalDateTime dt;
		try {
		  dt = LocalDateTime.parse(dtStr, iformatter); // Get the starting time for the overlay clock
		  long nanos = (long)(1e9 * time);
		  dt = dt.plusNanos(nanos);
		  
		  ZonedDateTime zdt = dt.atZone(ZoneId.of("GMT"));
		  
		  calendar.setTime(Date.from(zdt.toInstant()));
		  return dateFormat.format(calendar.getTime());
		} catch (Exception e) // catch all exceptions and return a custom error string
		{
			System.out.println("Error parsing CustomClock string = " + dtStr);
			return this.failText.getValue();
		}

	}

}
