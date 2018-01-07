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

import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import static java.util.Objects.nonNull;

/**
 * <h1>CollSyncBuilder</h1> 
 * <strong>CollSyncBuilder</strong> is a utility
 * <i>decorator</i> class, designed to assist you synchronizing a list
 * with a back end service, while preserving the identity of the objects
 * in your list.
 * 
 * @author Christopher Bryan Boyd <wdodencafe@gmail.com>
 * @version 0.1
 * @since 2018-01-07
 * @see {@link java.lang.System#identityHashCode}
 *
 */
public final class CollSyncBuilder<T>
{

	// SLF4J Logger
	private static final Logger logger = LoggerFactory.getLogger(CollSyncBuilder.class);

	private static ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	private Optional<ExecutorService> executorService = Optional.empty();

	private BiPredicate<T, T> equality = Objects::equals;

	private BiConsumer<T, T> equalizer = FieldSyncer::setFields;

	private Optional<LocalDateTime> expiration = Optional.empty();

	private Optional<Function<T, ?>> primaryKeyFunction = Optional.empty();

	private Optional<AutoRefreshSupplier<T>> autoRefreshSupplier = Optional.empty();

	private Optional<Observable<T>> createObservable = Optional.empty();

	private Optional<Observable<T>> updateObservable = Optional.empty();

	private Optional<Observable<T>> deleteObservable = Optional.empty();

	private Collection<Runnable> callback = new ArrayList<>();

	private Collection<Runnable> closeHandler = new ArrayList<>();

	private CollSyncBuilder()
	{
	}
 
	public CollSyncBuilder<T> withCallback(Runnable callback)
	{
		Objects.requireNonNull(callback, "Cannot provide a null callback.");
		logger.trace(String.format("CollSyncBuilder.withCallback([callback] Runnable %d)",
				System.identityHashCode(callback)));
		this.callback.add(callback);
		return this;

	}

	public CollSyncBuilder<T> withPrimaryKeyFunction(Function<T, ?> primaryKeyFunction)
	{
		Objects.requireNonNull(primaryKeyFunction, "Cannot provide a null Primary Key function");
		logger.trace(String.format("CollSyncBuilder.withPrimaryKeyFunction([primaryKeyFunction] Function<T, ?> %d)",
				System.identityHashCode(primaryKeyFunction)));
		this.primaryKeyFunction = Optional.of(primaryKeyFunction);
		return this;

	}

	public CollSyncBuilder<T> withCreate(Observable<T> create)
	{
		Objects.requireNonNull(create, "Observable cannot be null.");
		logger.trace(String.format("CollSyncBuilder.withCreate([create] Observable<T> %d)",
				System.identityHashCode(create)));
		this.createObservable = Optional.of(create);
		return this;

	}

	public CollSyncBuilder<T> withUpdate(Observable<T> update)
	{
		Objects.requireNonNull(update, "Observable cannot be null.");
		logger.trace(String.format("CollSyncBuilder.withUpdate([update] Observable<T> %d)",
				System.identityHashCode(update)));
		this.updateObservable = Optional.of(update);
		return this;

	}

	public CollSyncBuilder<T> withDelete(Observable<T> delete)
	{
		Objects.requireNonNull(delete, "Observable cannot be null.");
		logger.trace(String.format("CollSyncBuilder.withDelete([delete] Observable<T> %d)",
				System.identityHashCode(delete)));
		this.deleteObservable = Optional.of(delete);
		return this;

	}

	public CollSyncBuilder<T> withCloseHandler(Runnable closeHandler)
	{
		Objects.requireNonNull(closeHandler, "Cannot provide a null closeHandler.");
		logger.trace(String.format("CollSyncBuilder.withCloseHandler([closeHandler] Runnable %d)",
				System.identityHashCode(closeHandler)));
		this.closeHandler.add(closeHandler);
		return this;
	}

	public CollSyncBuilder<T> withReflectionEquality()
	{
		logger.trace("CollSyncBuilder.withReflectionEquality()", System.identityHashCode(closeHandler));
		this.equality = EqualsBuilder::reflectionEquals;
		return this;
	}

