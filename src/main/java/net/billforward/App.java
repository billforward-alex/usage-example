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

        try {
            getPreviousInvoice(getCurrentInvoice());
        } catch(Exception e) {
            System.err.println("Sad trombone :(");
            System.err.println(e);
        }
    }

    public static Invoice getCurrentInvoice() throws BillforwardException {
        return Invoice.getByID("INV-0A46471A-5702-49E2-AED4-3DBE57E7");
    }


    public static void getPreviousInvoice(Invoice currentInvoice) throws BillforwardException {
        //fetchEntitiesUntilConditionFulfilled<Invoice>(currentInvoice.getSubscriptionID());
        EntityPagingHelper<Invoice> pagingHelper = new EntityPagingHelper<>();
        Iterator<Invoice> iterator = pagingHelper.fetchEntities(Invoice::getBySubscriptionID, currentInvoice.getSubscriptionID()).iterator();
        while(iterator.hasNext()) {
            Invoice candidateInvoice = iterator.next();

        }

        //System.out.println(Arrays.toString(i));
    }

    public static class EntityPagingHelper<T extends BillingEntity> {
        public T fetchFirstEntityMeetingCondition(EntityReckoner<T> entityReckoner, EntityFetcher<T> entityFetcher, String nominalInput) throws BillforwardException {
            Iterator<T> iterator = fetchEntities(entityFetcher, nominalInput).iterator();
            while(iterator.hasNext()) {
                T candidateEntity = iterator.next();
                if (entityReckoner.suffices(candidateEntity)) {
                    return candidateEntity;
                }
            }
            return null;
        }

        public Iterable<T> fetchEntities(EntityFetcher<T> entityFetcher, String nominalInput) {
            return new Generator<T>() {
                @Override protected void run() {
                    Iterator<T[]> iterator = fetchPages(entityFetcher, nominalInput).iterator();
                    while(iterator.hasNext()) {
                        T[] page = iterator.next();
                        Arrays.stream(page)
                                .forEach(thisPage -> yield(thisPage));
                    }
                }
            };
        }

        public Iterable<T[]> fetchPages(EntityFetcher<T> entityFetcher, String nominalInput) {
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

        public interface EntityReckoner<T> {
            boolean suffices(T entity) throws BillforwardException;
        }

        public interface EntityFetcher<T> {
            T[] run(String nominalInput) throws BillforwardException;
        }

        public T[] getPage(EntityFetcher<T> entityFetcher, String nominalInput, int page, int pageSize) throws BillforwardException {
            String queryString = String.format("%s?%s", nominalInput,
                    Stream.of(
                            new AbstractMap.SimpleEntry<>("records", pageSize),
                            new AbstractMap.SimpleEntry<>("offset", page*pageSize),
                            new AbstractMap.SimpleEntry<>("order_by", "created"),
                            new AbstractMap.SimpleEntry<>("order", "DESC"),
                            new AbstractMap.SimpleEntry<>("include_retired", false)
                    ).map(x -> x.toString()).reduce((x, y) -> String.format("%s&%s", x, y)).get()
            );
            return entityFetcher.run(queryString);
        };
    }
}
