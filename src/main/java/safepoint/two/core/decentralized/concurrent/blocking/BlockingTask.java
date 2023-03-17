package safepoint.two.core.decentralized.concurrent.blocking;

import safepoint.two.core.decentralized.concurrent.task.Task;

public interface BlockingTask extends Task<BlockingContent> {
    @Override
    void invoke(BlockingContent unit);
}
