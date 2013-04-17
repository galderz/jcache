package jcache;

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class FootballPlayer {

   private String name;

   public FootballPlayer(String name) {
      this.name = name;
   }

   public String getName() {
      return name;
   }

   @Override
   public String toString() {
      return "\nFootballPlayer{" +
            "name='" + name + '\'' +
            "}";
   }
}
