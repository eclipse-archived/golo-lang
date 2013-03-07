package gololang.concurrent.workers;

public interface WorkerFunction {

  public void apply(Object message);
}
