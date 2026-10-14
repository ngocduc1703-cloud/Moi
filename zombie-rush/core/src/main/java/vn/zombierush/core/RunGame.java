package vn.zombierush.core;

import java.util.*;

/** Deterministic fixed-step endless runner. Heights are measured above the road. */
public final class RunGame {
    public static final int MAX_HORDE = 12, CAR_STRENGTH = 4;
    public static final double LEADER_X = 255, SPACING = 19, STEP = 1.0 / 120;
    public enum Kind { COIN, HUMAN, CAR, BARRIER, GAP, SHIELD, MAGNET, GIANT }
    public enum Status { RUNNING, OVER }
    public static final class Member {
        public final int id;
        public double height, velocity;
        public boolean alive = true;
        long lastMark = -1;
        double jumpedAt;
        JumpMark activeJump;
        Member(int id) { this.id = id; }
    }
    public static final class Item {
        public final Kind kind;
        public final double x, width, height;
        public boolean removed;
        public Item(Kind kind, double x, double width, double height) {
            this.kind = kind; this.x = x; this.width = width; this.height = height;
        }
    }
    static final class JumpMark {
        final long id;
        final double x, started;
        double heldFor = .45;
        boolean closed;
        JumpMark(long id, double x, double started) { this.id = id; this.x = x; this.started = started; }
    }
    public final List<Member> horde = new ArrayList<Member>();
    public final List<Item> items = new ArrayList<Item>();
    public Status status = Status.RUNNING;
    public double distance, elapsed, speed = 260, shieldSeconds, magnetSeconds, giantSeconds;
    public int coins, recruited, carsBroken, peakHorde, lostMembers;
    public long eventSerial;
    public String event = "", message = "";
    private final Random random;
    private final int coinValue, magnetDuration;
    private final Deque<JumpMark> jumps = new ArrayDeque<JumpMark>();
    private double accumulator, nextWaveX = 920;
    private int memberId, wave;
    private long markId;
    private boolean holding;
    private JumpMark currentJump;

