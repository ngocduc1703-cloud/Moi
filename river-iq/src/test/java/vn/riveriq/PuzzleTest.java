package vn.riveriq;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class PuzzleTest {
    @Test public void everyLevelHasSolution(){
        int index=0;for(Puzzle p:PuzzleCatalog.all()){List<Integer> solution=Solver.solve(p,0,false);assertFalse("Level "+index+" must be solvable",solution.isEmpty());assertTrue(solution.size()<=p.parMoves+8);index++;}
    }
    @Test public void solverMovesObeyEveryRule(){
        for(Puzzle p:PuzzleCatalog.all()){GameState game=new GameState(p);for(int passengers:Solver.solve(p,0,false)){game.selected=passengers;assertTrue(game.move());assertTrue(p.isValid(game.rightMask));}assertTrue(game.won);}
    }
    @Test public void classicPuzzleRejectsUnsafeBank(){
        GameState game=new GameState(PuzzleCatalog.all().get(0));game.toggle(0);assertFalse(game.move());assertEquals(0,game.rightMask);assertTrue(game.message.contains("luật an toàn"));
    }
    @Test public void undoRestoresState(){
        GameState game=new GameState(PuzzleCatalog.all().get(0));game.toggle(0);game.toggle(2);assertTrue(game.move());int moved=game.rightMask;game.undo();assertEquals(0,game.rightMask);assertNotEquals(moved,game.rightMask);assertFalse(game.boatRight);
    }
}
