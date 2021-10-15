package io.smallrye.graphql.test.coroutine;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.coroutines.awaitSuspending
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import java.time.LocalDate
import java.time.Month


@GraphQLApi
@Description("Allow all mutiny book releated APIs")
class CoroutineBookGraphQLApi {

    @Query("book")
    suspend fun getCoroutineBook(name:String) : Book? {
       val book1 = Book("0-571-05686-5", "Lord of the Flies", LocalDate.of(1954, Month.SEPTEMBER, 17), listOf("William Golding"))
        val BOOKS = mapOf(
            book1.title to book1
        )
        
        return Uni.createFrom().item(BOOKS[name]).awaitSuspending();
    }

//    @Query("failedBook")
//    public Uni<Book> failedBook(String name) {
//        return Uni.createFrom().failure(new CustomException());
//    }
    
//    private static Map<String, Book> BOOKS = new HashMap<>();
//    static {
//        Book book1 = new Book("0-571-05686-5", "Lord of the Flies", LocalDate.of(1954, Month.SEPTEMBER, 17), "William Golding");
//        BOOKS.put(book1.title, book1);
//
//        Book book2 = new Book("0-582-53008-3", "Animal Farm", LocalDate.of(1945, Month.AUGUST, 17), "George Orwell");
//        BOOKS.put(book2.title, book2);
//    }
}
