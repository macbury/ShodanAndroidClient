package macbury.shodan.sync;


/** The state of a state machine defines the logic of the entities that enter, exit and last this state. Additionally, a state may
 * be delegated by an entity to handle its messages.
 *
 * @param <E> is the type of the entity handled by this state machine
 **/
public interface BasicState<T> {
  public void enter(T context);
  public void exit(T context);
}
