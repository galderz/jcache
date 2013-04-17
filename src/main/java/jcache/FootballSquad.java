package jcache;

import java.util.List;

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class FootballSquad {

   private List<FootballPlayer> squad;

   public FootballSquad(List<FootballPlayer> squad) {
      this.squad = squad;
   }

   public List<FootballPlayer> getSquad() {
      return squad;
   }

   @Override
   public String toString() {
      return "FootballSquad{" +
            "squad=" + squad +
            '}';
   }

}
