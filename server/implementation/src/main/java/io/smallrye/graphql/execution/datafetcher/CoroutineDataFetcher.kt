package io.smallrye.graphql.execution.datafetcher

import graphql.GraphQLContext
import graphql.execution.DataFetcherResult
import graphql.schema.DataFetchingEnvironment
import io.smallrye.graphql.execution.datafetcher.helper.KotlinReflectionHelper
import io.smallrye.graphql.schema.model.Operation
import kotlinx.coroutines.*
import org.dataloader.BatchLoaderEnvironment
import java.util.concurrent.CompletionStage


class CoroutineDataFetcher<K, T>(operation: Operation) : AbstractDataFetcher<K, T>(operation) {
    override fun load(p0: MutableList<K>?, p1: BatchLoaderEnvironment?): CompletionStage<MutableList<T>> {
        TODO("Not yet implemented")
    }

    private val kotlinReflectionHelper: KotlinReflectionHelper = KotlinReflectionHelper(operation, eventEmitter)

    override fun <T : Any?> invokeAndTransform(
        dfe: DataFetchingEnvironment?,
        resultBuilder: DataFetcherResult.Builder<Any>?,
        transformedArguments: Array<out Any>
    ): T? {
        val context = dfe?.graphQlContext?.get<GraphQLContext>("context")
        val result = runBlocking {
            kotlinReflectionHelper.invoke<T>(transformedArguments)
        }
        return result

    }

    override fun <T : Any?> invokeFailure(resultBuilder: DataFetcherResult.Builder<Any>?): T {
        TODO("Not yet implemented")
    }
}
