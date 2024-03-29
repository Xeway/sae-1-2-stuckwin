/**
 * StuckWin.java
 * Jeu cree lors de la SAE 101-102 pendant la S1 de 2022/2023
 * en BUT Informatique a l'IUT de Nord Franche-Comte.
 *
 * Le jeu se joue uniquement a l'aide d'un terminal.
 * A chaque partie commencee, un fichier CSV est cree et enregistre
 * l'historique des mouvements faits par les joueurs.
 *
 * NOTE : nous tenions a vous informer d'un bug (plus un probleme de design)
 * dans le code squelette de StuckWin.java fournit.
 * En effet, quand l'adversaire est sur le point de gagner,
 * si l'on fait un mouvement invalide, on ne peut pas recommencer
 * un nouveau mouvement et le jeu s'arrete en affichant le vainqueur.
 * Le probleme vient des conditions dans le while de la fonction 'main'
 * Nous n'avons pas ose le corriger.
 *
 * NOTE2 : En plus de faire la commande "java StuckWin 1" ou
 * "java StuckWin 2", vous pouvez egalement faire
 * "java StuckWin 3" pour faire jouer les deux algorithmes
 * l'un contre l'autre
 *
 * @author HUMBERT Ewan <ewan.humbert@edu.univ-fcomte.fr>
 * @author SIOUAN Tom <tom.siouan@edu.univ-fcomte.fr>
 *
 * Classe : S1B2
 */

import java.io.FileOutputStream;
import java.util.Collections;
import java.security.SecureRandom;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;

public class StuckWin {
    static final Scanner input = new Scanner(System.in);
    private static final double BOARD_SIZE = 7;

    enum Result {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT}
    enum ModeMvt {REAL, SIMU}
    final char[] joueurs = {'B', 'R'};
    static final int SIZE = 8;
    static final char VIDE = '.';
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
    static final SecureRandom rand = new SecureRandom();
    static final String SPACE = "  ";
    static final String CSV_ERROR = "Il y a eu une erreur pour l'ecriture du CSV.";
    static final int NUMBER_OF_SIMULATIONS = 500;

