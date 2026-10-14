package vn.garden.core;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameTest {
    @Test public void everyLevelHasAPlayableSolutionAcrossSeeds() {
        for(int level=1;level<=30;level++) for(int seed=0;seed<30;seed++) {
            Game g=new Game(level,seed);
            assertEquals(0,g.tiles.size()%3);
            List<Integer> solution=new ArrayList<Integer>(g.solution);
            for(int id:solution) {
                assertTrue("Covered solution tile at level "+level,g.available(id));
                assertTrue(g.pick(id));
                assertTrue(g.tray.size()<Game.CAPACITY);
            }
            assertEquals(Game.Status.WON,g.status);assertEquals(0,g.remaining());assertEquals(g.tiles.size()/3,g.matches);
        }
    }
    @Test public void coveredTileAndRepeatClickAreRejected() {
        Game g=new Game(20,123);
        Tile covered=null;for(Tile t:g.tiles)if(!g.available(t.id)){covered=t;break;}
        assertNotNull(covered);assertFalse(g.pick(covered.id));assertEquals(0,g.moves);
        int id=g.availableTiles().get(0);assertTrue(g.pick(id));assertFalse(g.pick(id));assertEquals(1,g.moves);
    }
    @Test public void thirdTileClearsImmediatelyAndUndoRestoresExactState() {
        Game g=new Game(1,234);
        int a=g.solution.get(0),b=g.solution.get(1),c=g.solution.get(2);
        g.pick(a);g.pick(b);String before=g.encode();g.pick(c);
        assertTrue(g.tray.isEmpty());assertEquals(1,g.matches);assertEquals(110,g.score);
        assertTrue(g.undo());assertEquals(2,g.tray.size());assertEquals(0,g.score);assertEquals(2,g.undos);
        Game reference=Game.decode(before);assertEquals(reference.remaining(),g.remaining());
        assertEquals(reference.tray,g.tray);assertTrue(g.available(c));
    }
    @Test public void sevenUnmatchedTilesLoseAndUndoRecovers() {
        Game g=findLostGame();
        assertNotNull("Expected a reachable loss state",g);assertEquals(7,g.tray.size());
        assertFalse(g.pick(g.availableTiles().get(0)));assertTrue(g.undo());
        assertEquals(Game.Status.PLAYING,g.status);assertEquals(6,g.tray.size());
    }
    private Game findLostGame() {
        for(int seed=0;seed<100;seed++) {
            Game g=new Game(30,seed);Random r=new Random(seed);
            for(int move=0;move<100&&g.status==Game.Status.PLAYING;move++) {
                List<Integer> choices=g.availableTiles();Collections.shuffle(choices,r);
                int id=choices.get(0);
                for(int candidate:choices) {
                    int n=0;for(int trayId:g.tray)if(g.tiles.get(trayId).kind==g.tiles.get(candidate).kind)n++;
                    if(n<2){id=candidate;break;}
                }
                g.pick(id);
            }
            if(g.status==Game.Status.LOST)return g;
        }
        return null;
    }
    @Test public void shufflePreservesTrayAndKindsAndSuppliesWinningContinuation() {
        for(int seed=0;seed<50;seed++) {
            Game g=new Game(25,seed);g.pick(g.solution.get(0));g.pick(g.solution.get(1));
            List<Integer> tray=new ArrayList<Integer>(g.tray);int[] before=counts(g);
            assertTrue(g.shuffle());assertEquals(tray,g.tray);assertArrayEquals(before,counts(g));assertEquals(1,g.shuffles);
            for(int id:new ArrayList<Integer>(g.solution)){assertTrue(g.pick(id));assertNotEquals(Game.Status.LOST,g.status);}
            assertEquals(Game.Status.WON,g.status);
        }
    }
    private int[] counts(Game g){int[] c=new int[10];for(Tile t:g.tiles)if(!t.removed)c[t.kind]++;for(int id:g.tray)c[g.tiles.get(id).kind]++;return c;}
    @Test public void saveRoundTripPreservesGameAndCorruptionIsRejected() {
        Game g=new Game(10,444);g.pick(g.solution.get(0));g.hint();g.shuffle();
        assertEquals(g.encode(),Game.decode(g.encode()).encode());
        String[] corrupt={"", "G2;bad",g.encode().replace("PLAYING","WON"),g.encode().replace("G1;10;","G1;99;")};
        for(String save:corrupt){try{Game.decode(save);fail("Accepted corrupted save");}catch(RuntimeException expected){}}
    }
    @Test public void boostersAreFiniteAndCannotUndoToRefundThem() {
        Game g=new Game(1,2);g.pick(g.solution.get(0));
        for(int i=0;i<3;i++)assertTrue(g.hint()>=0);assertEquals(-1,g.hint());
        assertTrue(g.undo());assertEquals(0,g.hints);
        assertTrue(g.shuffle());assertTrue(g.shuffle());assertFalse(g.shuffle());
        for(int i=0;i<3;i++){g.pick(g.availableTiles().get(0));if(i<2)assertTrue(g.undo());else assertFalse(g.undo());}
        assertEquals(0,g.undos);
    }
    @Test public void sceneSavesProgressUnlocksLevelAndReloads() {
        MemoryStore store=new MemoryStore();Scene scene=new Scene(store);
        scene.click(240,525);assertEquals(Scene.Screen.GAME,scene.screen);
        for(int id:new ArrayList<Integer>(scene.game.solution)) {
            Tile t=scene.game.tiles.get(id);scene.click(Scene.tileX(t)+27,Scene.tileY(t)+27);
        }
        assertEquals(Game.Status.WON,scene.game.status);assertEquals(2,scene.unlocked);
        Scene restored=new Scene(store);assertEquals(2,restored.unlocked);assertEquals(Game.Status.WON,restored.game.status);
        restored.click(240,525);assertEquals(2,restored.game.level);
    }
    @Test public void corruptSaveFallsBackToHomeSafely() {
        MemoryStore store=new MemoryStore();store.write("game","broken");Scene scene=new Scene(store);assertNull(scene.game);assertEquals("",store.read("game"));
    }
    static final class MemoryStore implements SaveStore {
        final Map<String,String> map=new HashMap<String,String>();public String read(String k){return map.get(k);}public void write(String k,String v){map.put(k,v);}
    }
}
