package net.billforward;

import com.zoominfo.util.yieldreturn.Generator;
import net.billforward.exception.*;
import net.billforward.model.BillingEntity;
import net.billforward.model.Invoice;

import java.util.*;
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
            getPreviousInvoice(getCurrentInvoice());
        } catch(Exception e) {
            System.err.println("Sad trombone :(");
            System.err.println(e);
        }
    }

    public static Invoice getCurrentInvoice() throws CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException {
        return Invoice.getByID("INV-0A46471A-5702-49E2-AED4-3DBE57E7");
    }


    public static void getPreviousInvoice(Invoice currentInvoice) throws CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException {
        //fetchEntitiesUntilConditionFulfilled<Invoice>(currentInvoice.getSubscriptionID());
        //System.out.println(Arrays.toString(i));
    }

    public static class EntityPagingHelper<T extends BillingEntity> {
        public Iterable<T> fetchEntitiesUntilConditionFulfilled(EntityFetcher<T> entityFetcher, String nominalInput, EntitySuitableLambda<Invoice> condition) {
            return new Generator<T>() {
                @Override protected void run() {
                    Iterator<T[]> iterator = fetchPagesUntilConditionFulfilled(entityFetcher, nominalInput).iterator();
                    while(iterator.hasNext()) {
                        T[] page = iterator.next();
                        Arrays.stream(page)
                                .forEach(thisPage -> yield(thisPage));
                    }
                }
            };
        }

        public Iterable<T[]> fetchPagesUntilConditionFulfilled(EntityFetcher<T> entityFetcher, String nominalInput) {
            int pageLimit = 1;
            int pageSize = 1;
            return new Generator<T[]>() {
                @Override protected void run() {
                    for (int i = 0; i < pageLimit; i++) {
                        try {
                            yield(getPage(entityFetcher, nominalInput, i, pageSize));
                        } catch (Exception e) {
                            return;
                        }
                    }
                }
            };
        }

        interface EntitySuitableLambda<T> {
            boolean run(T input);
        }

        interface EntityFetcher<T> {
            T[] run(String nominalInput);
        }

        public T[] getPage(EntityFetcher<T> entityFetcher, String nominalInput, int page, int pageSize) throws CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException {
            String queryString = String.format("%s?%s", nominalInput,
                    Stream.of(
                            new AbstractMap.SimpleEntry<>("records", pageSize),
                            new AbstractMap.SimpleEntry<>("offset", page*pageSize),
                            new AbstractMap.SimpleEntry<>("order_by", "created"),
                            new AbstractMap.SimpleEntry<>("order", "DESC"),
                            new AbstractMap.SimpleEntry<>("include_retired", false)
                    ).map(x -> x.toString()).reduce((x, y) -> {
                        return String.format("%s&%s", x, y);
                    }).get()
            );
            return entityFetcher.run(queryString);
        };
    }
}
