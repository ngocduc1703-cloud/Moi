package vn.garden.core;

import java.util.*;

/** Platform-neutral rules, level generation, recovery and local save format. */
public final class Game {
    public enum Status { PLAYING, WON, LOST }
    public static final int MAX_LEVEL = 30, CAPACITY = 7, KINDS = 10;
    public final List<Tile> tiles = new ArrayList<Tile>();
    public final List<Integer> tray = new ArrayList<Integer>();
    public final List<Integer> solution = new ArrayList<Integer>();
    public int level, score, matches, moves, hints = 3, shuffles = 2, undos = 3;
    public Status status = Status.PLAYING;
    private long seed;
    private final Deque<String> history = new ArrayDeque<String>();

    public Game(int level, long seed) {
        if (level < 1 || level > MAX_LEVEL) throw new IllegalArgumentException("Invalid level");
        this.level = level; this.seed = seed;
        int rows = level < 7 ? 5 : 6;
        for (int row = 0; row < rows; row++) for (int col = 0; col < 6; col++) add(col, row, 0);
        int middleCols = level < 7 ? 4 : 5;
        for (int row = 0; row < 3; row++) for (int col = 0; col < middleCols; col++)
            add(col + (6 - middleCols) / 2f + .15f, row + 1.3f, 1);
        if (level >= 4) for (int row = 0; row < (level < 7 ? 2 : 3); row++)
            for (int col = 0; col < 3; col++) add(col + 1.6f, row + 1.8f, 2);
        if (level >= 16) for (int col = 0; col < 3; col++) add(col + 1.7f, 2.3f, 3);
        assignSolvableKinds(new Random(seed), false);
    }
    private void add(float x, float y, int layer) { tiles.add(new Tile(tiles.size(), 0, x, y, layer)); }
    public boolean available(int id) {
        if (id < 0 || id >= tiles.size()) return false;
        Tile t = tiles.get(id);
        if (t.removed) return false;
        for (Tile other : tiles) if (!other.removed && covers(other, t)) return false;
        return true;
    }
    private boolean covers(Tile a, Tile b) {
        return a.layer > b.layer && Math.abs(a.x - b.x) < .93f && Math.abs(a.y - b.y) < .93f;
    }
    public List<Integer> availableTiles() {
        List<Integer> result = new ArrayList<Integer>();
        for (Tile t : tiles) if (available(t.id)) result.add(t.id);
        return result;
    }
    public int remaining() { int n = 0; for (Tile t : tiles) if (!t.removed) n++; return n; }
    public boolean pick(int id) {
        if (status != Status.PLAYING || !available(id)) return false;
        history.push(encode());
        Tile t = tiles.get(id); t.removed = true; moves++;
        int insertion = tray.size();
        for (int i = 0; i < tray.size(); i++) if (tiles.get(tray.get(i)).kind == t.kind) insertion = i + 1;
        tray.add(insertion, id);
        int count = 0;
        for (int item : tray) if (tiles.get(item).kind == t.kind) count++;
        if (count == 3) {
            for (Iterator<Integer> it = tray.iterator(); it.hasNext();) if (tiles.get(it.next()).kind == t.kind) it.remove();
            matches++; score += 100 + level * 10;
        }
        if (remaining() == 0 && tray.isEmpty()) status = Status.WON;
        else if (tray.size() >= CAPACITY) status = Status.LOST;
        return true;
    }
    public boolean undo() {
        if (undos <= 0 || history.isEmpty() || status == Status.WON) return false;
        int nextUndos = undos - 1, savedHints = hints, savedShuffles = shuffles;
        restore(history.pop()); undos = nextUndos; hints = savedHints; shuffles = savedShuffles;
        return true;
    }
    public boolean canUndo() { return undos > 0 && !history.isEmpty() && status != Status.WON; }
    public int hint() {
        if (status != Status.PLAYING || hints <= 0) return -1;
        List<Integer> available = availableTiles();
        int best = -1, bestWeight = -1;
        for (int id : available) {
            int k = tiles.get(id).kind, inTray = 0, onBoard = 0;
            for (int item : tray) if (tiles.get(item).kind == k) inTray++;
            for (int item : available) if (tiles.get(item).kind == k) onBoard++;
            int weight = inTray * 100 + onBoard * 5;
            if (weight > bestWeight) { bestWeight = weight; best = id; }
        }
        if (best >= 0) hints--;
        return best;
    }
    public boolean shuffle() {
        if (shuffles <= 0 || status != Status.PLAYING) return false;
        shuffles--; history.clear(); assignSolvableKinds(new Random(seed + moves * 1009L + shuffles * 23L), true);
        return true;
    }
    /** Assign triples along a valid geometric removal order. Existing tray pairs are completed first. */
    private void assignSolvableKinds(Random random, boolean keepCounts) {
        boolean[] gone = new boolean[tiles.size()];
        for (Tile t : tiles) gone[t.id] = t.removed;
        solution.clear();
        while (solution.size() < remaining()) {
            List<Integer> choices = new ArrayList<Integer>();
            for (Tile t : tiles) if (!gone[t.id]) {
                boolean blocked = false;
                for (Tile o : tiles) if (!gone[o.id] && covers(o, t)) { blocked = true; break; }
                if (!blocked) choices.add(t.id);
            }
            int selected = choices.get(random.nextInt(choices.size()));
            solution.add(selected); gone[selected] = true;
        }
        List<Integer> kinds = new ArrayList<Integer>();
        if (keepCounts) {
            int[] counts = new int[KINDS], inTray = new int[KINDS];
            for (Tile t : tiles) if (!t.removed) counts[t.kind]++;
            for (int id : tray) inTray[tiles.get(id).kind]++;
            for (int k = 0; k < KINDS; k++) if (inTray[k] > 0) {
                int needed = 3 - inTray[k];
                for (int j = 0; j < needed; j++) kinds.add(k);
                counts[k] -= needed;
            }
            List<Integer> groups = new ArrayList<Integer>();
            for (int k = 0; k < KINDS; k++) for (int j = 0; j < counts[k] / 3; j++) groups.add(k);
            Collections.shuffle(groups, random);
            for (int k : groups) for (int j = 0; j < 3; j++) kinds.add(k);
        } else {
            int count = Math.min(KINDS, 4 + (level - 1) / 3);
            for (int i = 0; i < solution.size() / 3; i++) {
                int k = (i < count) ? i : random.nextInt(count);
                for (int j = 0; j < 3; j++) kinds.add(k);
            }
        }
        if (kinds.size() != solution.size()) throw new IllegalStateException("Unbalanced tile counts");
        for (int i = 0; i < solution.size(); i++) tiles.get(solution.get(i)).kind = kinds.get(i);
    }
    public String encode() {
        StringBuilder out = new StringBuilder("G1;");
        out.append(level).append(';').append(seed).append(';').append(score).append(';').append(matches).append(';')
            .append(moves).append(';').append(hints).append(';').append(shuffles).append(';').append(undos).append(';').append(status.name()).append(';');
        for (Tile t : tiles) out.append(t.kind).append(t.removed ? 'r' : 'b').append(',');
        out.append(';'); for (int id : tray) out.append(id).append(',');
        return out.toString();
    }
    public static Game decode(String encoded) {
        String[] p = encoded.split(";", -1);
        if (p.length != 12 || !p[0].equals("G1")) throw new IllegalArgumentException("Save version");
        Game g = new Game(Integer.parseInt(p[1]), Long.parseLong(p[2]));
        g.restore(encoded); return g;
    }
    private void restore(String encoded) {
        String[] p = encoded.split(";", -1);
        score = Integer.parseInt(p[3]); matches = Integer.parseInt(p[4]); moves = Integer.parseInt(p[5]);
        hints = Integer.parseInt(p[6]); shuffles = Integer.parseInt(p[7]); undos = Integer.parseInt(p[8]); status = Status.valueOf(p[9]);
        if (score < 0 || matches < 0 || moves < 0 || hints < 0 || hints > 3 || shuffles < 0 || shuffles > 2 || undos < 0 || undos > 3)
            throw new IllegalArgumentException("Invalid counters");
        String[] data = p[10].split(",");
        if (data.length != tiles.size()) throw new IllegalArgumentException("Invalid board size");
        for (int i = 0; i < data.length; i++) {
            int kind = Integer.parseInt(data[i].substring(0, data[i].length() - 1));
            char flag = data[i].charAt(data[i].length() - 1);
            if (kind < 0 || kind >= KINDS || (flag != 'r' && flag != 'b')) throw new IllegalArgumentException("Invalid tile");
            tiles.get(i).kind = kind; tiles.get(i).removed = flag == 'r';
        }
        tray.clear();
        if (!p[11].isEmpty()) for (String s : p[11].split(",")) {
            int id = Integer.parseInt(s);
            if (id < 0 || id >= tiles.size() || !tiles.get(id).removed || tray.contains(id)) throw new IllegalArgumentException("Invalid tray");
            tray.add(id);
        }
        if (tray.size() > CAPACITY) throw new IllegalArgumentException("Tray overflow");
        int[] counts = new int[KINDS];
        for (Tile t : tiles) if (!t.removed) counts[t.kind]++;
        for (int id : tray) counts[tiles.get(id).kind]++;
        for (int n : counts) if (n % 3 != 0) throw new IllegalArgumentException("Unbalanced save");
        if ((status == Status.WON) != (remaining() == 0 && tray.isEmpty()) || (status == Status.LOST) != (tray.size() == CAPACITY))
            throw new IllegalArgumentException("Invalid status");
    }
}
