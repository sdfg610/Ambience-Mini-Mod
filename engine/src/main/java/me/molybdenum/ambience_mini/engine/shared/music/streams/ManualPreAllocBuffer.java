package me.molybdenum.ambience_mini.engine.shared.music.streams;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ManualPreAllocBuffer extends LazyPreAllocBuffer
{
    private final Lock lock = new ReentrantLock();
    private final Condition gotData = lock.newCondition();

    private final int bufferLoadTimeoutMillis;


    public ManualPreAllocBuffer(int bufferSize, int bufferLoadTimeoutMillis) {
        super(bufferSize);
        this.bufferLoadTimeoutMillis = bufferLoadTimeoutMillis;
    }


    public void writeToBuffer(byte[] data) {
        int dataLength = data.length;
        int freeSpace = bufferSize - bytesLoaded;
        if (dataLength > freeSpace)
            throw new RuntimeException("Tried to buffer '" + dataLength + "' bytes of data with '" + freeSpace + "' bytes of free space");

        lock.lock();
        try {
            System.arraycopy(
                    data, 0,
                    buffer, bytesLoaded,
                    dataLength
            );

            bytesLoaded += dataLength;
            gotData.signalAll();
        } finally {
            lock.unlock();
        }
    }


    @Override
    protected void loadBufferTo(int targetPosition) {
        lock.lock();
        try {
            while (bytesLoaded <= targetPosition) {
                // If 'bufferLoadTimeoutMillis' milliseconds elapse without any new data buffered. Give up.
                if (!gotData.await(bufferLoadTimeoutMillis, TimeUnit.MILLISECONDS))
                    break;
            }
        }
        catch (InterruptedException ignored) { }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void close() { }
}
