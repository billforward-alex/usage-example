package net.billforward;

import com.sun.deploy.util.StringUtils;
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
            Invoice currentInvoice = getCurrentInvoice();
            Invoice previousInvoice = myApp.getPreviousInvoice(currentInvoice);
            System.out.println(StringUtils.join(Arrays.asList(
                "Departed period:",
                String.format("\tStart:\t%s", previousInvoice.getPeriodStart()),
                String.format("\tEnd:\t%s", previousInvoice.getPeriodEnd()),
                "Coming period:",
                String.format("\tStart:\t%s", currentInvoice.getPeriodStart()),
                String.format("\tEnd:\t%s", currentInvoice.getPeriodEnd())
            ), "\n"));
        } catch(Exception e) {
            System.err.println("Sad trombone :(");
            System.err.println(e);
        }
    }

    public static Invoice getCurrentInvoice() throws BillforwardException {
        // hardcoded, for demonstration purposes
        return Invoice.getByID("INV-0A46471A-5702-49E2-AED4-3DBE57E7");
    }

    public Invoice getPreviousInvoice(Invoice currentInvoice) {
        EntityPagingHelper<Invoice> pagingHelper = new EntityPagingHelper<>(e -> {
            System.err.println("An error occurred");
            System.err.println(e);
        }, 10, 200);
        return pagingHelper.fetchFirstEntityMeetingCondition(invoice -> !invoice.getID().equalsIgnoreCase(currentInvoice.getID())
                && invoice.getPeriodStart().before(currentInvoice.getPeriodStart())
                && invoice.getPeriodEnd().equals(currentInvoice.getPeriodStart())
                && invoice.getType().equals(Invoice.InvoiceType.Subscription), Invoice::getBySubscriptionID, currentInvoice.getSubscriptionID());

    }
}
