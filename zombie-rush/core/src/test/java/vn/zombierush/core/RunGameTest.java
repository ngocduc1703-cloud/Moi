package vn.zombierush.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class RunGameTest {
    private RunGame fixture(){RunGame r=new RunGame(123,new Profile());r.items.clear();return r;}
    private void simulate(RunGame run,double seconds){for(int i=0;i<(int)Math.round(seconds*120);i++)run.advance(RunGame.STEP);}
    @Test public void fixedStepsDoNotDependOnFrameRate() {
        RunGame a=fixture(),b=fixture();
        for(int i=0;i<60;i++)a.advance(.02);for(int i=0;i<24;i++)b.advance(.05);
        assertEquals(a.distance,b.distance,1e-8);assertEquals(a.elapsed,b.elapsed,1e-8);assertEquals(a.horde.size(),b.horde.size());
    }
    @Test public void holdingGivesAHigherJumpThanTapping() {
        RunGame shortJump=fixture(),longJump=fixture();shortJump.jumpDown();longJump.jumpDown();
        shortJump.advance(RunGame.STEP);shortJump.jumpUp();double shortMax=0,longMax=0;
        for(int i=0;i<140;i++){
            shortJump.advance(RunGame.STEP);longJump.advance(RunGame.STEP);
            shortMax=Math.max(shortMax,shortJump.horde.get(0).height);longMax=Math.max(longMax,longJump.horde.get(0).height);
        }
        assertTrue(shortMax>50);assertTrue(longMax>shortMax+35);assertEquals(0,longJump.horde.get(0).height,.001);
    }
    @Test public void followersJumpAtTheSamePlaceAfterTheLeader() {
        RunGame r=fixture();assertTrue(r.jumpDown());r.advance(.02);
        assertTrue(r.horde.get(0).height>0);assertEquals(0,r.horde.get(2).height,0);
        r.advance(.15);assertTrue(r.horde.get(2).height>0);
    }
    @Test public void oneCoinIsNotCollectedOncePerMember() {
        RunGame r=fixture();r.items.add(new RunGame.Item(RunGame.Kind.COIN,RunGame.LEADER_X+8,12,30));
        simulate(r,.6);assertEquals(1,r.coins);
    }
    @Test public void humansRecruitButTheHordeNeverExceedsItsLimit() {
        RunGame r=fixture();for(int i=0;i<20;i++)r.items.add(new RunGame.Item(RunGame.Kind.HUMAN,RunGame.LEADER_X+8,24,24));
        r.advance(.02);assertEquals(RunGame.MAX_HORDE,r.horde.size());assertEquals(20,r.recruited);assertEquals(RunGame.MAX_HORDE,r.peakHorde);
    }
    @Test public void anUnavoidedBarrierEndsASmallHordeAndStopsTheClock() {
        RunGame r=fixture();r.items.add(new RunGame.Item(RunGame.Kind.BARRIER,RunGame.LEADER_X+5,50,30));simulate(r,.8);
        assertEquals(RunGame.Status.OVER,r.status);assertEquals(3,r.lostMembers);double distance=r.distance;r.advance(.2);assertEquals(distance,r.distance,0);
    }
    @Test public void aTimedJumpClearsTheBarrierForTheWholeHorde() {
        RunGame r=fixture();r.items.add(new RunGame.Item(RunGame.Kind.BARRIER,RunGame.LEADER_X+95,50,30));r.jumpDown();simulate(r,1.3);
        assertEquals(RunGame.Status.RUNNING,r.status);assertEquals(3,r.horde.size());assertEquals(0,r.lostMembers);
    }
    @Test public void fourZombiesSmashACarButThreeCannot() {
        RunGame weak=fixture();weak.items.add(new RunGame.Item(RunGame.Kind.CAR,RunGame.LEADER_X+5,88,55));simulate(weak,1);
        assertEquals(RunGame.Status.OVER,weak.status);
        Profile stronger=new Profile();stronger.startLevel=1;RunGame strong=new RunGame(123,stronger);strong.items.clear();
        strong.items.add(new RunGame.Item(RunGame.Kind.CAR,RunGame.LEADER_X+5,88,55));simulate(strong,.4);
        assertEquals(4,strong.horde.size());assertEquals(1,strong.carsBroken);assertEquals(RunGame.Status.RUNNING,strong.status);
    }
    @Test public void fallingIntoAGapStillKillsTheHordeWithAShield() {
        RunGame r=fixture();r.shieldSeconds=8;r.items.add(new RunGame.Item(RunGame.Kind.GAP,RunGame.LEADER_X+5,140,0));simulate(r,1.5);
        assertEquals(RunGame.Status.OVER,r.status);assertEquals(3,r.lostMembers);
    }
    @Test public void aHeldJumpCanClearAGap() {
        RunGame r=fixture();r.items.add(new RunGame.Item(RunGame.Kind.GAP,RunGame.LEADER_X+85,125,0));r.jumpDown();simulate(r,1.5);
        assertEquals(RunGame.Status.RUNNING,r.status);assertEquals(3,r.horde.size());
    }
    @Test public void giantModeBridgesAGap() {
        RunGame r=fixture();r.giantSeconds=8;r.items.add(new RunGame.Item(RunGame.Kind.GAP,RunGame.LEADER_X+5,140,0));simulate(r,1.2);
        assertEquals(3,r.horde.size());assertEquals(RunGame.Status.RUNNING,r.status);
    }
    @Test public void magnetCollectsDistantHighCoins() {
        RunGame r=fixture();r.items.add(new RunGame.Item(RunGame.Kind.MAGNET,RunGame.LEADER_X+8,30,35));r.items.add(new RunGame.Item(RunGame.Kind.COIN,RunGame.LEADER_X+140,12,150));r.advance(.02);
        assertEquals(1,r.coins);assertTrue(r.magnetSeconds>9);
        RunGame plain=fixture();plain.items.add(new RunGame.Item(RunGame.Kind.COIN,RunGame.LEADER_X+140,12,150));plain.advance(.02);assertEquals(0,plain.coins);
    }
    @Test public void shieldProtectsASmallHordeFromACar() {
        RunGame r=fixture();r.items.add(new RunGame.Item(RunGame.Kind.SHIELD,RunGame.LEADER_X+8,30,35));r.items.add(new RunGame.Item(RunGame.Kind.CAR,RunGame.LEADER_X+5,88,55));r.advance(.02);
        assertEquals(3,r.horde.size());assertEquals(1,r.carsBroken);
    }
    @Test public void longRunsRecycleWorldObjectsAndIncreaseDifficulty() {
        RunGame r=new RunGame(321,new Profile());
        for(int i=0;i<4800;i++){r.giantSeconds=1000;r.advance(.05);assertTrue(r.items.size()<100);assertTrue(r.horde.size()<=RunGame.MAX_HORDE);}
        assertTrue(r.meters()>5000);assertTrue(r.speed>300);assertTrue(r.coins>0);assertTrue(r.carsBroken>0);
    }
    @Test public void pauseDoesNotAdvancePhysicsAndBankingCannotDuplicateCoins() {
        MemoryStore store=new MemoryStore();Scene scene=new Scene(store);scene.start(1);scene.run.items.clear();
        scene.run.items.add(new RunGame.Item(RunGame.Kind.COIN,RunGame.LEADER_X+8,12,30));scene.update(.02);assertEquals(1,scene.run.coins);
        scene.pause();double before=scene.run.distance;for(int i=0;i<20;i++)scene.update(.1);
        assertEquals(before,scene.run.distance,0);assertEquals(1,scene.profile.wallet);
        scene.close();scene.close();assertEquals(1,scene.profile.wallet);
        scene.back();scene.pause();assertEquals(1,scene.profile.wallet);assertEquals(1,Profile.decode(store.read("profile")).wallet);
    }
    @Test public void purchasesSpendCoinsAndRespectTheirCaps() {
        Profile p=new Profile();assertFalse(p.buy(0));p.wallet=1000;int cost=p.price(0);assertTrue(p.buy(0));assertEquals(1000-cost,p.wallet);assertEquals(4,p.startingHorde());
        p.wallet=10000;while(p.buy(0)){}assertEquals(3,p.startLevel);assertEquals(-1,p.price(0));assertFalse(p.buy(8));
        assertEquals(p.encode(),Profile.decode(p.encode()).encode());
    }
    @Test public void corruptProgressIsRejectedAndSceneRecovers() {
        String[] invalid={"broken","ZR1;-1;0;0;0;0;0;0;1","ZR1;0;0;0;0;8;0;0;1"};
        for(String input:invalid){try{Profile.decode(input);fail("Accepted invalid progress");}catch(RuntimeException expected){}}
        MemoryStore store=new MemoryStore();store.write("profile","broken");Scene scene=new Scene(store);assertEquals(0,scene.profile.wallet);assertEquals(3,scene.profile.startingHorde());
    }
    @Test public void stalledAndInvalidFrameTimesCannotTeleportTheHorde() {
        RunGame r=fixture();r.advance(10);assertTrue(r.elapsed<=.20001);double distance=r.distance;r.advance(Double.NaN);r.advance(-1);r.advance(Double.POSITIVE_INFINITY);assertEquals(distance,r.distance,0);
    }
    static final class MemoryStore implements SaveStore {final Map<String,String> values=new HashMap<String,String>();public String read(String key){return values.get(key);}public void write(String key,String value){values.put(key,value);}}
}