    /**
     * Deplace un pion ou simule son deplacement
     * @param couleur couleur du pion a deplacer
     * @param lcSource case source Lc
     * @param lcDest case destination Lc
     * @param mode ModeMVT.REAL/SIMU selon qu'on realise effectivement le deplacement ou qu'on le simule seulement.
     * @return enum {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT} selon le deplacement
     */
    Result deplace(char couleur, String lcSource, String lcDest, ModeMvt mode) {
        lcSource = lcSource.toUpperCase();
        lcDest = lcDest.toUpperCase();
        if (
            lcSource.length() != 2 ||
            !Pattern.compile("[A-G][1-7]").matcher(lcSource).find() ||
            lcDest.length() != 2 ||
            !Pattern.compile("[A-G][1-7]").matcher(lcDest).find()
        ) return Result.EXT_BOARD;

        int rowSrc = idLettreToInt(lcSource.charAt(0));
        int colSrc = Character.getNumericValue(lcSource.charAt(1));
        if (
            rowSrc >= BOARD_SIZE ||
            colSrc >= SIZE ||
            state[rowSrc][colSrc] == '-'
        ) return Result.EXT_BOARD;
        if (state[rowSrc][colSrc] == VIDE) return Result.EMPTY_SRC;
        if (state[rowSrc][colSrc] != couleur) return Result.BAD_COLOR;

        if (lcSource.equals(lcDest)) return Result.DEST_NOT_FREE;

        String[] possibleDestinations = possibleDests(couleur, rowSrc, colSrc);
        boolean isPossibleCase = false;
        for (int i = 0; i < possibleDestinations.length; i++) {
            if (possibleDestinations[i].equals(lcDest)) {
                isPossibleCase = true;
                break;
            }
        }
        if (!isPossibleCase) return Result.TOO_FAR;

        int rowDest = idLettreToInt(lcDest.charAt(0));
        int colDest = Character.getNumericValue(lcDest.charAt(1));
        if (
            rowDest >= BOARD_SIZE ||
            colDest >= SIZE ||
            state[rowDest][colDest] == '-'
        ) return Result.EXT_BOARD;
        if (state[rowDest][colDest] != VIDE) return Result.DEST_NOT_FREE;

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
     * Convertit une lettre en son numero (ex : A => 1, C => 3)
     * @param idLettre
     * @return un entier, le numero de la lettre
     */
    public static int idLettreToInt(char idLettre) {
        return idLettre - 65;
    }

    /**
     * Construit les trois chaines representant les positions accessibles
     * (mais pas forcement valides) a partir de la position de depart [idLettre][idCol].
     * @param couleur couleur du pion a jouer
     * @param idLettre id de la ligne du pion a jouer
     * @param idCol id de la colonne du pion a jouer
     * @return tableau des trois positions jouables par le pion (redondance possible sur les bords)
     */
    String[] possibleDests(char couleur, int idLettre, int idCol) {
      String[] destinations = new String[3];

      // selon la couleur du pion, on va chercher les mouvements possibles
      // "en bas" (si rouge) ou "en haut" (si bleu) du pion
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
    public static String validCase(int row, int col) {
      return Character.toString((char)(row+65)) + col;
    }

    /**
     * Affiche le plateau de jeu dans la configuration portee par
     * l'attribut d'etat "state"
     */
    void affiche() {
      for (int k = SIZE - 1; k >= 0; k--) {
        // type tableau pour utiliser les passages par reference
        String[] row = {""};

        createRow(0, k, row);

        printRow(row[0], 0, k);
      }

      for (int l = 1; l < BOARD_SIZE; l++) {
        // type tableau pour utiliser les passages par reference
        String[] row = {""};

        createRow(l, 0, row);

        printRow(row[0], l, 0);
      }
    }

    /**
     * Parcours la diagonale commencant/passant par l'element [i][j] du tableau 'state'
     * et enregistre les elements se trouvant sur la diagonale
     * @param i coordonnee hozirontale
     * @param j coordonnee verticale
     * @param row les elements se trouvant sur la diagonale
     */
    void createRow(int i, int j, String[] row) {
      row[0] += state[i][j];
      
      if (i < BOARD_SIZE - 1 && j < SIZE - 1) {
        createRow(i+1, j+1, row);
      }
    }

    /**
     * Affiche de maniere claire, ordonnee et coloriee une ligne du jeu
     * @param row une ligne du jeu
     * @param lettre nombre servant a afficher les coordonnees de la case (sa lettre)
     * @param chiffre nombre servant a afficher les coordonees de la case (son chiffre)
     */
    void printRow(String row, int lettre, int chiffre) {
      String[] characters = row.split("");

      StringBuilder result = new StringBuilder("");

      for (int i = 0; i < characters.length; i++) {
        String position = Character.toString((char)(65+lettre+i)) + (chiffre+i);

        // on definit la couleur pour chaque case
        switch (characters[i]) {
          case "R":
            result.append(ConsoleColors.RED_BACKGROUND + position
                                   + ConsoleColors.RESET + SPACE);
            break;
          case "B":
            result.append(ConsoleColors.BLUE_BACKGROUND + position
                                   + ConsoleColors.RESET + SPACE);
            break;
          case ".":
            result.append(ConsoleColors.BLACK + ConsoleColors.WHITE_BACKGROUND
                          + position + ConsoleColors.RESET + SPACE);
            break;
          default:
            result.append("");
            break;
        }
      }

      // on ajoute des espaces a gauche pour centrer les pions a l'affichage
      int nbCasesSurLigne = result.toString().split(SPACE).length;

      System.out.println(String.join("", Collections.nCopies(4 - nbCasesSurLigne, SPACE))
                         + result.toString());
    }

    /**
     * Joue un tour
     * @param couleur couleur du pion a jouer
     * @param typeIA type d'IA a jouer (1, 2 ou 3)
     * @return tableau contenant la position de depart et la destination du pion a jouer
     */
    String[] jouerIA(char couleur, String typeIA) {
        switch (typeIA) {
            case "1":
                return jouerIANaive(couleur);
            case "2":
                return jouerIAMCTS(couleur);
            default:
                System.out.println("Erreur : Choix de l'IA invalide "
                                 + "(1 pour IA naive, 2 pour IA MCTS, 3 pour IA naive vs MCTS).");
                System.exit(0);
                return new String[]{"", ""};
        }
    }

    /**
     * Choisis de maniere aleatoire parmi les mouvements possibles un pion a deplacer
     * @param couleur couleur des pions que l'IA doit jouer
     * @return tableau contenant la position de depart et la destination du pion a jouer
     */
    String[] jouerIANaive(char couleur) {
        ArrayList<int[][]> possibleMoves = getAllPossibleMoves(state, couleur);

        // genere un entier aleatoire entre [0-possibleMoves.size()[
        int r = rand.nextInt(possibleMoves.size());

        int[][] randMove = possibleMoves.get(r);

        return new String[]{
            validCase(randMove[0][0], randMove[0][1]),
            validCase(randMove[1][0], randMove[1][1])
        };
    }

    /**
     * Joue un tour grace a l'algorithme Monte Carlo tree search
     * @param couleur couleur des pions
     * @return tableau contenant la position de depart et la destination du pion a jouer
     */
    String[] jouerIAMCTS(char couleur) {
        HashMap<String, Integer> evaluations = evaluateNodes(couleur);

        int[][] bestMove = getBestMove(evaluations);

        return new String[]{
            validCase(bestMove[0][0], bestMove[0][1]),
            validCase(bestMove[1][0], bestMove[1][1])
        };
    }

    /**
     * Simule avec des mouvements aleatoires NUMBER_OF_SIMULATIONS parties
     * et evalue avec un score chaque simulation (+ gros score = meilleur)
     * Cela correspond aux 3 premieres etapes mentionnees dans le rapport
     * @param couleur couleur des pions
     * @return les scores pour chaque simulation faite
     */
    HashMap<String, Integer> evaluateNodes(char couleur) {
        HashMap<String, Integer> evaluations = new HashMap<>();

        for (int i = 0; i < NUMBER_OF_SIMULATIONS; i++) {
            char player = couleur;

            char[][] stateCopy = new char[(int)BOARD_SIZE][];
            for (int x = 0; x < BOARD_SIZE; x++) {
                stateCopy[x] = state[x].clone();
            }

            int score = 999;

            ArrayList<int[][]> possibleMoves = getAllPossibleMoves(stateCopy, player);

            boolean firstIteration = true;
            int[][] firstMove = new int[2][2];

            while (!possibleMoves.isEmpty()) {
                // genere un entier aleatoire entre [0-possibleMoves.size()[
                int r = rand.nextInt(possibleMoves.size());

                int[][] randMove = possibleMoves.get(r);

                // src
                stateCopy[randMove[0][0]][randMove[0][1]] = VIDE;
                // dest
                stateCopy[randMove[1][0]][randMove[1][1]] = player;

                if (firstIteration) {
                    firstMove = randMove;
                }

                score--;

                player = (player == joueurs[1]) ? joueurs[0] : joueurs[1];
                possibleMoves = getAllPossibleMoves(stateCopy, player);

                firstIteration = false;
            }

            String firstMoveKey = arrToStr(firstMove);

            if (player != couleur && finPartie(stateCopy, player) == player) {
                score = -Math.abs(score);
            }

            if (evaluations.containsKey(firstMoveKey)) {
                evaluations.put(firstMoveKey, evaluations.get(firstMoveKey) + score);
            } else {
                evaluations.put(firstMoveKey, score);
            }
        }

        return evaluations;
    }

    /**
     * Choisit le meilleur mouvement possible en fonction
     * des scores de chaque simulation dans le parametre "evaluations"
     * Cela correspond a la 4eme etape mentionnee dans le rapport
     * @param evaluations les scores de chaque simulation
     * @return les coordonnees du meilleur mouvement
     */
    int[][] getBestMove(HashMap<String, Integer> evaluations) {
        String bestMoveKey = "";
        int bestScore = 0;
        boolean firstRound = true;

        for (Map.Entry<String, Integer> entry : evaluations.entrySet()) {
            if (firstRound || entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestMoveKey = entry.getKey();
                firstRound = false;
            }
        }

        return strToArr(bestMoveKey);
    }

    /**
     * Convertit un tableau 2D d'entiers en chaine de caracteres
     * Ex : [[4, 5], [8, 1]] -> "4-5/8-1"
     * @param arr tableau a convertir
     * @return la chaine de caracteres
     */
    public static String arrToStr(int[][] arr) {
        StringBuilder result = new StringBuilder("");

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                result.append(arr[i][j]);
                if (j < arr[i].length - 1) {
                    result.append("-");
                }
            }

            if (i < arr.length - 1) {
                result.append("/");
            }
        }

        return result.toString();
    }

