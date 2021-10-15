package io.smallrye.graphql.bootstrap;

import graphql.schema.DataFetcher;
import io.smallrye.graphql.execution.datafetcher.*;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Wrapper;
import org.dataloader.BatchLoaderWithContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Create the datafetchers for a certain operation
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DataFetcherFactory {

    public <T> DataFetcher<T> getDataFetcher(Operation operation) {
        return (DataFetcher<T>) get(operation);
    }

    public <K, T> BatchLoaderWithContext<K, T> getSourceBatchLoader(Operation operation) {
        return (BatchLoaderWithContext<K, T>) get(operation);
    }

    public Wrapper unwrap(Field field, boolean isBatch) {
        if (isAsync(field) && isBatch) {
            return field.getWrapper().getWrapper().getWrapper();
        } else if (isAsync(field) && !isCoroutine(field)) {
            return field.getWrapper().getWrapper();
        } else if (isBatch) {
            return field.getWrapper().getWrapper();
        } else if (field.hasWrapper() && field.getWrapper().isCollectionOrArray()) {
            return field.getWrapper();
        } else if (field.hasWrapper()) {
            // TODO: Move Generics logic here ?
        }
        return null;
    }

    // TODO: Have some way to load custom ?
    private <V> V get(Operation operation) {
        if (isCompletionStage(operation)) {
            return (V) new CompletionStageDataFetcher(operation);
        } else if (isMutinyUni(operation)) {
            return (V) new UniDataFetcher(operation);
        } else if (isPublisher(operation)) {
            return (V) new PublisherDataFetcher(operation);
        } else if (isMutinyMulti(operation)) {
            return (V) new MultiDataFetcher(operation);
        } else if (isCoroutine(operation)) {
            try {
                Class.forName("io.smallrye.graphql.execution.datafetcher.CoroutineDataFetcher");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return (V) new CoroutineDataFetcher(operation);
        }
        return (V) new DefaultDataFetcher(operation);
    }

    private boolean isAsync(Field field) {
        return isCompletionStage(field) || isMutinyUni(field);
    }

    private boolean isCompletionStage(Field field) {
        if (field.hasWrapper()) {
            String wrapperClassName = field.getWrapper().getWrapperClassName();
            return wrapperClassName.equals(CompletableFuture.class.getName())
                    || wrapperClassName.equals(CompletionStage.class.getName());
        }
        return false;
    }

    private boolean isMutinyUni(Field field) {
        if (field.hasWrapper()) {
            String wrapperClassName = field.getWrapper().getWrapperClassName();
            return wrapperClassName.equals("io.smallrye.mutiny.Uni");
        }
        return false;
    }

    private boolean isPublisher(Field field) {
        if (field.hasWrapper()) {
            String wrapperClassName = field.getWrapper().getWrapperClassName();
            return wrapperClassName.equals("org.reactivestreams.Publisher");
        }
        return false;
    }

    private boolean isMutinyMulti(Field field) {
        if (field.hasWrapper()) {
            String wrapperClassName = field.getWrapper().getWrapperClassName();
            return wrapperClassName.equals("io.smallrye.mutiny.Multi");
        }
        return false;
    }

    private boolean isCoroutine(Field field) {
        if (field.hasWrapper()) {
            String wrapperClassName = field.getWrapper().getWrapperClassName();
            return wrapperClassName.equals("kotlin.coroutines.Continuation");
        }
        return false;
    }
}