    public RunGame(long seed, Profile profile) {
        random = new Random(seed); coinValue = profile.coinValue(); magnetDuration = profile.magnetDuration();
        for (int i = 0; i < profile.startingHorde(); i++) horde.add(new Member(memberId++));
        peakHorde = horde.size();
        populate();
    }
    public int meters() { return (int) (distance / 12); }
    public int score() { return meters() * 3 + coins * 10 + recruited * 30 + carsBroken * 75; }
    public double worldX(int index) { return distance + LEADER_X - index * SPACING; }
    public boolean jumpDown() {
        if (holding || status != Status.RUNNING || horde.isEmpty()) return false;
        holding = true;
        Member leader = horde.get(0);
        if (leader.height != 0 || overGap(worldX(0))) return false;
        currentJump = new JumpMark(markId++, worldX(0), elapsed);
        jumps.addLast(currentJump);
        return true;
    }
    public void jumpUp() {
        holding = false;
        if (currentJump != null && !currentJump.closed) {
            currentJump.heldFor = Math.min(.45, Math.max(0, elapsed - currentJump.started));
            currentJump.closed = true;
        }
    }
    public void advance(double seconds) {
        if (status != Status.RUNNING || seconds <= 0 || Double.isNaN(seconds) || Double.isInfinite(seconds)) return;
        accumulator += Math.min(.2, seconds);
        while (accumulator + 1e-10 >= STEP && status == Status.RUNNING) { step(); accumulator -= STEP; }
    }
    private boolean overGap(double x) {
        if (giantSeconds > 0) return false;
        for (Item item : items) if (!item.removed && item.kind == Kind.GAP && x > item.x && x < item.x + item.width) return true;
        return false;
    }
    private void emit(String name, String text) { event = name; message = text; eventSerial++; }
    private void step() {
        elapsed += STEP;
        speed = 260 + Math.min(130, distance / 85);
        distance += speed * STEP;
        shieldSeconds = Math.max(0, shieldSeconds - STEP);
        magnetSeconds = Math.max(0, magnetSeconds - STEP);
        giantSeconds = Math.max(0, giantSeconds - STEP);
        populate();
        for (int index = 0; index < horde.size(); index++) {
            Member member = horde.get(index);
            double x = worldX(index);
            for (JumpMark jump : jumps) if (jump.id > member.lastMark && x >= jump.x) {
                member.lastMark = jump.id;
                if (member.height == 0 && !overGap(x)) {
                    member.velocity = 510; member.jumpedAt = elapsed; member.activeJump = jump;
                }
            }
            if (member.height != 0 || member.velocity != 0 || overGap(x)) {
                boolean boost = member.activeJump != null && member.velocity > 0 && elapsed - member.jumpedAt < member.activeJump.heldFor;
                member.velocity -= (boost ? 940 : 1800) * STEP;
                member.height += member.velocity * STEP;
                if (member.height <= 0 && member.velocity < 0 && !overGap(x) && member.height > -18) {
                    member.height = 0; member.velocity = 0; member.activeJump = null;
                }
                if (member.height < -95) { member.alive = false; lostMembers++; emit("hurt", "Một thành viên rơi xuống hố!"); }
            }
        }
        // Collectibles are consumed once for the entire horde.
        int additions = 0;
        for (Item item : items) if (!item.removed && collectible(item.kind)) {
            boolean touched = item.kind == Kind.COIN && magnetSeconds > 0 && Math.abs(item.x - worldX(0)) < 190;
            for (int i = 0; i < horde.size() && !touched; i++) {
                Member member = horde.get(i);
                if (member.alive && member.height >= 0 && Math.abs(worldX(i) - item.x) < 24 && Math.abs(member.height + 24 - item.height) < 35) touched = true;
            }
            if (touched) {
                item.removed = true;
                if (item.kind == Kind.COIN) { coins += coinValue; emit("coin", "+" + coinValue + " xu"); }
                else if (item.kind == Kind.HUMAN) { additions++; recruited++; emit("recruit", "+1 zombie! Đàn càng đông càng mạnh"); }
                else if (item.kind == Kind.SHIELD) { shieldSeconds = 8; emit("power", "Khiên 8 giây! Vẫn cần nhảy qua hố"); }
                else if (item.kind == Kind.MAGNET) { magnetSeconds = magnetDuration; emit("power", "Nam châm! Tự hút xu xung quanh"); }
                else { giantSeconds = 8; emit("power", "Khổng lồ 8 giây! Lướt qua mọi chướng ngại"); }
            }
        }
        for (Item item : items) if (!item.removed && (item.kind == Kind.CAR || item.kind == Kind.BARRIER)) {
            for (int i = 0; i < horde.size() && !item.removed; i++) {
                Member member = horde.get(i);
                double x = worldX(i);
                if (member.alive && x + 10 > item.x && x - 10 < item.x + item.width && member.height < item.height) {
                    if (shieldSeconds > 0 || giantSeconds > 0 || (item.kind == Kind.CAR && living() >= CAR_STRENGTH)) {
                        item.removed = true;
                        if (item.kind == Kind.CAR) { carsBroken++; emit("smash", "Lật xe! +75 điểm"); }
                        else emit("smash", "Phá rào chắn!");
                    } else { member.alive = false; lostMembers++; emit("hurt", "Mất một zombie! Nhảy qua chướng ngại nhé"); }
                }
            }
        }
        for (Iterator<Member> it = horde.iterator(); it.hasNext();) if (!it.next().alive) it.remove();
        for (int i = 0; i < additions && horde.size() < MAX_HORDE; i++) {
            Member fresh = new Member(memberId++);
            double freshX = worldX(horde.size());
            for (JumpMark jump : jumps) if (jump.x <= freshX) fresh.lastMark = jump.id;
            horde.add(fresh);
        }
        peakHorde = Math.max(peakHorde, horde.size());
        if (horde.isEmpty()) { status = Status.OVER; jumpUp(); emit("over", "Cả đàn đã dừng lại. Thử một đường chạy mới!"); }
        double tailX = distance + LEADER_X - MAX_HORDE * SPACING;
        for (Iterator<Item> it = items.iterator(); it.hasNext();) {
            Item item = it.next(); if (item.removed || item.x + item.width < tailX - 180) it.remove();
        }
        while (!jumps.isEmpty() && jumps.peekFirst().x < tailX - 100) jumps.removeFirst();
    }
    private int living() { int n = 0; for (Member m : horde) if (m.alive) n++; return n; }
    private boolean collectible(Kind kind) { return kind == Kind.COIN || kind == Kind.HUMAN || kind == Kind.SHIELD || kind == Kind.MAGNET || kind == Kind.GIANT; }
    private void populate() {
        while (nextWaveX < distance + 1700) {
            double x = nextWaveX;
            items.add(new Item(Kind.HUMAN, x - 140, 24, 24));
            if (wave == 0) items.add(new Item(Kind.HUMAN, x - 90, 24, 24));
            int pattern = wave == 0 ? 0 : wave == 1 ? 1 : wave == 2 ? 2 : wave == 3 ? 3 : random.nextInt(5);
            if (pattern == 1) items.add(new Item(Kind.BARRIER, x, 50, 30));
            else if (pattern == 2) items.add(new Item(Kind.CAR, x, 88, 55));
            else if (pattern == 3) items.add(new Item(Kind.GAP, x, 105 + random.nextInt(45), 0));
            for (int i = 0; i < 6; i++) {
                double height = pattern == 1 || pattern == 3 ? 48 + Math.sin(i * Math.PI / 5) * 55 : 30;
                items.add(new Item(Kind.COIN, x - 45 + i * 32, 12, height));
            }
            if (wave > 0 && wave % 3 == 0) {
                Kind power = wave % 9 == 3 ? Kind.SHIELD : wave % 9 == 6 ? Kind.MAGNET : Kind.GIANT;
                items.add(new Item(power, x + 215, 30, 35));
            }
            nextWaveX += 480 + random.nextInt(150);
            wave++;
        }
    }
}