	public CollSyncBuilder<T> withEquality(BiPredicate<T, T> equality)
	{
		Objects.requireNonNull(equality, "BiPredicate<T, T> 'equality' cannot be null.");
		logger.trace("CollSyncBuilder.withEquality([equality] BiPredicate<T, T> %d)",
				System.identityHashCode(equality));
		this.equality = equality;
		return this;
	}

	public CollSyncBuilder<T> withEqualizer(BiConsumer<T, T> equalizer)
	{
		Objects.requireNonNull(equalizer, "BiConsumer<T, T> 'equalizer' cannot be null.");
		logger.trace("CollSyncBuilder.withEqualizer(BiConsumer<T, T> %d)", System.identityHashCode(equalizer));
		this.equalizer = equalizer;
		return this;
	}

	public CollSyncBuilder<T> withExecutor(ExecutorService service)
	{
		this.executorService = Optional.ofNullable(service);
		logger.trace("CollSyncBuilder.withExecutor([service] ExecutorService "
				+ (service == null ? null : System.identityHashCode(service)) + ")");
		return this;
	}

	public CollSyncBuilder<T> withExpiration(LocalDateTime expiration)
	{
		logger.trace("CollSyncBuilder.withExpiration([expiration] LocalDateTime "
				+ (expiration == null ? null : System.identityHashCode(expiration)) + ")");
		this.expiration = Optional.ofNullable(expiration);
		return this;
	}

	public CollSyncBuilder<T> withAutoRefresh(Supplier<Collection<T>> supplier, int time, TimeUnit timeUnit,
			int limit)
	{
		Objects.requireNonNull(supplier, "Supplier<T> 'supplier' cannot be null.");

		Preconditions.checkArgument(time > 0, "int 'time' must be greater than 0, you supplied %s.", time);

		Objects.requireNonNull(timeUnit, "TimeUnit 'timeUnit' cannot be null.");

		logger.trace(
				"CollSyncBuilder.withAutoRefresh([supplier] Supplier<Collection<T>> %d, [time] int %d, [timeUnit] TimeUnit %s, [limit] int %d)",
				System.identityHashCode(supplier), time, timeUnit.name(), limit);

		this.autoRefreshSupplier = Optional.of(AutoRefreshSupplier.of(supplier, time, timeUnit, limit));

		return this;
	}

	public CollSyncBuilder<T> withAutoRefresh(Supplier<Collection<T>> supplier, int time, TimeUnit timeUnit)
	{
		Preconditions.checkNotNull(supplier, "Supplier<T> 'supplier' cannot be null.");

		Preconditions.checkArgument(time > 0, "int 'time' must be greater than 0, you supplied %s.", time);

		Preconditions.checkNotNull(timeUnit, "TimeUnit 'timeUnit' cannot be null.");

		logger.trace(
				"CollSyncBuilder.withAutoRefresh([supplier] Supplier<Collection<T>> %d, [time] int %d, [timeUnit] TimeUnit %s",
				System.identityHashCode(supplier), time, timeUnit.name());

		this.autoRefreshSupplier = Optional.of(AutoRefreshSupplier.of(supplier, time, timeUnit, -1));

		return this;
	}

