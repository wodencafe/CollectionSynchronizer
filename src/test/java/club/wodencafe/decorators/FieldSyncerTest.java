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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import club.wodencafe.decorators.FieldSyncer;

/**
 * Test basic reflection functionality.
 * 
 * @author Christopher Bryan Boyd <wodencafe@gmail.com>
 * @since 0.1
 *
 */
public class FieldSyncerTest
{

	@Test
	public void test()
	{
		TestObject t1 = new TestObject("a", "b", "c", 1);
		TestObject t2 = new TestObject("d", "e", "f", 2);
		FieldSyncer.setFields(t1, t2);
		assertEquals(t1.getField1(), t2.getField1());
		assertEquals(t1.getField2(), t2.getField2());
		assertEquals(t1.getField3(), t2.getField3());
		assertEquals(t1.getField4(), t2.getField4());
	}

}

class TestObject
{

	private String field1;
	private String field2;
	private String field3;
	private Integer field4;

	public TestObject(String field1,
			String field2,
			String field3,
			Integer field4)
	{
		super();
		this.field1 = field1;
		this.field2 = field2;
		this.field3 = field3;
		this.field4 = field4;
	}

	public String getField1()
	{
		return field1;
	}

	public String getField2()
	{
		return field2;
	}

	public String getField3()
	{
		return field3;
	}

	public Integer getField4()
	{
		return field4;
	}
}
