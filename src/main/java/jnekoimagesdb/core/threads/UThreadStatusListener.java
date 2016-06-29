package jnekoimagesdb.core.threads;

public interface UThreadStatusListener {
    public void OnThreadCreated(String id);
    public void OnThreadPaused(String id);
    public void OnThreadStarted(String id);
    public void OnThreadBroken(String id);
    public void OnThreadError(String id);
    public void OnThreadStop(String id);
    public void OnThreadStatusReport(String id, long executeTime, long upTime);
    public void OnThreadLETDetected(String id);
}