	public RunnableCloseable decorate(Collection<T> list) throws IllegalArgumentException
	{

		Objects.requireNonNull(list, "Collection to decorate cannot be null");
		logger.trace("CollSyncBuilder.decorate([list] Supplier<Collection<T>> %d)", System.identityHashCode(list));
		AtomicInteger atomic = new AtomicInteger();
		WeakReference<Collection<T>> obj = new WeakReference<>(list);
		handleInvalidArguments();

		WeakReference<?>[] ac = new WeakReference<?>[1];
		if (!executorService.isPresent())
			executorService = Optional.of(Executors.newCachedThreadPool());

		Observer<T>[] createHandler = getCreateHandler(list);
		Observer<T>[] updateHandler = getUpdateHandler(list);
		Observer<T>[] deleteHandler = getDeleteHandler(list);
		handleExpiration(ac);
		ScheduledFuture<?>[] future = handleAutoRefresh(list, ac, atomic);

		Collection<AutoCloseable> innerCloseables = new HashSet<>();

		if (nonNull(future) && nonNull(future[0]))
		{
			innerCloseables.add(new AutoCloseable()
			{

				@Override
				public void close() throws Exception
				{

					if (!future[0].isCancelled() && !future[0].isDone())
					{
						future[0].cancel(true);
					}
				}
			});
		}
		if (createObservable.isPresent())
		{
			innerCloseables.add(new AutoCloseable()
			{

				@Override
				public void close() throws Exception
				{

					if (Objects.nonNull(createHandler[0]))
					{
						createHandler[0].onComplete();
					}
				}
			});
		}
		if (updateObservable.isPresent())
		{
			innerCloseables.add(new AutoCloseable()
			{

				@Override
				public void close() throws Exception
				{
					if (Objects.nonNull(updateHandler[0]))
					{
						updateHandler[0].onComplete();
					}
				}
			});
		}
		if (deleteObservable.isPresent())

		{
			innerCloseables.add(new AutoCloseable()
			{

				@Override
				public void close() throws Exception
				{
					if (Objects.nonNull(deleteHandler[0]))
					{
						deleteHandler[0].onComplete();
					}
				}
			});
		}

		Runnable proxy = () ->
		{
			if (ac[0].get() != null)
				GarbageDisposal.undecorate(ac[0].get());
			if (closeHandler.size() > 0)
			{
				CompletableFuture.runAsync(() ->
				{
					for (Runnable runnable : closeHandler)
						runnable.run();
				}, executorService.get());

			}

			try
			{
				getAutoCloseable(innerCloseables, obj).close();
			}
			catch (Exception e)
			{
				logger.error("CollSyncBuilder.decorate$Runnable[proxy] error while cleaning", e);
			}

		};
		RunnableCloseable autoCloseable = new RunnableCloseable()
		{

			private boolean closed = false;

			@Override
			public void close() throws Exception
			{
				if (!closed)
				{
					closed = true;
					if (obj.get() != null)
						GarbageDisposal.undecorate(obj.get());
					proxy.run();
				}
			}

			@Override
			public CompletableFuture<Void> runAsync()
			{
				if (autoRefreshSupplier.isPresent())
				{
					CompletableFuture<Void> cf = new CompletableFuture<>();
					if (autoRefreshSupplier.get().getLimit() > atomic.get() && obj.get() != null)
					{
						refreshList(obj.get(), executorService.get(), equality, primaryKeyFunction.get(),
								autoRefreshSupplier.get().getSupplier(), equalizer, atomic, Optional.of(() ->
								{
									if (callback.size() > 0)
										for (Runnable runnable : callback)
											runnable.run();
									cf.complete(null);
								}), true);
					}
					else
					{
						try
						{
							close();
						}
						catch (Exception e)
						{
							logger.error(
									"CollSyncBuilder.decorate$RefreshableCloseable[autoCloseable] error while decorating",
									e);
						}
					}
					return cf;
				}
				else
					throw new UnsupportedOperationException(
							"Cannot call refresh without calling CollSyncBuilder.withAutoRefresh");
			}

			@Override
			public boolean isClosed()
			{
				return closed;
			}

			@Override
			public void run()
			{
				if (autoRefreshSupplier.isPresent())
				{
					if (autoRefreshSupplier.get().getLimit() > atomic.get() && obj.get() != null)
					{
						try
						{
							refreshList(obj.get(), executorService.get(), equality, primaryKeyFunction.get(),
									autoRefreshSupplier.get().getSupplier(), equalizer, atomic, Optional.of(() ->
									{
										if (callback.size() > 0)
											for (Runnable runnable : callback)
												runnable.run();
									}), false).get();
						}
						catch (InterruptedException | ExecutionException e)
						{
							logger.error(
								"CollSyncBuilder.decorate$RefreshableCloseable.run()[autoCloseable] error while running AutoCloseable",
								e);
						}
					}
					else
					{
						try
						{
							close();
						}
						catch (Exception e)
						{
							logger.error(
									"CollSyncBuilder.decorate$RefreshableCloseable.run()[autoCloseable] error while closing AutoCloseable",
									e);
						}
					}
				}
				else
					throw new UnsupportedOperationException(
							"Cannot call refresh without calling CollSyncBuilder.withAutoRefresh");

			}

		};
		ac[0] = new WeakReference<>(autoCloseable);
		GarbageDisposal.decorate(autoCloseable, proxy);
		GarbageDisposal.decorate(list, proxy);
		return autoCloseable;
	}

