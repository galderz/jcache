package jcache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class Database {

   static final String[] BRAZIL_SQUAD_1982 = {
         "Valdir Peres",
         "Leandro", "Oscar", "Luisinho", "Junior",
         "Toninho", "Cerezzo", "Falcao",
         "Socrates", "Serginho", "Eder",
   };

   static FootballSquad squadPlayers(String key) {
      List<FootballPlayer> players = new ArrayList<FootballPlayer>();
      for (String name : BRAZIL_SQUAD_1982) {
         players.add(new FootballPlayer(name));
         // Slow down artificially
         for (int i = 0; i < 1000; i++)
            Collections.reverse(players);
      }

      return new FootballSquad(players);
   }

}
