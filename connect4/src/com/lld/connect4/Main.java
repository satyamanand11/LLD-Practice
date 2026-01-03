package com.lld.connect4;

import com.lld.connect4.domain.board.Board;
import com.lld.connect4.domain.game.*;
import com.lld.connect4.exception.Connect4Exception;
import com.lld.connect4.service.facade.Connect4System;
import com.lld.connect4.service.facade.Connect4SystemImpl;

/**
 * Main class demonstrating the Connect4 game system.
 * Shows usage of the facade and various game operations.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Connect4 Game System Demo ===\n");

        // Get singleton facade instance (entry point)
        Connect4System facade = Connect4SystemImpl.getInstance();

        // Create a new game
        GameId gameId = facade.createGame();
        System.out.println("Created game: " + gameId);

        // Add players to game
        PlayerId player1Id = facade.joinGame(gameId, "Alice", Disc.RED);
        PlayerId player2Id = facade.joinGame(gameId, "Bob", Disc.YELLOW);
        System.out.println("Players joined the game\n");

        // Display initial board
        displayBoard(facade.getBoard(gameId));
        System.out.println();

        // Simulate a game
        System.out.println("Starting game simulation...\n");
        
        // Example moves that create a winning scenario
        int[] moves = {3, 3, 4, 4, 5, 5, 6}; // Red wins with diagonal

        for (int i = 0; i < moves.length; i++) {
            int column = moves[i];
            
            try {
                // Get current game state to determine whose turn it is
                Game game = facade.getGame(gameId);
                Player currentPlayer = game.getCurrentPlayer();
                String currentPlayerName = currentPlayer.getName();
                Disc currentDisc = currentPlayer.getDisc();
                
                System.out.println(currentPlayerName + " (Disc: " + currentDisc.getSymbol() + 
                                 ") places disc in column " + column);
                
                MoveResult result = facade.makeMove(gameId, currentPlayer.getId(), column);
                
                displayBoard(facade.getBoard(gameId));
                System.out.println();

                if (result.isWinningMove()) {
                    System.out.println("🎉 " + currentPlayerName + " wins!");
                    System.out.println("Winning line: " + result.getWinningLine().getPositions());
                    break;
                } else if (result.isDraw()) {
                    System.out.println("It's a draw!");
                    break;
                }

            } catch (Connect4Exception e) {
                System.err.println("Error: " + e.getMessage());
                break;
            }
        }

        // Display final game state
        GameState finalState = facade.getGameState(gameId);
        System.out.println("\nFinal game state: " + finalState);

        // Demonstrate error handling
        System.out.println("\n=== Error Handling Demo ===");
        try {
            facade.makeMove(gameId, player1Id, 0); // Game already finished
        } catch (Connect4Exception e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }

        // Demonstrate creating another game
        System.out.println("\n=== Creating Second Game ===");
        GameId game2Id = facade.createGame();
        System.out.println("Created second game: " + game2Id);
        facade.joinGame(game2Id, "Charlie", Disc.RED);
        System.out.println("Charlie joined game 2");
    }

    /**
     * Displays the board in a readable format.
     */
    private static void displayBoard(Board board) {
        System.out.println("Board State:");
        System.out.print("  ");
        for (int col = 0; col < board.getCols(); col++) {
            System.out.print(col + " ");
        }
        System.out.println();

        for (int row = 0; row < board.getRows(); row++) {
            System.out.print(row + " ");
            for (int col = 0; col < board.getCols(); col++) {
                Disc disc = board.getDisc(row, col);
                if (disc == null) {
                    System.out.print(". ");
                } else {
                    System.out.print(disc.getSymbol() + " ");
                }
            }
            System.out.println();
        }
    }
}

