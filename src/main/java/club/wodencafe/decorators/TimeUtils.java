/* 
 * BSD 3-Clause License (https://opensource.org/licenses/BSD-3-Clause)
 *
 * Copyright (c) 2018, Christopher Bryan Boyd <wodencafe@gmail.com> All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package club.wodencafe.decorators;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Convert to and from {@link java.time.temporal.ChronoUnit}
 * and {@link java.util.concurrent.TimeUnit}.
 * 
 * This functionality is provided by JDK 9, but this is for
 * backwards compatibility with JDK 8.
 * 
 * @author Christopher Bryan Boyd <wodencafe@gmail.com>
 * @since 0.3
 *
 */
public final class TimeUtils
{
	/**
	 * Convert a {@link java.util.concurrent.TimeUnit}
	 * to a {@link java.time.temporal.ChronoUnit}.
	 * 
	 * @param 	unit
	 * 			The {@link java.util.concurrent.TimeUnit} to convert.
	 * @return	The {@link java.time.temporal.ChronoUnit} equivalent.
	 * 
	 * @throws	UnsupportedOperationException
	 * 			Thrown if an an unsupported {@link java.util.concurrent.TimeUnit}
	 * 			is provided (Try to stick to <strong>days</strong> and smaller).
	 */
	public static final ChronoUnit getChronoUnit(TimeUnit unit)
	{
		if (Objects.isNull(unit))
			return null;
		switch (unit)
		{
			case DAYS :
				return ChronoUnit.DAYS;
			case HOURS :
				return ChronoUnit.HOURS;
			case MICROSECONDS :
				return ChronoUnit.MICROS;
			case MILLISECONDS :
				return ChronoUnit.MILLIS;
			case MINUTES :
				return ChronoUnit.MINUTES;
			case NANOSECONDS :
				return ChronoUnit.NANOS;
			case SECONDS :
				return ChronoUnit.SECONDS;
			default :
				throw new UnsupportedOperationException(String.format("Cannot convert %s to ChronoUnit.", unit.name()));
		}
	}
	
	/**
	 * Convert a {@link java.time.temporal.ChronoUnit}
	 * to a  {@link java.util.concurrent.TimeUnit}.
	 * 
	 * @param 	unit
	 * 			The {@link java.time.temporal.ChronoUnit} to convert.
	 * @return	The {@link java.util.concurrent.TimeUnit} equivalent.
	 * 
	 * @throws	UnsupportedOperationException
	 * 			Thrown if an an unsupported {@link java.time.temporal.ChronoUnit}
	 * 			is provided (unlikely).
	 */
	public static final TimeUnit getTimeUnit(ChronoUnit unit) throws UnsupportedOperationException
	{
		if (Objects.isNull(unit))
			return null;
		switch (unit)
		{
			case DAYS :
				return TimeUnit.DAYS;
			case HOURS :
				return TimeUnit.HOURS;
			case MICROS :
				return TimeUnit.MICROSECONDS;
			case MILLIS :
				return TimeUnit.MILLISECONDS;
			case MINUTES :
				return TimeUnit.MINUTES;
			case NANOS :
				return TimeUnit.NANOSECONDS;
			case SECONDS :
				return TimeUnit.SECONDS;
			default:
				throw new UnsupportedOperationException(String.format("Cannot convert %s to TimeUnit.", unit.name()));
		}
	}
}
