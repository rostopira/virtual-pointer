package ua.rostopira.virtualpointer;

import java.net.InetAddress;

/**
 * Singleton
 */
final public class S {
    private static S instance;
    public InetAddress IP;
    public static final int port = 6969;

    private S() {
        instance = this;
    }

    public static S get() {
        if (instance == null) {
            synchronized (S.class) {
                if (instance == null)
                    new S();
            }
        }
        return instance;
    }
}
