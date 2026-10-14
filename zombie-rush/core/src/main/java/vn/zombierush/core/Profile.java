package vn.zombierush.core;

/** Persistent offline progress; purchases spend earned in-game coins only. */
public final class Profile {
    public int wallet, bestScore, bestMeters, runs, startLevel, magnetLevel, coinLevel;
    public boolean sound = true;
    public int startingHorde() { return 3 + startLevel; }
    public int coinValue() { return 1 + coinLevel; }
    public int magnetDuration() { return 10 + magnetLevel * 3; }
    public int level(int upgrade) { return upgrade == 0 ? startLevel : upgrade == 1 ? magnetLevel : coinLevel; }
    public int limit(int upgrade) { return upgrade == 2 ? 2 : 3; }
    public int price(int upgrade) {
        if (upgrade < 0 || upgrade > 2 || level(upgrade) >= limit(upgrade)) return -1;
        int base = upgrade == 0 ? 75 : upgrade == 1 ? 50 : 90;
        return base << level(upgrade);
    }
    public boolean buy(int upgrade) {
        int cost = price(upgrade);
        if (cost < 0 || wallet < cost) return false;
        wallet -= cost;
        if (upgrade == 0) startLevel++; else if (upgrade == 1) magnetLevel++; else coinLevel++;
        return true;
    }
    public String encode() {
        return "ZR1;" + wallet + ";" + bestScore + ";" + bestMeters + ";" + runs + ";" + startLevel + ";" + magnetLevel + ";" + coinLevel + ";" + (sound ? "1" : "0");
    }
    public static Profile decode(String text) {
        if (text == null) return new Profile();
        String[] p = text.split(";", -1);
        if (p.length != 9 || !p[0].equals("ZR1")) throw new IllegalArgumentException("Invalid profile version");
        int[] n = new int[8];
        for (int i = 0; i < n.length; i++) {
            n[i] = Integer.parseInt(p[i + 1]);
            if (n[i] < 0 || n[i] > 1000000000) throw new IllegalArgumentException("Invalid progress");
        }
        if (n[4] > 3 || n[5] > 3 || n[6] > 2 || n[7] > 1) throw new IllegalArgumentException("Invalid upgrade");
        Profile result = new Profile();
        result.wallet = n[0]; result.bestScore = n[1]; result.bestMeters = n[2]; result.runs = n[3];
        result.startLevel = n[4]; result.magnetLevel = n[5]; result.coinLevel = n[6]; result.sound = n[7] == 1;
        return result;
    }
}