    /**
     * Convertit une chaine de caracteres en un tableau 2D d'entiers
     * @param str chaine a convertir
     * @return le tableau 2D d'entiers
     */
    public static int[][] strToArr(String str) {
        int[][] result;

        String[] rows = str.split("/");
        result = new int[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            String[] elements = rows[i].split("-");

            result[i] = new int[elements.length];
            for (int j = 0; j < elements.length; j++) {
                result[i][j] = Integer.parseInt(elements[j]);
            }
        }

        return result;
    }

    /**
     * Recupere et retourne tous les mouvements possibles avec les pions de couleur
     * @param currentState etat de la partie
     * @param couleur couleur des pions
     * @return une liste contenant tous les mouvements possibles (pions src et dest)
     */
    ArrayList<int[][]> getAllPossibleMoves(char[][] currentState, char couleur) {
        int[][] pions = new int[13][2];
        getPions(currentState, pions, couleur);

        ArrayList<int[][]> possibleMoves = new ArrayList<>();

        for (int i = 0; i < pions.length; i++) {
            String[] possibleDestsPion = possibleDests(couleur, pions[i][0], pions[i][1]);
            for (int j = 0; j < possibleDestsPion.length; j++) {
                int row = idLettreToInt(possibleDestsPion[j].charAt(0));
                int col = Character.getNumericValue(possibleDestsPion[j].charAt(1));

                if (
                    row >= 0 && col >= 0 &&
                    row < BOARD_SIZE && col < SIZE &&
                    currentState[row][col] == VIDE
                ) {
                    int[][] p = new int[2][2];
                    // source
                    p[0][0] = pions[i][0];
                    p[0][1] = pions[i][1];
                    // dest
                    p[1][0] = row;
                    p[1][1] = col;

                    possibleMoves.add(p);
                }
            }
        }

        return possibleMoves;
    }

