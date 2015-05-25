package net.billforward;

import com.zoominfo.util.yieldreturn.Generator;
import net.billforward.exception.BillforwardException;

/**
 * Created by birch on 25/05/2015.
 */
public abstract class BillForwardGenerator<T> extends Generator<T> {
    @Override
    protected void run() {
        try {
            runThrows();
        } catch (BillforwardException e) {
            return;
        }
    }

    protected abstract void runThrows() throws BillforwardException;
}
