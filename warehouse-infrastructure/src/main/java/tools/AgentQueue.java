package tools;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

public class AgentQueue {

    public interface Command {
        void execute() throws Exception;
    }

    public interface CommandWithResult<T> extends Query<T> {
        T execute() throws Exception;
    }

    public interface Query<T> {
        T execute() throws Exception;
    }

    private final Queue<Runnable> tasks = new ConcurrentLinkedDeque<>();
    private final ReentrantLock lock = new ReentrantLock();

    public CompletionStage<?> command(Command command) {
        CompletableFuture<?> future = new CompletableFuture<>();
        tasks.offer(new CommandTask(future, command));
        execute();
        return future;
    }

    public <T> CompletionStage<T> command(CommandWithResult<T> command) {
        CompletableFuture<T> future = new CompletableFuture<>();
        tasks.offer(new QueryTask<>(future, command));
        execute();
        return future;
    }

    public <T> CompletionStage<T> query(Query<T> query) {
        CompletableFuture<T> future = new CompletableFuture<>();
        tasks.offer(new QueryTask<>(future, query));
        execute();
        return future;
    }

    public <T> T querySync(Query<T> query) {
        CompletableFuture<T> future = new CompletableFuture<>();
        tasks.offer(new QueryTask<>(future, query));
        execute();
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getCause());
        } catch (ExecutionException e) {
            throw e.getCause() instanceof RuntimeException
                    ? ((RuntimeException) e.getCause())
                    : new RuntimeException(e.getCause());
        }
    }

    private void execute() {
        if (lock.tryLock()) {
            try {
                while (!tasks.isEmpty()) {
                    Runnable task = tasks.poll();
                    task.run();
                }
            } finally {
                lock.unlock();
            }
            if (!tasks.isEmpty()) {
                execute();
            }
        }
    }

    private static class CommandTask implements Runnable {
        final CompletableFuture<?> future;
        final Command command;

        private CommandTask(CompletableFuture<?> future, Command command) {
            this.future = future;
            this.command = command;
        }

        @Override
        public void run() {
            try {
                command.execute();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }
    }

    private static class QueryTask<T> implements Runnable {
        final CompletableFuture<T> future;
        final Query<T> query;

        private QueryTask(CompletableFuture<T> future, Query<T> query) {
            this.future = future;
            this.query = query;
        }

        @Override
        public void run() {
            try {
                future.complete(query.execute());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }
    }
}