    /**
     * Gere le jeu en fonction du joueur/couleur
     * @param couleur couleur du joueur jouant actuellement
     * @param typeIA type d'IA a jouer (1, 2 ou 3)
     * @return tableau de deux chaines {source, destination} des pions a jouer
     */
    String[] jouer(char couleur, String typeIA) {
        String src = "";
        String dst = "";
        String[] mvtIa;

        switch(couleur) {
            case 'B':
                System.out.println("Mouvement " + couleur);
                if (typeIA.equals("3")) {
                    mvtIa = jouerIA(couleur, "1");
                    src = mvtIa[0];
                    dst = mvtIa[1];
                } else {
                    src = input.next();
                    dst = input.next();
                }
                System.out.println(src + "->" + dst);
                break;
            case 'R':
                System.out.println("Mouvement " + couleur);
                mvtIa = jouerIA(couleur, typeIA.equals("3") ? "2" : typeIA);
                src = mvtIa[0];
                dst = mvtIa[1];
                System.out.println(src + "->" + dst);
                break;
            default:
                System.out.println("ERREUR COULEUR");
                break;
        }

        return new String[]{src, dst};
    }

    /**
     * Retourne 'R' ou 'B' si vainqueur, 'N' si partie pas finie
     * @param currentState etat de la partie
     * @param couleur couleur du prochain pion
     * @return le resultat ('R', 'B' ou 'N')
     */
    char finPartie(char[][] currentState, char couleur) {
        int[][] pions = new int[13][2];

        getPions(currentState, pions, couleur);

        boolean canPlay = checkCanPlay(currentState, pions, couleur);

        return canPlay ? 'N' : couleur;
    }

    /**
     * Recupere les coordonnees de tous les pions bleu ou bien rouge selon le
     * parametre 'couleur' et les mets dans le tableau 'pions' donne en parametre
     * @param currentState etat de la partie
     * @param pions tableau qui va contenir les coordonnees des pions
     * @param couleur couleur des pions a recuperer
     */
    void getPions(char[][] currentState, int[][] pions, char couleur) {
        int index = 0;

        int i = 0;
        while (i < BOARD_SIZE && index < pions.length) {
            int j = 0;
            while (j < SIZE && index < pions.length) {
                if (currentState[i][j] == couleur) {
                    pions[index][0] = i;
                    pions[index][1] = j;
                    index++;
                }
                j++;
            }
            i++;
        }
    }

