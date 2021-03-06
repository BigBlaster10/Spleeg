package main.java.org.trompgames.utils;

import java.util.ArrayList;
import java.util.List;

public abstract class Updateable {

    private static List<Updateable> updates = new ArrayList<>();

    private int ticks;

    private int currentTicks = 0;

    public Updateable(int ticks) {
        this.ticks = ticks;
        updates.add(this);
    }

    protected abstract void update();

    static int totalTicks = 1;

    public static void updateUpdateables() {
        for (Updateable update : updates) {
            update.currentTicks++;
            if (update.currentTicks >= update.ticks) {
                update.currentTicks = 0;
                update.update();
            }
        }
        totalTicks++;
        if (totalTicks > 20) totalTicks = 1;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    public static List<Updateable> getUpdates() {
        if (updates != null) {
            return updates;
        }
        throw new NullPointerException("Updates not initialized.");
    }
    
    public void remove(){
    	updates.remove(this);
    }
}
