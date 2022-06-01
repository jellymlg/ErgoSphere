package fanta.ergosphere.process;

public enum AppState {
    INSTALL   (0), // not installed
    INSTALLING(1), // installing
    START     (2), // stopped
    STARTING  (3), // starting
    STOP      (4), // running
    STOPPING  (5); // stopping

    private final int state;

    private AppState(int x) {
        state = x;
    }

    public int code() {
        return state;
    }
}