	private AutoCloseable getAutoCloseable(Collection<AutoCloseable> closeables, WeakReference<Collection<T>> obj)
	{
		Objects.requireNonNull(closeables, "closeables cannot be null");
		Objects.requireNonNull(obj, "weakreference cannot be null");
		Objects.requireNonNull(obj.get(), "weakreference value cannot be null");

		logger.trace(
				"CollSyncBuilder.getAutoCloseable([closeables] Collection<AutoCloseable> %d, [obj] WeakReference<Collection<T>> %d)",
				System.identityHashCode(closeables), System.identityHashCode(closeables));
		final AutoCloseable[] a = new AutoCloseable[1];
		a[0] = new AutoCloseable()
		{

			private boolean closed = false;

			@Override
			public void close() throws Exception
			{
				if (!closed)
				{
					closed = true;
					for (AutoCloseable ac : closeables)
						ac.close();
				}
			}
		};
		return a[0];
	}

	private ScheduledFuture<?>[] handleAutoRefresh(Collection<T> list, WeakReference<?>[] ac, AtomicInteger atomic)
	{
		ScheduledFuture<?>[] future = new ScheduledFuture<?>[1];
		if (autoRefreshSupplier.isPresent())
		{
			future[0] = getSchedule(list, executorService.get(), equality, primaryKeyFunction.get(),
					autoRefreshSupplier.get(), ac, equalizer, atomic, Optional.of(() ->
					{

						if (callback.size() > 0)
							for (Runnable runnable : callback)
								runnable.run();
					}));

		}
		return future;
	}

	private void handleInvalidArguments()
	{
		primaryKeyFunction.orElseThrow(() -> new IllegalArgumentException("Must Specify Primary Key Function"));

		if (!createObservable.isPresent() && !updateObservable.isPresent() && !deleteObservable.isPresent()
				&& !autoRefreshSupplier.isPresent())
			throw new IllegalArgumentException("Must specify Observable<T> 'createObservable', "
					+ "Observable<T> 'updateObservable', Observable<T> 'deleteObservable', or AutoRefreshSupplier<T> 'autoRefreshSupplier'.");
	}

	private void handleExpiration(WeakReference<?>[] ac)
	{
		if (expiration.isPresent())
		{
			long seconds = LocalDateTime.now().until(expiration.get(), ChronoUnit.SECONDS);
			scheduledExecutor.schedule(() -> executorService.get().submit(() ->
			{
				try
				{
					if (nonNull(ac) && nonNull(ac[0]) && nonNull(ac[0].get()))
					{
						AutoCloseable autoCloseable = (AutoCloseable) ac[0].get();
						autoCloseable.close();
					}
				}
				catch (Exception e)
				{
					logger.error("CollSyncBuilder.handleExpiration", e);
				}
			}), seconds, TimeUnit.SECONDS);
		}
	}

	private Observer<T>[] getDeleteHandler(Collection<T> list)
	{
		@SuppressWarnings("unchecked")
		Observer<T>[] deleteHandler = (Observer<T>[]) new Observer<?>[1];
		if (deleteObservable.isPresent())
		{
			deleteHandler[0] = Handlers.handleDelete(list, primaryKeyFunction.get(), executorService.get(),
					Optional.of(() ->
					{
						if (callback.size() > 0)
							for (Runnable runnable : callback)
								runnable.run();
					}));
			deleteObservable.get().subscribeOn(Schedulers.from(executorService.get())).subscribe(deleteHandler[0]);

		}
		return deleteHandler;
	}

	private Observer<T>[] getUpdateHandler(Collection<T> list)
	{
		@SuppressWarnings("unchecked")
		Observer<T>[] updateHandler = (Observer<T>[]) new Observer<?>[1];
		if (updateObservable.isPresent())
		{
			updateHandler[0] = Handlers.handleUpdate(list, primaryKeyFunction.get(), executorService.get(), equality,
					equalizer, Optional.of(() ->
					{
						if (callback.size() > 0)
							for (Runnable runnable : callback)
								runnable.run();
					}));
			updateObservable.get().subscribeOn(Schedulers.from(executorService.get())).subscribe(updateHandler[0]);

		}
		return updateHandler;
	}

