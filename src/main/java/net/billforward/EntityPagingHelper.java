package net.billforward;

import com.zoominfo.util.yieldreturn.Generator;
import net.billforward.exception.BillforwardException;
import net.billforward.model.BillingEntity;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Created by birch on 25/05/2015.
 */
public class EntityPagingHelper<T extends BillingEntity> {
    public T fetchFirstEntityMeetingCondition(EntityReckoner<T> entityReckoner, EntityFetcher<T> entityFetcher, String nominalInput, ErrorHandler errorHandler) throws BillforwardException {
        Iterator<T> iterator = fetchEntities(entityFetcher, nominalInput, errorHandler).iterator();
        while(iterator.hasNext()) {
            T candidateEntity = iterator.next();
            if (entityReckoner.suffices(candidateEntity)) {
                return candidateEntity;
            }
        }
        return null;
    }

    public Iterable<T> fetchEntities(EntityFetcher<T> entityFetcher, String nominalInput, ErrorHandler errorHandler) {
        return new BillForwardGenerator<T>() {
            @Override protected void runThrows() throws BillforwardException {
                Iterator<T[]> iterator = fetchPages(entityFetcher, nominalInput, errorHandler).iterator();
                while(iterator.hasNext()) {
                    T[] page = iterator.next();
                    Arrays.stream(page)
                            .forEach(thisPage -> yield(thisPage));
                }
            }
        };
    }

    public Iterable<T[]> fetchPages(EntityFetcher<T> entityFetcher, String nominalInput, ErrorHandler errorHandler) {
        int pageLimit = 1;
        int pageSize = 1;
        return new BillForwardGenerator<T[]>() {
            @Override protected void runThrows() throws BillforwardException {
                for (int i = 0; i < pageLimit; i++) {
                    yield(getPage(entityFetcher, nominalInput, i, pageSize));
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

    public interface ErrorHandler {
        BillforwardException handleError(BillforwardException e);
    }

    public T[] getPage(EntityFetcher<T> entityFetcher, String nominalInput, int page, int pageSize) throws BillforwardException {
        String queryString = String.format("%s?%s", nominalInput,
                Stream.of(
                        new AbstractMap.SimpleEntry<>("records", pageSize),
                        new AbstractMap.SimpleEntry<>("offset", page * pageSize),
                        new AbstractMap.SimpleEntry<>("order_by", "created"),
                        new AbstractMap.SimpleEntry<>("order", "DESC"),
                        new AbstractMap.SimpleEntry<>("include_retired", false)
                ).map(x -> x.toString()).reduce((x, y) -> String.format("%s&%s", x, y)).get()
        );
        return entityFetcher.run(queryString);
    };
}