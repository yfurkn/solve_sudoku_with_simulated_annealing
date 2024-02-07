import java.util.*;

public class Main {

    private static final int SIZE = 9; // Size of the Sudoku grid

    /////////////////////////////////////////  FİLL RANDOM /////////////////////////////////////////////////////////////

    private static boolean fillSudoku(int[][] board) {
        for (int row = 0; row < SIZE; row += 3) {
            for (int col = 0; col < SIZE; col += 3) {
                if (!fillBlock(board, row, col)) {
                    return false; // If a block cannot be filled, return false
                }
            }
        }
        return true; // All blocks filled successfully
    }

    private static boolean fillBlock(int[][] board, int startRow, int startCol) {
        List<Integer> availableNumbers = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) {
            availableNumbers.add(i);
        }
        Collections.shuffle(availableNumbers); // Shuffle the list to get numbers randomly

        for (int row = startRow; row < startRow + 3; row++) {
            for (int col = startCol; col < startCol + 3; col++) {
                if (board[row][col] == 0) {
                    for (int numIndex = 0; numIndex < availableNumbers.size(); numIndex++) {
                        int number = availableNumbers.get(numIndex);
                        if (!isNumberInBlock(board, startRow, startCol, number)) {
                            board[row][col] = number;
                            availableNumbers.remove(numIndex);
                            break;
                        }
                    }
                }
            }
        }
        return true; // The block is filled successfully
    }

    private static boolean isNumberInBlock(int[][] board, int startRow, int startCol, int number) {
        for (int row = startRow; row < startRow + 3; row++) {
            for (int col = startCol; col < startCol + 3; col++) {
                if (board[row][col] == number) {
                    return true;
                }
            }
        }
        return false;
    }


    ///////////////////////////////////////// CALCULATE FAULT SCORE  /////////////////////////////////////////////////
    private static int calculateFaultScore(int[][] board) {
        int faultScore = 0;
        Set<Integer> seen;

        // Check rows for duplicates
        for (int row = 0; row < SIZE; row++) {
            seen = new HashSet<>();
            for (int col = 0; col < SIZE; col++) {
                int number = board[row][col];
                if (number != 0 && !seen.add(number)) {
                    faultScore++;
                }
            }
        }

        // Check columns for duplicates
        for (int col = 0; col < SIZE; col++) {
            seen = new HashSet<>();
            for (int row = 0; row < SIZE; row++) {
                int number = board[row][col];
                if (number != 0 && !seen.add(number)) {
                    faultScore++;
                }
            }
        }

        // Check 3x3 blocks for duplicates
        for (int blockRow = 0; blockRow < SIZE; blockRow += 3) {
            for (int blockCol = 0; blockCol < SIZE; blockCol += 3) {
                seen = new HashSet<>();
                for (int row = blockRow; row < blockRow + 3; row++) {
                    for (int col = blockCol; col < blockCol + 3; col++) {
                        int number = board[row][col];
                        if (number != 0 && !seen.add(number)) {
                            faultScore++;
                        }
                    }
                }
            }
        }

        return faultScore;
    }


    ///////////////////////////////////////////  PRINT BOARD  ///////////////////////////////////////////////////
    // Utility method to print the Sudoku grid in the desired format
    public static void printBoard(int[][] board) {
        for (int row = 0; row < 9; row++) {
            if (row % 3 == 0 && row != 0) {
                System.out.println("-------------------------");
            }
            for (int col = 0; col < 9; col++) {
                if (col % 3 == 0 && col != 0) {
                    System.out.print("| ");
                }
                System.out.print(board[row][col] + " ");
            }
            System.out.println();
        }

        System.out.println("Initial Fault Score: " + calculateFaultScore(board) + "\n");
    }


    ///////////////////////////////////////////  SIMULATED ANNEALING  //////////////////////////////////////////////////

    private static void simulatedAnnealing(int[][] board) {
        double temp = Double.MAX_VALUE; // Starting temperature
        double coolingRate = 0.99; // Cooling rate
        int iteration = 0;

        int currentFaultScore = calculateFaultScore(board);
        int[][] bestSolution = copyBoard(board);
        int bestFaultScore = currentFaultScore;
        int previousFaultScore = currentFaultScore; // To track the faultScore at the last 50,000 iteration check

        while (temp > Double.MIN_VALUE && bestFaultScore != 0) {
            int[][] newBoard = copyBoard(board);
            successor_func(newBoard);

            int newFaultScore = calculateFaultScore(newBoard);
            int delta = newFaultScore - currentFaultScore;

            if (acceptanceProbability(delta, temp) > Math.random()) {
                board = copyBoard(newBoard);
                currentFaultScore = newFaultScore;

                if (currentFaultScore < bestFaultScore) {
                    bestSolution = copyBoard(board);
                    bestFaultScore = currentFaultScore;
                }
            }


            temp *= coolingRate;
            iteration++;

            // Check every 50,000 iterations
            if (iteration % 100 == 0) {
                if (currentFaultScore == previousFaultScore) {

                        successor_func(newBoard);

                    // Update the faultScore after running successor_func
                    currentFaultScore = calculateFaultScore(newBoard);
                }
                previousFaultScore = currentFaultScore; // Update previousFaultScore
                bestFaultScore = currentFaultScore;
            }

            if (iteration % 100 == 0){
                System.out.println("Iteration: " + iteration + " - Fault Score: " + bestFaultScore);
            }
        }

        printBoard(bestSolution);
        System.out.println("Found in " + iteration + ". iteration");
    }

    private static double acceptanceProbability(int delta, double temperature) {
        if (delta < 0) {
            return 1.0;
        }
        return Math.exp(-delta / temperature);
    }

    private static void successor_func(int[][] board) {

        Random rand = new Random();


        ////////////////////////////////////////////////////  BLOK İÇİ DEĞİŞTİRME  /////////////////////////////////////////////////////////////////



        int blockRow = rand.nextInt(3) * 3; // Randomly select a block row
        int blockCol = rand.nextInt(3) * 3; // Randomly select a block column

        int cell1Row, cell1Col;
        int cell2Row, cell2Col;

        do {
            cell1Row = blockRow + rand.nextInt(3);
            cell1Col = blockCol + rand.nextInt(3);
            cell2Row = blockRow + rand.nextInt(3);
            cell2Col = blockCol + rand.nextInt(3);
        } while (cell1Row == cell2Row && cell1Col == cell2Col || !fixed(cell1Row, cell1Col) || !fixed(cell2Row, cell2Col)); // Ensure it's a different cell

        // Swap the values of the two cells
        int temp = board[cell1Row][cell1Col];
        board[cell1Row][cell1Col] = board[cell2Row][cell2Col];
        board[cell2Row][cell2Col] = temp;


/*
        ////////////////////////////////////////////////// RANDOM 2 DEĞERİ DEĞİŞTİRME ////////////////////////////////////////////////////////////////////

        int row1, row2, col1, col2;
        do {
            row1 = rand.nextInt(SIZE);
            row2 = rand.nextInt(SIZE);
            col1 = rand.nextInt(SIZE);
            col2 = rand.nextInt(SIZE);
        } while ((row1 == row2 && col1 == col2) || !fixed(row1, col1) || !fixed(row2, col2));

        int temp = board[row1][col1];
        board[row1][col1] = board[row2][col2];
        board[row2][col2] = temp;
*/


    }


    // check default numbers
    private static boolean fixed(int row, int col){
        if (row == 0){
            if (col == 2){
                return false;
            }
        }
        else if (row == 1){
            if (col == 1 || col == 4 || col == 5 || col == 6){
                return false;
            }
        }
        else if (row == 2){
            if (col == 1 || col == 4 || col == 7){
                return false;
            }
        }
        else if (row == 3){
            if (col == 2 || col == 3 || col == 6){
                return false;
            }
        }
        else if (row == 4){
            if (col == 4 || col == 6){
                return false;
            }
        }
        else if (row == 5){
            if (col == 0 || col == 6){
                return false;
            }
        }
        else if (row == 6){
            if (col == 0 || col == 2 || col == 8){
                return false;
            }
        }
        else if (row == 7){
            if (col == 3 || col == 7){
                return false;
            }
        }
        else if (row == 8){
            if (col == 2 || col == 3 || col == 6 || col == 8){
                return false;
            }
        }
        return true;
    }

    // copying board
    private static int[][] copyBoard(int[][] original) {
        int[][] newBoard = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            newBoard[i] = original[i].clone();
        }
        return newBoard;
    }


    public static void main(String[] args) {
        int[][] board = {
                {0, 0, 6, 0, 0, 0, 0, 0, 0},
                {0, 8, 0, 0, 5, 4, 2, 0, 0},
                {0, 4, 0, 0, 9, 0, 0, 7, 0},
                {0, 0, 7, 9, 0, 0, 3, 0, 0},
                {0, 0, 0, 0, 8, 0, 4, 0, 0},
                {6, 0, 0, 0, 0, 0, 1, 0, 0},
                {2, 0, 3, 0, 0, 0, 0, 0, 1},
                {0, 0, 0, 5, 0, 0, 0, 4, 0},
                {0, 0, 8, 3, 0, 0, 5, 0, 2}
        };

        if (fillSudoku(board)) {
            printBoard(board);
        } else {
            System.out.println("Cannot fill the Sudoku grid following the rules.");
        }

        simulatedAnnealing(board);

    }
}