	private Observer<T>[] getCreateHandler(Collection<T> list)
	{
		@SuppressWarnings("unchecked")
		Observer<T>[] createHandler = (Observer<T>[]) new Observer<?>[1];
		if (createObservable.isPresent())
		{
			createHandler[0] = Handlers.handleCreate(list, primaryKeyFunction.get(), executorService.get(),
					Optional.of(() ->
					{
						if (callback.size() > 0)
							for (Runnable runnable : callback)
								runnable.run();
					}));
			createObservable.get().subscribeOn(Schedulers.from(executorService.get())).subscribe(createHandler[0]);

		}
		return createHandler;
	}

	private static <T> ScheduledFuture<?> getSchedule(Collection<T> list, ExecutorService executorService,
			BiPredicate<T, T> equality, Function<T, ?> primaryKeyFunction, AutoRefreshSupplier<T> autoRefreshSupplier,
			WeakReference<?>[] ac, BiConsumer<T, T> equalizer, AtomicInteger atomic, Optional<Runnable> callback)
	{
		logger.trace("CollSyncBuilder.getSchedule(" + "[list] Collection<T> %d, "
				+ "[executorService] ExecutorService %d, " + "[equality] BiPredicate<T, T> %d, "
				+ "[primaryKeyFunction] Function<T, ?> %d, " + "[autoRefreshSupplier] AutoRefreshSupplier<T> %d, "
				+ "[ac] WeakReference<?>[] %d, " + "[equalizer] BiConsumer<T, T> %d, " + "[atomic] AtomicInteger %d, "
				+ "[callback] Optional<Runnable> %d)", System.identityHashCode(list),
				System.identityHashCode(executorService), System.identityHashCode(equality),
				System.identityHashCode(primaryKeyFunction), System.identityHashCode(autoRefreshSupplier),
				System.identityHashCode(ac), System.identityHashCode(equalizer), System.identityHashCode(atomic),
				System.identityHashCode(callback));

		final WeakReference<Collection<T>> weakReference = new WeakReference<>(list);
		return scheduledExecutor.scheduleAtFixedRate(() ->
		{
			Runnable runnable = () ->
			{
				try
				{
					if (nonNull(weakReference.get()))
					{
						if (autoRefreshSupplier.getLimit() > atomic.get())
						{
							Collection<T> weakList = weakReference.get();
							synchronized (weakList)
							{
								refreshList(weakList, executorService, equality, primaryKeyFunction,
										autoRefreshSupplier.getSupplier(), equalizer, atomic, callback, true);
							}
						}
						else
						{
							clean(ac);
						}
					}

				}
				catch (Throwable th)
				{
					th.printStackTrace();
					clean(ac);
				}
			};

			try
			{
				CompletableFuture.runAsync(runnable, executorService).get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				logger.error("CollSyncBuilder.getSchedule", e);
			}
		}, autoRefreshSupplier.getTime(), autoRefreshSupplier.getTime(), autoRefreshSupplier.getTimeUnit());
	}

