package net.billforward;

import net.billforward.exception.BillforwardException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by birch on 25/05/2015.
 */
public class BillForwardGenerator<T> extends AbstractBillForwardGenerator<T> {
    protected EntityPagingHelper.ErrorHandler errorHandler;

    public BillForwardGenerator(EntityPagingHelper.ErrorHandler errorHandler) {
        super();
        this.errorHandler = errorHandler;
    }

    @Override
    protected void handleError(BillforwardException e) {
        errorHandler.handleError(e);
    }

    @Override
    protected void runThrows() throws BillforwardException {
        // extend this please
        throw new NotImplementedException();
    }
}
