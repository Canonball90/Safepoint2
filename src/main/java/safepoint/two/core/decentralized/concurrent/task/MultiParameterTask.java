package safepoint.two.core.decentralized.concurrent.task;

import java.util.List;

/**
 * Created by B_312 on 05/01/2021
 */
public interface MultiParameterTask<T> {
    void invoke(List<T> valueIn);
}