	private static <T> CompletableFuture<Void> refreshList(Collection<T> list, ExecutorService executorService,
			BiPredicate<T, T> equality, Function<T, ?> primaryKeyFunction, Supplier<Collection<T>> supplier,
			BiConsumer<T, T> equalizer, AtomicInteger atomicInteger, Optional<Runnable> callback,boolean callAsync)
	{
		return CompletableFuture.runAsync(() ->
		{
			Optional<Collection<CompletableFuture<Void>>> optional;
			if (callAsync)
				optional = Optional.of(new HashSet<>());
			else
				optional = Optional.empty();
			atomicInteger.incrementAndGet();
			Map<T, Object> pkList = new HashMap<>();
			list.forEach(c -> pkList.put(c, primaryKeyFunction.apply(c)));
			Collection<T> newList = supplier.get();
			for (T c : newList)
			{
				if (!pkList.containsValue(primaryKeyFunction.apply(c)))
				{
					list.add(c);
					if (callback.isPresent())
					{
						CompletableFuture<Void> cf = CompletableFuture.runAsync(callback.get(), executorService);
						if (callAsync)
							optional.get().add(cf);
												
					}
				}
			}
			Map<T, Object> newPkList = new HashMap<>();
			newList.forEach(c -> newPkList.put(c, primaryKeyFunction.apply(c)));
			for (T c : list)
				if (!newPkList.containsValue(primaryKeyFunction.apply(c)))
				{
					list.remove(c);
					if (callback.isPresent())
					{
						CompletableFuture<Void> cf = CompletableFuture.runAsync(callback.get(), executorService);
						if (callAsync)
							optional.get().add(cf);
					}
				}
			for (T value : list)
			{
				Optional<T> newValue = newList.stream()
						.filter(x -> Objects.equals(primaryKeyFunction.apply(x), primaryKeyFunction.apply(value)))
						.findAny();
				if (newValue.isPresent() && !equality.test(value, newValue.get()))
				{

					equalizer.accept(newValue.get(), value);
					if (callback.isPresent())
					{
						CompletableFuture<Void> cf = CompletableFuture.runAsync(callback.get(), executorService);
						if (callAsync)
							optional.get().add(cf);
					}

				}
			}
			if (callAsync)
				for (CompletableFuture<Void> cf :optional.get()) {
					try
					{
						cf.get();
					}
					catch (InterruptedException | ExecutionException e)
					{
						logger.error("CollSyncBuilder.refreshList", e);
					}
				}
		}, executorService);
	}

	private static void clean(WeakReference<?>[] weakReference)
	{
		try
		{
			if (logger.isTraceEnabled())
			{
				logger.trace("CollSyncBuilder.clean([weakReference] WeakReference<?>[] "
						+ (weakReference == null ? "NULL" : System.identityHashCode(weakReference)) + ")");
			}
			if (nonNull(weakReference) && nonNull(weakReference[0]) && nonNull(weakReference[0].get()))
			{
				AutoCloseable autoCloseable = (AutoCloseable) weakReference[0].get();
				autoCloseable.close();
			}
			else
			{
				logger.warn("CollSyncBuilder.clean unable to clean WeakReference<?>[]");
			}
		}
		catch (Exception e)
		{
			logger.error("CollSyncBuilder.clean error while cleaning", e);
		}
	}

	public static <X> CollSyncBuilder<X> builder(Class<X> clazz)
	{
		Objects.requireNonNull(clazz, "Class cannot be null.");
		logger.trace("CollSyncBuilder.builder([clazz] Class<X> %d)", System.identityHashCode(clazz));
		return new CollSyncBuilder<X>();
	}

	private interface AutoRefreshSupplier<T>
	{

		Supplier<Collection<T>> getSupplier();

		int getTime();

		TimeUnit getTimeUnit();

		int getLimit();

		static <T> AutoRefreshSupplier<T> of(Supplier<Collection<T>> supplier, int time, TimeUnit timeUnit, int limit)
		{
			logger.trace(
					"AutoRefreshSupplier.of([[supplier] Supplier<Collection<T>> %d, [time] int %d, [timeUnit] TimeUnit %s, [limit] int %d)",
					System.identityHashCode(supplier), time, timeUnit.name(), limit);
			return new AutoRefreshSupplierImpl<T>(supplier, time, timeUnit, limit);
		}

	}

	private static class Handlers
	{

		private static abstract class BaseObserver<T> implements Observer<T>
		{

			private WeakReference<Disposable> d;
			protected final WeakReference<Collection<T>> weakReference;

			BaseObserver(Collection<T> list)
			{
				this.weakReference = new WeakReference<>(list);

			}

			@Override
			public void onSubscribe(Disposable d)
			{
				this.d = new WeakReference<>(d);
			}

			@Override
			public void onError(Throwable e)
			{
				logger.error("CollSyncBuilder.Handlers.BaseObservable.onError", e);
				onComplete();
			}

			@Override
			public void onComplete()
			{
				if (nonNull(d) && nonNull(d.get()) && !d.get().isDisposed())
					d.get().dispose();
			}

		}

