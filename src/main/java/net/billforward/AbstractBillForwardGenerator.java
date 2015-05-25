package net.billforward;

import com.zoominfo.util.yieldreturn.Generator;
import net.billforward.EntityPagingHelper.ErrorHandler;
import net.billforward.exception.BillforwardException;

/**
 * Created by birch on 25/05/2015.
 */
public abstract class AbstractBillForwardGenerator<T> extends Generator<T> {
    @Override
    protected void run() {
        try {
            runThrows();
        } catch (BillforwardException e) {
            handleError(e);
        }
    }

    protected abstract void handleError(BillforwardException e);

    protected abstract void runThrows() throws BillforwardException;
}
