package net.billforward;

import com.sun.deploy.util.StringUtils;
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
    protected EntityPagingHelper.ErrorHandler errorHandler;
    protected int pageSize;
    protected int pageLimit;

    public EntityPagingHelper() {
        this(e -> {
            System.err.println("An error occurred");
            System.err.println(e);
        }, 10, 200);
    }

    public EntityPagingHelper(EntityPagingHelper.ErrorHandler errorHandler, int pageSize, int pageLimit) {
        this.errorHandler = errorHandler;
        this.pageSize = pageSize;
        this.pageLimit = pageLimit;
    }

    public T fetchFirstEntityMeetingCondition(EntityReckoner<T> entityReckoner, EntityFetcher<T> entityFetcher, String nominalInput) {
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
        return new BillForwardGenerator<T>(errorHandler) {
            @Override protected void runThrows() throws BillforwardException {
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
        return new BillForwardGenerator<T[]>(errorHandler) {
            @Override protected void runThrows() throws BillforwardException {
                for (int i = 0; i < pageLimit; i++) {
                    T[] page = getPage(entityFetcher, nominalInput, i, pageSize);
                    if (page == null || page.length < 1) {
                        // empty page; we're done here
                        break;
                    }
                    yield(page);
                    if (page.length < pageSize) {
                        // incomplete page; we're done here
                        break;
                    }
                }
            }
        };
    }

    public interface EntityReckoner<T> {
        boolean suffices(T entity);
    }

    public interface EntityFetcher<T> {
        T[] run(String nominalInput) throws BillforwardException;
    }

    public interface ErrorHandler {
        void handleError(BillforwardException e);
    }

    public T[] getPage(EntityFetcher<T> entityFetcher, String nominalInput, int page, int pageSize) throws BillforwardException {
        return entityFetcher.run(StringUtils.join(Arrays.asList(nominalInput,
                Stream.of(
                        new AbstractMap.SimpleEntry<>("records", pageSize),
                        new AbstractMap.SimpleEntry<>("offset", page * pageSize),
                        new AbstractMap.SimpleEntry<>("order_by", "created"),
                        new AbstractMap.SimpleEntry<>("order", "DESC"),
                        new AbstractMap.SimpleEntry<>("include_retired", false)
                ).map(x -> x.toString()).reduce((x, y) -> StringUtils.join(Arrays.asList(x, y), "&")).get()), "?"));
    };
}