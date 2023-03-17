package safepoint.two.core.decentralized.concurrent.task;

import safepoint.two.core.decentralized.concurrent.utils.Syncer;

abstract class Syncable implements Runnable {
    protected Syncer syncer;
}
