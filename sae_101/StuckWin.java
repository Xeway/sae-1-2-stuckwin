import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StuckWin {
    static final Scanner input = new Scanner(System.in);
    private static final double BOARD_SIZE = 7;

    enum Result {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT}
    enum ModeMvt {REAL, SIMU}
    final char[] joueurs = {'B', 'R'};
    final int SIZE = 8;
    final char VIDE = '.';
    // 'B'=bleu 'R'=rouge '.'=vide '-'=n'existe pas
    char[][] state = {
            {'-', '-', '-', '-', 'R', 'R', 'R', 'R'},
            {'-', '-', '-', '.', 'R', 'R', 'R', 'R'},
            {'-', '-', '.', '.', '.', 'R', 'R', 'R'},
            {'-', 'B', 'B', '.', '.', '.', 'R', 'R'},
            {'-', 'B', 'B', 'B', '.', '.', '.', '-'},
            {'-', 'B', 'B', 'B', 'B', '.', '-', '-'},
            {'-', 'B', 'B', 'B', 'B', '-', '-', '-'},
    };
    final String SPACE = "  ";

    /**
     * Déplace un pion ou simule son déplacement
     * @param couleur couleur du pion à déplacer
     * @param lcSource case source Lc
     * @param lcDest case destination Lc
     * @param mode ModeMVT.REAL/SIMU selon qu'on réalise effectivement le déplacement ou qu'on le simule seulement.
     * @return enum {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT} selon le déplacement
     */
    Result deplace(char couleur, String lcSource, String lcDest,  ModeMvt mode) {
      // votre code ici. Supprimer la ligne ci-dessous.
      throw new java.lang.UnsupportedOperationException("à compléter");
    }

    /**
     * Construit les trois chaînes représentant les positions accessibles
     * à partir de la position de départ [idLettre][idCol].
     * @param couleur couleur du pion à jouer
     * @param idLettre id de la ligne du pion à jouer
     * @param idCol id de la colonne du pion à jouer
     * @return tableau des trois positions jouables par le pion (redondance possible sur les bords)
     */
    String[] possibleDests(char couleur, int idLettre, int idCol){
      // votre code ici. Supprimer la ligne ci-dessous.
      throw new java.lang.UnsupportedOperationException("à compléter");
    }

    /**
     * Affiche le plateau de jeu dans la configuration portée par
     * l'attribut d'état "state"
     */
    void affiche() {
      for (int k = state[0].length - 1; k >= 0; k--) {
        // type tableau pour utiliser les passages par référence
        String[] row = {""};

        createRow(0, k, row);

        printRow(row[0], 0, k);
      }

      for (int l = 1; l < state.length; l++) {
        // type tableau pour utiliser les passages par référence
        String[] row = {""};

        createRow(l, 0, row);

        printRow(row[0], l, 0);
      }
    }

    /**
     * Parcours la diagonale commençant/passant par l'element [i][j] du tableau 'state' et enregistre les éléments se trouvant sur la diagonale
     * @param i coordonnée hozirontale
     * @param j coordonnée verticale
     * @param row les élements se trouvant sur la diagonale
     */
    void createRow(int i, int j, String[] row) {
      row[0] += state[i][j];
      
      if (i < state.length - 1 && j < state[i].length - 1) {
        createRow(i+1, j+1, row);
      }
    }

    /**
     * Affiche de manière claire, ordonnée et coloriée une ligne du jeu
     * @param row une ligne du jeu
     * @param lettre nombre servant à afficher les coordonnées de la case (sa lettre)
     * @param chiffre nombre servant à afficher les coordonées de la case (son chiffre)
     */
    void printRow(String row, int lettre, int chiffre) {
      String[] characters = row.split("");

      String result = "";

      for (int i = 0; i < characters.length; i++) {
        String position = Character.toString((char)(65+lettre+i)) + (chiffre+i);

        // on définit la couleur pour chaque case
        switch (characters[i]) {
          case "R":
            result += ConsoleColors.RED_BACKGROUND_BRIGHT + position + ConsoleColors.RESET + SPACE;
            break;
          case "B":
            result += ConsoleColors.BLUE_BACKGROUND_BRIGHT + position + ConsoleColors.RESET + SPACE;
            break;
          case ".":
            result += ConsoleColors.WHITE_BACKGROUND_BRIGHT + position + ConsoleColors.RESET + SPACE;
            break;
          default:
            result += "";
            break;
        }
      }

      int nbCasesSurLigne = result.split(SPACE).length;
      result = SPACE.repeat(4 - nbCasesSurLigne) + result;

      System.out.println(result);
    }

    /**
     * Joue un tour
     * @param couleur couleur du pion à jouer
     * @return tableau contenant la position de départ et la destination du pion à jouer.
     */
    String[] jouerIA(char couleur) {
      // votre code ici. Supprimer la ligne ci-dessous.
      throw new java.lang.UnsupportedOperationException("à compléter");
    }

    /**
     * gère le jeu en fonction du joueur/couleur
     * @param couleur
     * @return tableau de deux chaînes {source,destination} du pion à jouer
     */
    String[] jouer(char couleur){
        String src = "";
        String dst = "";
        String[] mvtIa;
        switch(couleur) {
            case 'B':
                System.out.println("Mouvement " + couleur);
                src = input.next();
                dst = input.next();
                System.out.println(src + "->" + dst);
                break;
            case 'R':
                System.out.println("Mouvement " + couleur);
                mvtIa = jouerIA(couleur);
                src = mvtIa[0];
                dst = mvtIa[1];
                System.out.println(src + "->" + dst);
                break;
        }
        return new String[]{src, dst};
    }

    /**
     * retourne 'R' ou 'B' si vainqueur, 'N' si partie pas finie
     * @param couleur
     * @return
     */
    char finPartie(char couleur){
      // votre code ici. Supprimer la ligne ci-dessous.
      throw new java.lang.UnsupportedOperationException("à compléter");
    }


    public static void main(String[] args) {
        StuckWin jeu = new StuckWin();
        String src = "";
        String dest;
        String[] reponse;
        Result status;
        char partie = 'N';
        char curCouleur = jeu.joueurs[0];
        char nextCouleur = jeu.joueurs[1];
        char tmp;
        int cpt = 0;

        // version console
        do {
              // séquence pour Bleu ou rouge
              jeu.affiche();
              do {
                  status = Result.EXIT;
                  reponse = jeu.jouer(curCouleur);
                  src = reponse[0];
                  dest = reponse[1];
                  if("q".equals(src))
                      return;
                  status = jeu.deplace(curCouleur, src, dest, ModeMvt.REAL);
                  partie = jeu.finPartie(nextCouleur);
                  System.out.println("status : "+status + " partie : " + partie);
              } while(status != Result.OK && partie=='N');
              tmp = curCouleur;
              curCouleur = nextCouleur;
              nextCouleur = tmp;
              cpt ++;
        } while(partie =='N'); // TODO affiche vainqueur
        System.out.printf("Victoire : " + partie + " (" + (cpt/2) + " coups)");
    }
}
