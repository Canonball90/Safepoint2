package safepoint.two.core.initializers;

import safepoint.two.Safepoint;
import safepoint.two.core.Core;

import java.util.ArrayDeque;
import java.util.Queue;

import static safepoint.two.Safepoint.mc;

public class ThreadInitializer extends Core {

    private final ClientService clientService = new ClientService();
    private static final Queue<Runnable> clientProcesses = new ArrayDeque<>();

    public ThreadInitializer() {
        super("ThreadInitializer");
        clientService.setName("safepoint-client-thread");
        clientService.setDaemon(true);
        clientService.start();
    }

    public static class ClientService extends Thread {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (mc.world == null || mc.player == null) {

                        if (!clientProcesses.isEmpty()) {
                            clientProcesses.poll().run();
                        }

                        Safepoint.moduleInitializer.getModuleList().forEach(module -> {
                            try {
                                if (module.isEnabled()) {
                                    module.onThread();
                                }
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        });
                    }
                    else {
                        Thread.yield();
                    }

                } catch(Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    public void submit(Runnable in) {
        clientProcesses.add(in);
    }

    public ClientService getService() {
        return clientService;
    }
}
