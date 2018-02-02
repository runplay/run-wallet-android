package run.wallet.iota.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by coops on 11/01/18.
 */

public class TickerHist {
    private String ticker;
    private long lastUpdate;
    private int step=3;
    private List<Tick> ticks=new ArrayList();

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<Tick> getTicks() {
        return ticks;
    }

    public void setTicks(List<Tick> ticks) {
        this.ticks = ticks;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
}
