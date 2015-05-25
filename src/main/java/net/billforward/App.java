package net.billforward;

import com.zoominfo.util.yieldreturn.Generator;
import net.billforward.exception.*;
import net.billforward.model.BillingEntity;
import net.billforward.model.Invoice;

import java.util.*;
import java.util.stream.Stream;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        BillForwardClient.makeDefaultClient("48ccf496-26b9-42f5-b11e-56a6a2171db8", "http://local.billforward.net:8089/RestAPI");

        App myApp = new App();

        try {
            myApp.getPreviousInvoice(getCurrentInvoice());
        } catch(Exception e) {
            System.err.println("Sad trombone :(");
            System.err.println(e);
        }
    }

    public static Invoice getCurrentInvoice() throws BillforwardException {
        return Invoice.getByID("INV-0A46471A-5702-49E2-AED4-3DBE57E7");
    }


    public void getPreviousInvoice(Invoice currentInvoice) throws BillforwardException {
        //fetchEntitiesUntilConditionFulfilled<Invoice>(currentInvoice.getSubscriptionID());
        EntityPagingHelper<Invoice> pagingHelper = new EntityPagingHelper<>();
        Iterator<Invoice> iterator = pagingHelper.fetchEntities(Invoice::getBySubscriptionID, currentInvoice.getSubscriptionID()).iterator();
        while(iterator.hasNext()) {
            Invoice candidateInvoice = iterator.next();

        }

        //System.out.println(Arrays.toString(i));
    }
}
