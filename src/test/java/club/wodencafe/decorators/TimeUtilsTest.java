package club.wodencafe.decorators;

import static org.junit.Assert.assertEquals;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TimeUtilsTest
{
	@Test
	public void test()
	{
		assertEquals(ChronoUnit.DAYS, TimeUtils.getChronoUnit(TimeUnit.DAYS));
		assertEquals(ChronoUnit.HOURS, TimeUtils.getChronoUnit(TimeUnit.HOURS));
		assertEquals(ChronoUnit.MINUTES, TimeUtils.getChronoUnit(TimeUnit.MINUTES));
		assertEquals(ChronoUnit.SECONDS, TimeUtils.getChronoUnit(TimeUnit.SECONDS));
		assertEquals(ChronoUnit.MILLIS, TimeUtils.getChronoUnit(TimeUnit.MILLISECONDS));
		assertEquals(ChronoUnit.MICROS, TimeUtils.getChronoUnit(TimeUnit.MICROSECONDS));
		assertEquals(ChronoUnit.NANOS, TimeUtils.getChronoUnit(TimeUnit.NANOSECONDS));
		assertEquals(TimeUnit.DAYS, TimeUtils.getTimeUnit(ChronoUnit.DAYS));
		assertEquals(TimeUnit.HOURS, TimeUtils.getTimeUnit(ChronoUnit.HOURS));
		assertEquals(TimeUnit.MINUTES, TimeUtils.getTimeUnit(ChronoUnit.MINUTES));
		assertEquals(TimeUnit.SECONDS, TimeUtils.getTimeUnit(ChronoUnit.SECONDS));
		assertEquals(TimeUnit.MILLISECONDS, TimeUtils.getTimeUnit(ChronoUnit.MILLIS));
		assertEquals(TimeUnit.MICROSECONDS, TimeUtils.getTimeUnit(ChronoUnit.MICROS));
		assertEquals(TimeUnit.NANOSECONDS, TimeUtils.getTimeUnit(ChronoUnit.NANOS));
	}
}
