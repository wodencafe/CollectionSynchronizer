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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.jayway.awaitility.Awaitility;

public class DecorationBuilderTest
{

	private boolean consumed = false;

	@Before
	public void reset()
	{
		consumed = false;
		PersonService.reset();
	}

	@Test
	public void test() throws IllegalArgumentException, Exception
	{
		Collection<Person> personList = PersonService.getPeople();
		assertTrue(personList.iterator().hasNext());
		Person p1 = personList.iterator().next();
		int originalIdentityHashCode = System.identityHashCode(p1);
		Person p2 = new Person();
		p2.setId(p1.getId());
		p2.setName(p1.getName());
		p2.setAge(p1.getAge() + 1);
		PersonService.savePerson(p2);
		PhantomReference<RunnableCloseable> weak = decorateList(personList);
		assertEquals(p1.getAge(), p2.getAge());
		assertFalse(consumed);
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
		{

			System.gc();
			return weak.isEnqueued();

		});
		assertTrue(consumed);
		assertEquals(System.identityHashCode(p1), originalIdentityHashCode);
	}

	private PhantomReference<RunnableCloseable> decorateList(Collection<Person> personList)
			throws InterruptedException, ExecutionException
	{
		RunnableCloseable ac = CollSyncBuilder.builder(Person.class)
				.withAutoRefresh(() -> PersonService.getPeople(), 1, TimeUnit.HOURS, 1)
				.withPrimaryKeyFunction(Person::getId).withCloseHandler(() -> consumed = true).decorate(personList);

		CompletableFuture.runAsync(ac).get();
		return new PhantomReference<>(ac, new ReferenceQueue<>());
	}

	private static class Person
	{

		private Integer id;
		private String name;
		private Integer age;

		public void setId(Integer id)
		{
			this.id = id;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public void setAge(Integer age)
		{
			this.age = age;
		}

		public Integer getId()
		{
			return id;
		}

		public String getName()
		{
			return name;
		}

		public Integer getAge()
		{
			return age;
		}

	}

	private static class PersonService
	{

		private static Collection<Person> backingStore = new ArrayList<>();

		public static void reset()
		{
			backingStore.clear();
			Person p1 = new Person();
			p1.id = 1;
			p1.name = "John Doe";
			p1.age = 25;
			Person p2 = new Person();
			p2.id = 2;
			p2.name = "Jane Doe";
			p2.age = 25;
			backingStore.add(p1);
			backingStore.add(p2);
		}

		public static Collection<Person> getPeople()
		{
			return backingStore.stream().map(p ->
			{
				Person pn = new Person();
				pn.id = p.id;
				pn.name = p.name;
				pn.age = p.age;
				return pn;
			}).collect(Collectors.toList());
		}

		// TODO: Use this in an upcoming test.
		/*
		 * public static void deletePerson(Person pn) { Optional<Person> op =
		 * backingStore.stream().filter(x -> x.getId() == pn.getId()).findAny();
		 * if (op.isPresent()) backingStore.remove(op.get()); }
		 */

		public static void savePerson(Person pn)
		{
			if (pn.id == null || pn.id == -1 || !backingStore.stream().map(x -> x.getId()).anyMatch(x -> x == pn.id))
			{
				addPerson(pn);
			}
			else
			{
				for (Person p : backingStore)
				{
					if (p.id == pn.id)
					{
						p.name = pn.name;
						p.age = pn.age;
					}
				}

			}
		}

		public static void addPerson(Person pn)
		{
			if (pn.id == null)
			{
				OptionalInt optInt = backingStore.stream().map(x -> x.getId()).mapToInt(Integer::intValue).max();
				if (optInt.isPresent())
				{
					Integer newId = optInt.getAsInt() + 1;
					pn.id = newId;
				}
			}
			backingStore.add(pn);
		}
	}
}
