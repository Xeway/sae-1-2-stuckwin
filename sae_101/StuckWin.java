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
    Result deplace(char couleur, String lcSource, String lcDest, ModeMvt mode) {
        int rowSrc = idLettreToInt(lcSource.charAt(0));
        int colSrc = Character.getNumericValue(lcSource.charAt(1));

        String[] possibleDestinations = possibleDests(couleur, rowSrc, colSrc);

        int rowDest = idLettreToInt(lcDest.charAt(0));
        int colDest = Character.getNumericValue(lcDest.charAt(1));

        if (state[rowSrc][colSrc] != couleur) return Result.BAD_COLOR;
        if (rowSrc >= state.length || colSrc >= state[rowSrc].length || state[rowSrc][colSrc] == '-') return Result.EMPTY_SRC;
        if (rowDest >= state.length || colDest >= state[rowDest].length || state[rowDest][colDest] == '-') return Result.EXT_BOARD;
        if (state[rowDest][colDest] != '.') return Result.DEST_NOT_FREE;
        boolean isPossibleCase = false;
        for (int i = 0; i < possibleDestinations.length; i++) {
            if (possibleDestinations[i] == lcDest) {
                isPossibleCase = true;
                break;
            }
        }
        if (!isPossibleCase) return Result.TOO_FAR;

        if (mode == ModeMvt.REAL) {
            tmp = state[rowSrc][colSrc];
            state[rowSrc][colSrc] = state[rowDest][colDest];
            state[rowDest][colDest] = tmp;
            // on aurait aussi pu faire
            // state[rowSrc][colSrc] = '.'
            // state[rowSrc][colSrc] = state[rowDest][colDest]

            return Result.OK;
        } else {
            return Result.OK;
        }
    }

    /**
     * Convertit une lettre en son numéro (ex : A => 1, C => 3)
     * @param idLettre
     * @return un entier, le numéro de la lettre
     */
    int idLettreToInt(char idLettre) {
        return (int)(idLettre) - 65;
    }

    /**
     * Construit les trois chaînes représentant les positions accessibles
     * à partir de la position de départ [idLettre][idCol].
     * @param couleur couleur du pion à jouer
     * @param idLettre id de la ligne du pion à jouer
     * @param idCol id de la colonne du pion à jouer
     * @return tableau des trois positions jouables par le pion (redondance possible sur les bords)
     */
    String[] possibleDests(char couleur, int idLettre, int idCol) {
      String[] destinations = new String[3];

      int c = (couleur == joueurs[1]) ? -1 : 1;

      destinations[0] = validCase(idLettre-(1*c), idCol);
      destinations[1] = validCase(idLettre-(1*c), idCol+(1*c));
      destinations[2] = validCase(idLettre, idCol+(1*c));

      return destinations;
    }

    /**
    * Vérifie si la case sélectionée (state[row][col]) peut être jouée par le joueur
    * @param row
    * @param col
    * @return la destination de la case si jouable, sinon une chaine vide
    */
    String validCase(int row, int col) {
      return Integer.toString((char)(row+65)) + col;
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
