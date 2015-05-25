package net.billforward;

import com.sun.deploy.util.StringUtils;
import net.billforward.exception.*;
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

        try {
            Invoice currentInvoice = getCurrentInvoice();
            Invoice previousInvoice = getPreviousInvoice(currentInvoice);
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

    public static Invoice getPreviousInvoice(Invoice currentInvoice) throws Exception {
        if (currentInvoice.getInitialInvoice()) {
            throw new Exception("This is the first invoice; no previous invoice will exist.");
        }
        int pageSize = 1;
        int pageLimit = 100;
        for (int i=0; i<pageLimit; i++) {
            Invoice[] page = getPage(currentInvoice.getSubscriptionID(), i, pageSize);
            if (page == null || page.length < 1) {
                throw new Exception("Reached an empty page, and no matching entity exists");
            }
            for(Invoice invoice : page) {
                if (!invoice.getID().equalsIgnoreCase(currentInvoice.getID())
                        && invoice.getPeriodStart().before(currentInvoice.getPeriodStart())
                        && invoice.getPeriodEnd().equals(currentInvoice.getPeriodStart())
                        && invoice.getType().equals(Invoice.InvoiceType.Subscription)) {
                    return invoice;
                }
            }
            if (page.length < pageSize) {
                throw new Exception("Reached end of final page, and no matching entity exists");
            }
        }
        throw new Exception("Page limit reached without every encountering a matching entity");
    }

    public static Invoice[] getPage(String nominalInput, int page, int pageSize) throws BillforwardException {
        return Invoice.getBySubscriptionID(StringUtils.join(Arrays.asList(nominalInput,
                Stream.of(
                        new AbstractMap.SimpleEntry<>("records", pageSize),
                        new AbstractMap.SimpleEntry<>("offset", page * pageSize),
                        new AbstractMap.SimpleEntry<>("order_by", "created"),
                        new AbstractMap.SimpleEntry<>("order", "DESC"),
                        new AbstractMap.SimpleEntry<>("include_retired", false)
                ).map(x -> x.toString()).reduce((x, y) -> StringUtils.join(Arrays.asList(x, y), "&")).get()), "?"));
    };


}
