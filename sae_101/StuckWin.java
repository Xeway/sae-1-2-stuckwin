/**
 * StuckWin.java
 * Jeu créé lors de la SAE 101-102 pendant la S1 de 2022/2023
 * en BUT Informatique à l'IUT de Nord Franche-Comté.
 *
 * Le jeu se joue uniquement à l'aide d'un terminal.
 * A chaque partie commencée, un fichier CSV est créé et enregistre
 * l'historique des mouvements faits par les joueurs.
 *
 * @author HUMBERT Ewan <ewan.humbert@edu.univ-fcomte.fr>
 * @author SIOUAN Tom <tom.siouan@edu.univ-fcomte.fr>
 *
 * Classe : S1B2
 */

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;

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
        lcSource = lcSource.toUpperCase();
        lcDest = lcDest.toUpperCase();
        if (lcSource.length() != 2 || !Pattern.compile("[A-Z][0-9]").matcher(lcSource).find())
            return Result.EMPTY_SRC;
        if (lcDest.length() != 2 || !Pattern.compile("[A-Z][0-9]").matcher(lcDest).find())
            return Result.EXT_BOARD;

        int rowSrc = idLettreToInt(lcSource.charAt(0));
        int colSrc = Character.getNumericValue(lcSource.charAt(1));
        if (state[rowSrc][colSrc] != couleur) return Result.BAD_COLOR;
        if (
                rowSrc >= BOARD_SIZE ||
                colSrc >= SIZE ||
                state[rowSrc][colSrc] == '-'
        ) return Result.EMPTY_SRC;

        int rowDest = idLettreToInt(lcDest.charAt(0));
        int colDest = Character.getNumericValue(lcDest.charAt(1));
        if (
                rowDest >= BOARD_SIZE ||
                colDest >= SIZE ||
                state[rowDest][colDest] == '-'
        ) return Result.EXT_BOARD;
        if (state[rowDest][colDest] != VIDE) return Result.DEST_NOT_FREE;

        String[] possibleDestinations = possibleDests(couleur, rowSrc, colSrc);
        boolean isPossibleCase = false;
        for (int i = 0; i < possibleDestinations.length; i++) {
            if (possibleDestinations[i].equals(lcDest)) {
                isPossibleCase = true;
                break;
            }
        }
        if (!isPossibleCase) return Result.TOO_FAR;

        if (mode == ModeMvt.REAL) {
            char tmp = state[rowSrc][colSrc];
            state[rowSrc][colSrc] = state[rowDest][colDest];
            state[rowDest][colDest] = tmp;
            // on aurait aussi pu faire
            // state[rowSrc][colSrc] = VIDE
            // state[rowSrc][colSrc] = state[rowDest][colDest]
        }

        return Result.OK;
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

      destinations[0] = validCase(idLettre-c, idCol);
      destinations[1] = validCase(idLettre-c, idCol+c);
      destinations[2] = validCase(idLettre, idCol+c);

      return destinations;
    }

    /**
    * Convertit row et col en identifiant d'une case de la forme [A-G][1-7]
    * @param row
    * @param col
    * @return l'identifiant de la case
    */
    String validCase(int row, int col) {
      return Character.toString((char)(row+65)) + col;
    }

    /**
     * Affiche le plateau de jeu dans la configuration portée par
     * l'attribut d'état "state"
     */
    void affiche() {
      for (int k = SIZE - 1; k >= 0; k--) {
        // type tableau pour utiliser les passages par référence
        String[] row = {""};

        createRow(0, k, row);

        printRow(row[0], 0, k);
      }

      for (int l = 1; l < BOARD_SIZE; l++) {
        // type tableau pour utiliser les passages par référence
        String[] row = {""};

        createRow(l, 0, row);

        printRow(row[0], l, 0);
      }
    }

    /**
     * Parcours la diagonale commençant/passant par l'element [i][j] du tableau 'state'
     * et enregistre les éléments se trouvant sur la diagonale
     * @param i coordonnée hozirontale
     * @param j coordonnée verticale
     * @param row les élements se trouvant sur la diagonale
     */
    void createRow(int i, int j, String[] row) {
      row[0] += state[i][j];
      
      if (i < BOARD_SIZE - 1 && j < SIZE - 1) {
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
            result += ConsoleColors.RED_BACKGROUND + position + ConsoleColors.RESET + SPACE;
            break;
          case "B":
            result += ConsoleColors.BLUE_BACKGROUND + position + ConsoleColors.RESET + SPACE;
            break;
          case ".":
            result += ConsoleColors.BLACK + ConsoleColors.WHITE_BACKGROUND
                        + position + ConsoleColors.RESET + SPACE;
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
     * @return tableau contenant la position de départ et la destination du pion à jouer
     */
    String[] jouerIA(char couleur) {
      // votre code ici. Supprimer la ligne ci-dessous.
      throw new java.lang.UnsupportedOperationException("à compléter");
    }

    /**
     * Gère le jeu en fonction du joueur/couleur
     * @param couleur couleur du joueur jouant actuellement
     * @return tableau de deux chaînes {source, destination} des pions à jouer
     */
    String[] jouer(char couleur) {
        String src = "";
        String dst = "";

        System.out.println("Mouvement " + couleur);
        src = input.next();
        dst = input.next();
        System.out.println(src + "->" + dst);

        return new String[]{src, dst};
    }

    /**
     * Retourne 'R' ou 'B' si vainqueur, 'N' si partie pas finie
     * @param couleur couleur du prochain pion
     * @return le résultat ('R', 'B' ou 'N')
     */
    char finPartie(char couleur) {
        List<int[]> pions = new ArrayList<>();

        // on récupère tous les pions bleu ou bien rouge selon le paramètre 'couleur'
        int i = 0;
        while (i < BOARD_SIZE && pions.size() < 13) {
            int j = 0;
            while (j < SIZE && pions.size() < 13) {
                if (state[i][j] == couleur) {
                    pions.add(new int[]{i, j});
                }
                j++;
            }
            i++;
        }

        boolean canPlay = false;

        // avec chaque pion de couleur, on récupère les mouvements qu'il peut effectuer
        // si un des mouvements de ne serait-ce qu'un seul pion est valide
        // alors la partie n'est pas finie
        i = 0;
        while (i < pions.size() && !canPlay) {
            String[] possibleDestsPion = possibleDests(couleur, pions.get(i)[0], pions.get(i)[1]);
            for (i = 0; i < possibleDestsPion.length; i++) {
                int row = idLettreToInt(possibleDestsPion[i].charAt(0));
                int col = Character.getNumericValue(possibleDestsPion[i].charAt(1));

                if (state[row][col] == VIDE) {
                    canPlay = true;
                    break;
                }
            }
            i++;
        }

        return canPlay ? 'N' : couleur;
    }

    /**
     * Créé un nouveau fichier CSV et initialise dedans le titre de chaque colonne
     * @return le fichier CSV prêt à être utilisé
     */
    public static File createCSV() {
        int csvId = 0;
        String fileName;
        File f;

        // on trouve un numéro pas déjà utilisé pour XX dans StuckWin_XX.csv
        // afin de ne pas écrire dans un fichier qui existe déjà
        // et créer un tout nouveau fichier CSV vierge
        do {
            csvId++;
            fileName = "StuckWin_" + ((csvId < 10) ? ("0" + csvId) : (csvId)) + ".csv";
            f = new File(fileName);
        } while (f.isFile());

        try {
            PrintWriter csv = new PrintWriter(f);

            csv.println("Couleur,Mouvement source,Mouvement destination,Status");

            csv.close();
        } catch (IOException e) {
            System.out.println("Il y a eu une erreur pour l'écriture du CSV.");
            e.printStackTrace();
        }

        return f;
    }

    /**
     * Enregistre les données du mouvement en cours mises en paramètre dans le fichier CSV donné
     * @param f le fichier CSV à écrire dedans
     * @param couleur couleur ('B' ou 'R') du joueur ayant joué
     * @param src coordonnée du pion source choisie par le joueur
     * @param dest coordonnée du pion destination choisie par le joueur
     * @param status état du mouvement fait
     */
    public static void writeCSV(File f, char couleur, String src, String dest, Result status) {
        try {
            PrintWriter csv = new PrintWriter(new FileOutputStream(f, true));

            csv.printf("%c,%s,%s,%s\n", couleur, src, dest, status.toString());

            csv.close();
        } catch (IOException e) {
            System.out.println("Il y a eu une erreur pour l'écriture du CSV.");
            e.printStackTrace();
        }
    }

    /**
     * Ecrit à la fin du fichier CSV donné le gagnant de la partie
     * @param f le fichier CSV à écrire dedans
     * @param winner chaîne de caractères disant qui est le gagnant
     */
    public static void writeWinnerCSV(File f, String winner) {
        try {
            PrintWriter csv = new PrintWriter(new FileOutputStream(f, true));

            csv.print(winner);

            csv.close();
        } catch (IOException e) {
            System.out.println("Il y a eu une erreur pour l'écriture du CSV.");
            e.printStackTrace();
        }
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
        File csvFile = createCSV();

        // version console
        do {
              // séquence pour Bleu ou rouge
              jeu.affiche();
              do {
                  status = Result.EXIT;
                  reponse = jeu.jouer(curCouleur);
                  src = reponse[0];
                  dest = reponse[1];
                  if ("q".equals(src)) {
                      writeWinnerCSV(csvFile, "Partie interrompue");
                      return;
                  }
                  status = jeu.deplace(curCouleur, src, dest, ModeMvt.REAL);
                  partie = jeu.finPartie(nextCouleur);
                  writeCSV(csvFile, curCouleur, src, dest, status);
                  System.out.println("status : " + status + " partie : " + partie);
              } while (status != Result.OK && partie == 'N');
              tmp = curCouleur;
              curCouleur = nextCouleur;
              nextCouleur = tmp;
              cpt++;
        } while (partie == 'N'); // TODO affiche vainqueur
        String winnerResult = "Victoire : " + partie + " (" + (cpt/2) + " coups)";
        writeWinnerCSV(csvFile, winnerResult);
        System.out.printf(winnerResult);
    }
}
