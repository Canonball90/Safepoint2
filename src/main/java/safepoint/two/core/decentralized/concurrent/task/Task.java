package safepoint.two.core.decentralized.concurrent.task;

/**
 * Created by B_312 on 05/01/2021
 */
public interface Task<T> {
    void invoke(T valueIn);
}
