package me.molybdenum.ambience_mini.engine.client.core.networking.handlers;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.responses.Response;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SyncHandler implements Handler
{
    private final Lock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    private Response response;


    public @Nullable Response await(long timeoutMillis) {
        try {
            lock.lock();
            if (response == null)
                //noinspection ResultOfMethodCallIgnored
                cond.await(timeoutMillis, TimeUnit.MILLISECONDS); // Regardless of time-out or not, just return.
            return response;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }


    @Override
    public void handle(Response response) {
        try {
            lock.lock();
            this.response = response;
            cond.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