    /**
     * Parcours chaque pion de couleur et recupere les mouvements possibles
     * que celui-ci peut faire
     * Si un des mouvements de ne serait-ce qu'un seul pion est valide
     * alors la partie n'est pas finie donc renvoie true
     * @param currentState etat de la partie
     * @param pions tableau contenant les coordonnees des pions de couleur
     * @param couleur couleur des pions
     * @return vrai si un des pions de couleur peut encore jouer/bouger, faux sinon
     */
    boolean checkCanPlay(char[][] currentState, int[][] pions, char couleur) {
        boolean canPlay = false;

        int i = 0;
        while (i < pions.length && !canPlay) {
            String[] possibleDestsPion = possibleDests(couleur, pions[i][0], pions[i][1]);
            for (int j = 0; j < possibleDestsPion.length; j++) {
                int row = idLettreToInt(possibleDestsPion[j].charAt(0));
                int col = Character.getNumericValue(possibleDestsPion[j].charAt(1));

                if (
                    row >= 0 && col >= 0 &&
                    row < BOARD_SIZE && col < SIZE &&
                    currentState[row][col] == VIDE
                ) {
                    canPlay = true;
                    break;
                }
            }
            i++;
        }

        return canPlay;
    }

    /**
     * Cree un nouveau fichier CSV et initialise dedans le titre de chaque colonne
     * @return le fichier CSV pret a etre utilise
     */
    public static File createCSV() {
        int csvId = 0;
        String fileName;
        File f;

        // on trouve un numero pas deja utilise pour XX dans StuckWin_XX.csv
        // afin de ne pas ecrire dans un fichier qui existe deja
        // et creer un tout nouveau fichier CSV vierge
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
            System.out.println(CSV_ERROR);
        }

        return f;
    }

    /**
     * Enregistre les donnees du mouvement en cours mises en parametre dans le fichier CSV donne
     * @param f le fichier CSV a ecrire dedans
     * @param couleur couleur ('B' ou 'R') du joueur ayant joue
     * @param src coordonnee du pion source choisie par le joueur
     * @param dest coordonnee du pion destination choisie par le joueur
     * @param status etat du mouvement fait
     */
    public static void writeCSV(File f, char couleur, String src, String dest, Result status) {
        try {
            PrintWriter csv = new PrintWriter(new FileOutputStream(f, true));

            csv.printf("%c,%s,%s,%s%n", couleur, src, dest, status.toString());

            csv.close();
        } catch (IOException e) {
            System.out.println(CSV_ERROR);
        }
    }

    /**
     * Ecrit a la fin du fichier CSV donne le gagnant de la partie
     * @param f le fichier CSV a ecrire dedans
     * @param winner chaine de caracteres disant qui est le gagnant
     */
    public static void writeWinnerCSV(File f, String winner) {
        try {
            PrintWriter csv = new PrintWriter(new FileOutputStream(f, true));

            csv.print(winner);

            csv.close();
        } catch (IOException e) {
            System.out.println(CSV_ERROR);
        }
    }

    public static void main(String[] args) {
        StuckWin jeu = new StuckWin();
        String src = "";
        String dest;
        String[] reponse;
        Result status;
        char partie;
        char curCouleur = jeu.joueurs[0];
        char nextCouleur = jeu.joueurs[1];
        char tmp;
        int cpt = 0;
        File csvFile = createCSV();

        // version console
        do {
              // sequence pour Bleu ou rouge
              jeu.affiche();
              do {
                  reponse = jeu.jouer(curCouleur, args[0]);
                  src = reponse[0];
                  dest = reponse[1];
                  if ("q".equals(src)) {
                      writeWinnerCSV(csvFile, "Partie interrompue");
                      return;
                  }
                  status = jeu.deplace(curCouleur, src, dest, ModeMvt.REAL);
                  partie = jeu.finPartie(jeu.state, nextCouleur);
                  writeCSV(csvFile, curCouleur, src, dest, status);
                  System.out.println("status : " + status + " partie : " + partie);
              } while (status != Result.OK && partie == 'N');
              tmp = curCouleur;
              curCouleur = nextCouleur;
              nextCouleur = tmp;
              cpt++;
        } while (partie == 'N');
        String winnerResult = "Victoire : " + partie + " (" + (cpt/2) + " coups)";
        writeWinnerCSV(csvFile, winnerResult);
        System.out.printf(winnerResult);
    }
}
