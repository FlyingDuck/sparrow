package club.cookbean.sparrow.writer;


public interface WriteBehind extends CacheWriter {

  /**
   * Start the write behind queue
   *
   */
  void start();

  /**
   * Stop the coordinator and all the internal data structures.
   * <p>
   * This stops as quickly as possible without losing any previously added items. However, no guarantees are made
   * towards the processing of these items. It's highly likely that items are still inside the internal data structures
   * and not processed.
   */
  void stop();

  /**
   * Gets the best estimate for items in the queue still awaiting processing.
   *
   * @return the amount of elements still awaiting processing.
   */
  long getQueueSize();

}
