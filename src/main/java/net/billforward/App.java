package net.billforward;

import net.billforward.exception.*;
import net.billforward.model.Invoice;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
            getPreviousInvoice();
        } catch(Exception e) {
            System.err.println("Sad trombone :(");
            System.err.println(e);
        }
    }
    public static void getPreviousInvoice() throws CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException {
//        Invoice i = Invoice.getByID("INV-0A46471A-5702-49E2-AED4-3DBE57E7");

        String queryString = String.format("?%s", Stream.of(
                new AbstractMap.SimpleEntry<>("records", 1),
                new AbstractMap.SimpleEntry<>("offset", 0),
                new AbstractMap.SimpleEntry<>("order_by", "created"),
                new AbstractMap.SimpleEntry<>("order", "DESC"),
                new AbstractMap.SimpleEntry<>("include_retired", false)
        ).map(x -> x.toString()).reduce((x, y) -> {
            return String.format("%s&%s", x, y);
        }).get());
        Invoice[] i = Invoice.getBySubscriptionID("SUB-4A1981A5-4816-47F2-AC5B-7B1B46C5");
        //System.out.println(Arrays.toString(i));
    }
}