		private static <T> Observer<T> handleDelete(Collection<T> list, Function<T, ?> primaryKeyFunction,
				ExecutorService executorService, Optional<Runnable> callback)
		{
			return new BaseObserver<T>(list)
			{

				@Override
				public void onNext(T c)
				{
					if (nonNull(weakReference.get()))
					{
						Collection<T> list = weakReference.get();
						try
						{
							synchronized (list)
							{
								Optional<T> value = list.stream().filter(
										(x) -> Objects.equals(primaryKeyFunction.apply(c), primaryKeyFunction.apply(x)))
										.findAny();
								if (value.isPresent())
								{
									list.remove(value.get());
									if (callback.isPresent())
										CompletableFuture.runAsync(callback.get(), executorService);
								}
							}
						}
						catch (Throwable th)
						{
							logger.error("CollSyncBuilder.Handlers.handleDelete.$BaseObservable.onNext error", th);
							onComplete();
						}
					}
				}

			};
		}

		private static <T> Observer<T> handleUpdate(Collection<T> list, Function<T, ?> primaryKeyFunction,
				ExecutorService executorService, BiPredicate<T, T> equality, BiConsumer<T, T> equalizer,
				Optional<Runnable> callback)
		{
			return new BaseObserver<T>(list)
			{

				@Override
				public void onNext(T c)
				{
					if (nonNull(weakReference.get()))
					{
						Collection<T> list = weakReference.get();
						try
						{
							synchronized (list)
							{
								Optional<T> value = list.stream().filter(
										(x) -> Objects.equals(primaryKeyFunction.apply(c), primaryKeyFunction.apply(x)))
										.filter((x) -> !equality.test(c, x)).findAny();
								if (value.isPresent())
								{
									equalizer.accept(c, value.get());
									if (callback.isPresent())
										CompletableFuture.runAsync(callback.get(), executorService);
								}
							}
						}
						catch (Throwable th)
						{
							logger.error("CollSyncBuilder.Handlers.handleUpdate.$BaseObservable.onNext error", th);
							onComplete();
						}
					}
				}
			};
		}

		private static <T> Observer<T> handleCreate(Collection<T> list, Function<T, ?> primaryKeyFunction,
				ExecutorService executorService, Optional<Runnable> callback)
		{
			return new BaseObserver<T>(list)
			{

				@Override
				public void onNext(T c)
				{
					if (nonNull(weakReference.get()))
					{
						Collection<T> list = weakReference.get();
						try
						{
							synchronized (list)
							{
								if (!list.stream().map(primaryKeyFunction::apply)
										.anyMatch(x -> Objects.equals(primaryKeyFunction.apply(c), x)))
								{
									list.add(c);
									if (callback.isPresent())
										CompletableFuture.runAsync(callback.get(), executorService);
								}
							}
						}
						catch (Throwable th)
						{
							logger.error("CollSyncBuilder.Handlers.handleCreate.$BaseObservable.onNext error", th);
							onComplete();
						}
					}
				}
			};
		}
	}

	private static class AutoRefreshSupplierImpl<T> implements AutoRefreshSupplier<T>
	{

		private final Supplier<Collection<T>> supplier;
		private final int time;
		private final TimeUnit timeUnit;
		private final int limit;

		private AutoRefreshSupplierImpl(Supplier<Collection<T>> supplier,
				int time,
				TimeUnit timeUnit,
				int limit)
		{
			super();

			logger.trace(
						"new AutoRefreshSupplierImpl(" +
							"[supplier] Supplier<Collection<T>> %d," +
							"[time] int %d," + 
							"[timeUnit] TimeUnit %s," + 
							"[limit] int %d)",
					System.identityHashCode(supplier), time, timeUnit.name(), limit);
			this.supplier = supplier;
			this.time = time;
			this.timeUnit = timeUnit;
			this.limit = limit;
		}

		public Supplier<Collection<T>> getSupplier()
		{
			return supplier;
		}

		public int getTime()
		{
			return time;
		}

		public TimeUnit getTimeUnit()
		{
			return timeUnit;
		}

		@Override
		public int getLimit()
		{
			return limit;
		}

	}
}
