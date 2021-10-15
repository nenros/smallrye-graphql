package io.smallrye.graphql.test.coroutine

import java.time.LocalDate

data class Book(
    var isbn: String? = null,
    var title: String? = null,
    var published: LocalDate? = null,
    var authors: List<String>? = null
)
