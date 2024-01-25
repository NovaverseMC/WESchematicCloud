package it.novaverse.weschematiccloud.utils;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class NanoPromise<T> {

    @Nonnull
    public static <T> NanoPromise<T> empty() {
        return new NanoPromise<>();
    }

    @Nonnull
    public static <T> NanoPromise<T> completed(@Nullable T value) {
        return new NanoPromise<>(value);
    }

    @Nonnull
    public static NanoPromise<Void> start() {
        return completed(null);
    }

    @Nonnull
    public static <T> NanoPromise<T> exceptionally(@Nonnull Throwable t) {
        Objects.requireNonNull(t);
        return new NanoPromise<>(t);
    }

    @Nonnull
    public static <T> NanoPromise<T> wrapFuture(@Nonnull Future<T> future) {
        Objects.requireNonNull(future);
        if (future instanceof CompletableFuture<?>) {
            return new NanoPromise<>(((CompletableFuture<T>) future).thenApply(Function.identity()));
        } else if (future instanceof CompletionStage<?>) {
            //noinspection unchecked
            CompletionStage<T> fut = (CompletionStage<T>) future;
            return new NanoPromise<>(fut.toCompletableFuture().thenApply(Function.identity()));
        } else if (future instanceof ListenableFuture<?>) {
            ListenableFuture<T> fut = (ListenableFuture<T>) future;
            NanoPromise<T> promise = empty();
            promise.supplied.set(true);

            Futures.addCallback(fut, new FutureCallback<>() {
                @Override
                public void onSuccess(@Nullable T result) {
                    promise.complete(result);
                }

                @Override
                public void onFailure(@Nonnull Throwable t) {
                    Objects.requireNonNull(t);
                    promise.completeExceptionally(t);
                }
            }, MoreExecutors.directExecutor());

            return promise;
        } else {
            if (future.isDone()) {
                try {
                    return completed(future.get());
                } catch (ExecutionException e) {
                    return exceptionally(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return supplyingExceptionallyAsync(future::get);
            }
        }
    }

    @Nonnull
    static <U> NanoPromise<U> supplyingSync(@Nonnull Supplier<U> supplier) {
        Objects.requireNonNull(supplier);
        NanoPromise<U> p = empty();
        return p.supplySync(supplier);
    }

    @Nonnull
    static <U> NanoPromise<U> supplyingAsync(@Nonnull Supplier<U> supplier) {
        Objects.requireNonNull(supplier);
        NanoPromise<U> p = empty();
        return p.supplyAsync(supplier);
    }

    @Nonnull
    static <U> NanoPromise<U> supplyingDelayedSync(@Nonnull Supplier<U> supplier, long delayTicks) {
        Objects.requireNonNull(supplier);
        NanoPromise<U> p = empty();
        return p.supplyDelayedSync(supplier, delayTicks);
    }

    @Nonnull
    static <U> NanoPromise<U> supplyingDelayedAsync(@Nonnull Supplier<U> supplier, long delayTicks) {
        Objects.requireNonNull(supplier);
        NanoPromise<U> p = empty();
        return p.supplyDelayedAsync(supplier, delayTicks);
    }

    @Nonnull
    static <U> NanoPromise<U> supplyingExceptionallySync(@Nonnull Callable<U> callable) {
        Objects.requireNonNull(callable);
        NanoPromise<U> p = empty();
        return p.supplyExceptionallySync(callable);
    }

    @Nonnull
    static <U> NanoPromise<U> supplyingExceptionallyAsync(@Nonnull Callable<U> callable) {
        Objects.requireNonNull(callable);
        NanoPromise<U> p = empty();
        return p.supplyExceptionallyAsync(callable);
    }

    @Nonnull
    static <U> NanoPromise<U> supplyingExceptionallyDelayedSync(@Nonnull Callable<U> callable, long delayTicks) {
        Objects.requireNonNull(callable);
        NanoPromise<U> p = empty();
        return p.supplyExceptionallyDelayedSync(callable, delayTicks);
    }

    @Nonnull
    static <U> NanoPromise<U> supplyingExceptionallyDelayedAsync(@Nonnull Callable<U> callable, long delayTicks) {
        Objects.requireNonNull(callable);
        NanoPromise<U> p = empty();
        return p.supplyExceptionallyDelayedAsync(callable, delayTicks);
    }

    @Nullable
    private static JavaPlugin plugin;

    @Nonnull
    private static JavaPlugin getPlugin() {
        if (plugin == null) {
            plugin = Objects.requireNonNull(JavaPlugin.getProvidingPlugin(NanoPromise.class));
        }
        return plugin;
    }

    @Nonnull
    public static Supplier<Void> runnableToSupplier(@Nonnull Runnable runnable) {
        Objects.requireNonNull(runnable);
        return () -> {
            runnable.run();
            return null;
        };
    }

    @Nonnull
    public static <T> Function<T, Void> consumerToFunction(@Nonnull Consumer<T> consumer) {
        Objects.requireNonNull(consumer);
        return (value) -> {
            consumer.accept(value);
            return null;
        };
    }

    @Nonnull
    public static <T> Function<T, Void> runnableToFunction(@Nonnull Runnable runnable) {
        Objects.requireNonNull(runnable);
        return (value) -> {
            runnable.run();
            return null;
        };
    }

    private final AtomicBoolean supplied = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    @Nonnull
    private final CompletableFuture<T> future;

    private NanoPromise() {
        future = new CompletableFuture<>();
    }

    private NanoPromise(@Nullable T value) {
        future = CompletableFuture.completedFuture(value);
        supplied.set(true);
    }

    private NanoPromise(@Nonnull Throwable t) {
        Objects.requireNonNull(t);
        future = new CompletableFuture<>();
        future.completeExceptionally(t);
        supplied.set(true);
    }

    private NanoPromise(@Nonnull CompletableFuture<T> future) {
        Objects.requireNonNull(future);
        this.future = Objects.requireNonNull(future, "future");
        supplied.set(true);
        cancelled.set(future.isCancelled());
    }

    /* utility methods */

    private void executeSync(@Nonnull Runnable runnable) {
        Objects.requireNonNull(runnable);
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(getPlugin(), runnable);
        }
    }

    private void executeAsync(@Nonnull Runnable runnable) {
        Objects.requireNonNull(runnable);
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), runnable);
    }

    private void executeDelayedSync(@Nonnull Runnable runnable, long delayTicks) {
        Objects.requireNonNull(runnable);
        if (delayTicks <= 0) {
            executeSync(runnable);
        } else {
            Bukkit.getScheduler().runTaskLater(getPlugin(), runnable, delayTicks);
        }
    }

    private void executeDelayedAsync(@Nonnull Runnable runnable, long delayTicks) {
        Objects.requireNonNull(runnable);
        if (delayTicks <= 0) {
            executeAsync(runnable);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(), runnable, delayTicks);
        }
    }

    private boolean complete(@Nullable T value) {
        return !cancelled.get() && future.complete(value);
    }

    private boolean completeExceptionally(@Nonnull Throwable t) {
        Objects.requireNonNull(t);
        return !cancelled.get() && future.completeExceptionally(t);
    }

    private void markAsSupplied() {
        if (!supplied.compareAndSet(false, true)) {
            throw new IllegalStateException("Promise is already being supplied.");
        }
    }

    /* future methods */

    public boolean cancel(boolean mayInterruptIfRunning) {
        cancelled.set(true);
        return future.cancel(mayInterruptIfRunning);
    }

    public boolean cancel() {
        return cancel(true);
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean isDone() {
        return future.isDone();
    }

    @Nullable
    public T get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Nullable
    public T get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Objects.requireNonNull(unit);
        return future.get(timeout, unit);
    }

    @Nullable
    public T join() {
        return future.join();
    }

    @Nullable
    public T getNow(@Nullable T valueIfAbsent) {
        return future.getNow(valueIfAbsent);
    }

    @Nonnull
    public CompletableFuture<T> toCompletableFuture() {
        return future.thenApply(Function.identity());
    }

    public void close() {
        cancel();
    }

    public boolean isClosed() {
        return isCancelled();
    }

    /* implementation */

    @Nonnull
    public NanoPromise<T> supply(@Nullable T value) {
        markAsSupplied();
        complete(value);
        return this;
    }

    @Nonnull
    public NanoPromise<T> supplyException(@Nonnull Throwable exception) {
        Objects.requireNonNull(exception);
        markAsSupplied();
        completeExceptionally(exception);
        return this;
    }

    @Nonnull
    public NanoPromise<T> supplySync(@Nonnull Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        markAsSupplied();
        executeSync(new SupplyRunnable(supplier));
        return this;
    }

    @Nonnull
    public NanoPromise<T> supplyAsync(@Nonnull Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        markAsSupplied();
        executeAsync(new SupplyRunnable(supplier));
        return this;
    }

    @Nonnull
    public NanoPromise<T> supplyDelayedSync(@Nonnull Supplier<T> supplier, long delayTicks) {
        Objects.requireNonNull(supplier);
        markAsSupplied();
        executeDelayedSync(new SupplyRunnable(supplier), delayTicks);
        return this;
    }

    @Nonnull
    public NanoPromise<T> supplyDelayedAsync(@Nonnull Supplier<T> supplier, long delayTicks) {
        Objects.requireNonNull(supplier);
        markAsSupplied();
        executeDelayedAsync(new SupplyRunnable(supplier), delayTicks);
        return this;
    }

    @Nonnull
    public NanoPromise<T> supplyExceptionallySync(@Nonnull Callable<T> callable) {
        Objects.requireNonNull(callable);
        markAsSupplied();
        executeSync(new ThrowingSupplyRunnable(callable));
        return this;
    }

    @Nonnull
    public NanoPromise<T> supplyExceptionallyAsync(@Nonnull Callable<T> callable) {
        Objects.requireNonNull(callable);
        markAsSupplied();
        executeAsync(new ThrowingSupplyRunnable(callable));
        return this;
    }

    @Nonnull
    public NanoPromise<T> supplyExceptionallyDelayedSync(@Nonnull Callable<T> callable, long delayTicks) {
        Objects.requireNonNull(callable);
        markAsSupplied();
        executeDelayedSync(new ThrowingSupplyRunnable(callable), delayTicks);
        return this;
    }

    @Nonnull
    public NanoPromise<T> supplyExceptionallyDelayedAsync(@Nonnull Callable<T> callable, long delayTicks) {
        Objects.requireNonNull(callable);
        markAsSupplied();
        executeDelayedAsync(new ThrowingSupplyRunnable(callable), delayTicks);
        return this;
    }

    @Nonnull
    public <U> NanoPromise<U> thenApplySync(@Nonnull Function<? super T, ? extends U> fn) {
        Objects.requireNonNull(fn);
        NanoPromise<U> promise = empty();
        future.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeSync(new ApplyRunnable<>(promise, fn, value));
            }
        });
        return promise;
    }

    @Nonnull
    public <U> NanoPromise<U> thenApplyAsync(@Nonnull Function<? super T, ? extends U> fn) {
        Objects.requireNonNull(fn);
        NanoPromise<U> promise = empty();
        future.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeAsync(new ApplyRunnable<>(promise, fn, value));
            }
        });
        return promise;
    }

    @Nonnull
    public <U> NanoPromise<U> thenApplyDelayedSync(@Nonnull Function<? super T, ? extends U> fn, long delayTicks) {
        Objects.requireNonNull(fn);
        NanoPromise<U> promise = empty();
        future.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedSync(new ApplyRunnable<>(promise, fn, value), delayTicks);
            }
        });
        return promise;
    }

    @Nonnull
    public <U> NanoPromise<U> thenApplyDelayedAsync(@Nonnull Function<? super T, ? extends U> fn, long delayTicks) {
        Objects.requireNonNull(fn);
        NanoPromise<U> promise = empty();
        future.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedAsync(new ApplyRunnable<>(promise, fn, value), delayTicks);
            }
        });
        return promise;
    }

    @Nonnull
    public NanoPromise<Void> thenAcceptSync(@Nonnull Consumer<? super T> action) {
        return thenApplySync(consumerToFunction(action));
    }

    @Nonnull
    public NanoPromise<Void> thenAcceptAsync(@Nonnull Consumer<? super T> action) {
        return thenApplyAsync(consumerToFunction(action));
    }

    @Nonnull
    public NanoPromise<Void> thenAcceptDelayedSync(@Nonnull Consumer<? super T> action, long delayTicks) {
        return thenApplyDelayedSync(consumerToFunction(action), delayTicks);
    }

    @Nonnull
    public NanoPromise<Void> thenAcceptDelayedAsync(@Nonnull Consumer<? super T> action, long delayTicks) {
        return thenApplyDelayedAsync(consumerToFunction(action), delayTicks);
    }

    @Nonnull
    public NanoPromise<Void> thenRunSync(@Nonnull Runnable action) {
        return thenApplySync(runnableToFunction(action));
    }

    @Nonnull
    public NanoPromise<Void> thenRunAsync(@Nonnull Runnable action) {
        return thenApplyAsync(runnableToFunction(action));
    }

    @Nonnull
    public NanoPromise<Void> thenRunDelayedSync(@Nonnull Runnable action, long delayTicks) {
        return thenApplyDelayedSync(runnableToFunction(action), delayTicks);
    }

    @Nonnull
    public NanoPromise<Void> thenRunDelayedAsync(@Nonnull Runnable action, long delayTicks) {
        return thenApplyDelayedAsync(runnableToFunction(action), delayTicks);
    }

    @Nonnull
    public <U> NanoPromise<U> thenComposeSync(@Nonnull Function<? super T, ? extends NanoPromise<U>> fn) {
        Objects.requireNonNull(fn);
        NanoPromise<U> promise = empty();
        future.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeSync(new ComposeRunnable<>(promise, fn, value, true));
            }
        });
        return promise;
    }

    @Nonnull
    public <U> NanoPromise<U> thenComposeAsync(@Nonnull Function<? super T, ? extends NanoPromise<U>> fn) {
        Objects.requireNonNull(fn);
        NanoPromise<U> promise = empty();
        future.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeAsync(new ComposeRunnable<>(promise, fn, value, false));
            }
        });
        return promise;
    }

    @Nonnull
    public <U> NanoPromise<U> thenComposeDelayedSync(@Nonnull Function<? super T, ? extends NanoPromise<U>> fn, long delayTicks) {
        Objects.requireNonNull(fn);
        NanoPromise<U> promise = empty();
        future.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedSync(new ComposeRunnable<>(promise, fn, value, true), delayTicks);
            }
        });
        return promise;
    }

    @Nonnull
    public <U> NanoPromise<U> thenComposeDelayedAsync(@Nonnull Function<? super T, ? extends NanoPromise<U>> fn, long delayTicks) {
        Objects.requireNonNull(fn);
        NanoPromise<U> promise = empty();
        future.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedAsync(new ComposeRunnable<>(promise, fn, value, false), delayTicks);
            }
        });
        return promise;
    }

    @Nonnull
    public NanoPromise<T> exceptionallySync(@Nonnull Function<Throwable, ? extends T> fn) {
        Objects.requireNonNull(fn);
        NanoPromise<T> promise = empty();
        future.whenComplete((value, t) -> {
            if (t == null) {
                promise.complete(value);
            } else {
                executeSync(new ExceptionallyRunnable<>(promise, fn, t));
            }
        });
        return promise;
    }

    @Nonnull
    public NanoPromise<T> exceptionallyAsync(@Nonnull Function<Throwable, ? extends T> fn) {
        Objects.requireNonNull(fn);
        NanoPromise<T> promise = empty();
        future.whenComplete((value, t) -> {
            if (t == null) {
                promise.complete(value);
            } else {
                executeAsync(new ExceptionallyRunnable<>(promise, fn, t));
            }
        });
        return promise;
    }

    @Nonnull
    public NanoPromise<T> exceptionallyDelayedSync(@Nonnull Function<Throwable, ? extends T> fn, long delayTicks) {
        Objects.requireNonNull(fn);
        NanoPromise<T> promise = empty();
        future.whenComplete((value, t) -> {
            if (t == null) {
                promise.complete(value);
            } else {
                executeDelayedSync(new ExceptionallyRunnable<>(promise, fn, t), delayTicks);
            }
        });
        return promise;
    }

    @Nonnull
    public NanoPromise<T> exceptionallyDelayedAsync(@Nonnull Function<Throwable, ? extends T> fn, long delayTicks) {
        Objects.requireNonNull(fn);
        NanoPromise<T> promise = empty();
        future.whenComplete((value, t) -> {
            if (t == null) {
                promise.complete(value);
            } else {
                executeDelayedAsync(new ExceptionallyRunnable<>(promise, fn, t), delayTicks);
            }
        });
        return promise;
    }

    /* delegating behaviour runnables */

    private final class ThrowingSupplyRunnable implements Runnable {
        private final Callable<T> supplier;

        private ThrowingSupplyRunnable(@Nonnull Callable<T> supplier) {
            Objects.requireNonNull(supplier);
            this.supplier = supplier;
        }

        @Override
        public void run() {
            if (cancelled.get()) {
                return;
            }
            try {
                future.complete(this.supplier.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }
    }

    private final class SupplyRunnable implements Runnable {
        private final Supplier<T> supplier;

        private SupplyRunnable(@Nonnull Supplier<T> supplier) {
            Objects.requireNonNull(supplier);
            this.supplier = supplier;
        }

        @Override
        public void run() {
            if (cancelled.get()) {
                return;
            }
            try {
                future.complete(supplier.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }
    }

    private final class ApplyRunnable<U> implements Runnable {
        private final NanoPromise<U> promise;
        private final Function<? super T, ? extends U> function;
        private final T value;

        private ApplyRunnable(
                @Nonnull NanoPromise<U> promise,
                @Nonnull Function<? super T, ? extends U> function,
                @Nullable T value
        ) {
            this.promise = Objects.requireNonNull(promise);
            this.function = Objects.requireNonNull(function);
            this.value = value;
        }

        @Override
        public void run() {
            if (cancelled.get()) {
                return;
            }
            try {
                promise.complete(function.apply(value));
            } catch (Throwable t) {
                promise.completeExceptionally(t);
            }
        }
    }

    private final class ComposeRunnable<U> implements Runnable {
        private final NanoPromise<U> promise;
        private final Function<? super T, ? extends NanoPromise<U>> function;
        private final T value;
        private final boolean sync;

        private ComposeRunnable(
                @Nonnull NanoPromise<U> promise,
                @Nonnull Function<? super T, ? extends NanoPromise<U>> function,
                @Nullable T value,
                boolean sync
        ) {
            this.promise = Objects.requireNonNull(promise);
            this.function = Objects.requireNonNull(function);
            this.value = value;
            this.sync = sync;
        }

        @Override
        public void run() {
            if (cancelled.get()) {
                return;
            }
            try {
                NanoPromise<U> p = function.apply(value);
                if (p == null) {
                    promise.complete(null);
                } else {
                    if (sync) {
                        p.thenAcceptSync(promise::complete);
                    } else {
                        p.thenAcceptAsync(promise::complete);
                    }
                }
            } catch (Throwable t) {
                promise.completeExceptionally(t);
            }
        }
    }

    private final class ExceptionallyRunnable<U> implements Runnable {
        private final NanoPromise<U> promise;
        private final Function<Throwable, ? extends U> function;
        private final Throwable t;

        private ExceptionallyRunnable(
                @Nonnull NanoPromise<U> promise,
                @Nonnull Function<Throwable, ? extends U> function,
                @Nonnull Throwable t
        ) {
            this.promise = Objects.requireNonNull(promise);
            this.function = Objects.requireNonNull(function);
            this.t = Objects.requireNonNull(t);
        }

        @Override
        public void run() {
            if (cancelled.get()) {
                return;
            }
            try {
                promise.complete(function.apply(t));
            } catch (Throwable t) {
                promise.completeExceptionally(t);
            }
        }
    }
}
