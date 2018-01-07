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

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>FieldSyncer</h1> 
 * <strong>FieldSyncer</strong> will synchronize your objects
 * with reflection.
 * 
 * @author Christopher Bryan Boyd <wdodencafe@gmail.com>
 * @version 0.1
 * @since 2018-01-07
 *
 */
final class FieldSyncer
{

	// SLF4J Logger
	private static final Logger logger = LoggerFactory.getLogger(FieldSyncer.class);

	public static final <T> void setFields(T from, T to)
	{
		Objects.requireNonNull(from, "from field cannot be null.");
		Objects.requireNonNull(to, "to field cannot be null.");
		logger.trace("FieldSyncer.setFields([from] T %d, [to] T %d)", System.identityHashCode(from),
				System.identityHashCode(to));
		Stream.of(from.getClass().getDeclaredFields()).forEach(field ->
		{
			setFieldSafe(from, to, field);
		});
	}

	private static final <T> void setFieldSafe(T from, T to, Field field)
	{
		try
		{
			setField(from, to, field);

		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
	}

	private static final <T> void setField(T from, T to, Field field)
			throws NoSuchFieldException, IllegalAccessException
	{
		Field fieldFrom = from.getClass().getDeclaredField(field.getName());

		fieldFrom.setAccessible(true);
		Object value = fieldFrom.get(from);
		Field fieldTo = to.getClass().getDeclaredField(field.getName());
		fieldTo.setAccessible(true);
		fieldTo.set(to, value);
	}
}
