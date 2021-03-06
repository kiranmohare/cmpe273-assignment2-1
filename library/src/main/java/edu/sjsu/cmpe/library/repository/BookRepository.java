package edu.sjsu.cmpe.library.repository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.messaging.Producer;
import edu.sjsu.cmpe.library.domain.Book.Status;

@SuppressWarnings("unused")
public class BookRepository implements BookRepositoryInterface 
{
    /** In-memory map to store books. (Key, Value) -> (ISBN, Book) */
    private final ConcurrentHashMap<Long, Book> bookInMemoryMap;
    private final LibraryServiceConfiguration configuration;
    
    /** Never access this key directly; instead use generateISBNKey() */
    
    private long isbnKey;
    private String stompQueueName;
    private String stompTopicName;
    private String apolloUser;
    private String apolloPassword;
    private String apolloHost;
    private int apolloPort;
    private String stompQueue;
    private final Producer producer;
        
    public BookRepository(LibraryServiceConfiguration config) 
    {
    	bookInMemoryMap = seedData();
    	this.configuration = config;
    	System.out.println("checkpoint");
    	this.producer = new Producer(config);
    	apolloUser = configuration.getApolloUser();
    	apolloPassword = configuration.getApolloPassword();
    	apolloHost = configuration.getApolloHost();
    	apolloPort = configuration.getApolloPort();
    	stompQueueName = configuration.getStompQueueName();
    	stompTopicName = configuration.getStompTopicName();
    	System.out.println(apolloUser);
	    isbnKey = 2;
    }

    private ConcurrentHashMap<Long, Book> seedData()
    {
	ConcurrentHashMap<Long, Book> bookMap = new ConcurrentHashMap<Long, Book>();
	Book book = new Book();
	book.setIsbn(1);
	book.setCategory("computer");
	book.setTitle("Java Concurrency in Practice");
	try {
	    book.setCoverimage(new URL("http://goo.gl/N96GJN"));
	} catch (MalformedURLException e) {
	    // eat the exception
	}
	bookMap.put(book.getIsbn(), book);

	book = new Book();
	book.setIsbn(2);
	book.setCategory("computer");
	book.setTitle("Restful Web Services");
	try {
	    book.setCoverimage(new URL("http://goo.gl/ZGmzoJ"));
	} catch (MalformedURLException e) {
	    // eat the exception
	}
	bookMap.put(book.getIsbn(), book);

	return bookMap;
    }

    /**
     * This should be called if and only if you are adding new books to the
     * repository.
     * 
     * @return a new incremental ISBN number
     */
    private final Long generateISBNKey() {
	// increment existing isbnKey and return the new value
	return Long.valueOf(++isbnKey);
    }

    /**
     * This will auto-generate unique ISBN for new books.
     */
    @Override
    public Book saveBook(Book newBook) {
	checkNotNull(newBook, "newBook instance must not be null");
	// Generate new ISBN
	Long isbn = generateISBNKey();
	newBook.setIsbn(isbn);
	// TODO: create and associate other fields such as author

	// Finally, save the new book into the map
	bookInMemoryMap.putIfAbsent(isbn, newBook);

	return newBook;
    }

    /**
     * @see edu.sjsu.cmpe.library.repository.BookRepositoryInterface#getBookByISBN(java.lang.Long)
     */
    @Override
    public Book getBookByISBN(Long isbn) 
    {
	checkArgument(isbn > 0,
		"ISBN was %s but expected greater than zero value", isbn);
	if(bookInMemoryMap.containsKey(isbn)) {
        //System.out.println("returning book..");
        return bookInMemoryMap.get(isbn);
}
	else {
        //System.out.println("returning null");
        Book book = new Book();
        return book;
}
    }

    @Override
    public List<Book> getAllBooks() {
	return new ArrayList<Book>(bookInMemoryMap.values());
    }

    /*
     * Delete a book from the map by the isbn. If the given ISBN was invalid, do
     * nothing.
     * 
     * @see
     * edu.sjsu.cmpe.library.repository.BookRepositoryInterface#delete(java.
     * lang.Long)
     */
    
    @Override
    public void delete(Long isbn) 
    
    {
	    bookInMemoryMap.remove(isbn);
    }
    
    public void addBook(Book tempBook) 
    
    {
    	checkNotNull(tempBook, "newBook instance must not be null");
    	System.out.println(tempBook.getTitle());
    	bookInMemoryMap.put(tempBook.getIsbn(), tempBook);
    }
    
   
    @Override
	public Book updateBookStatus(Book book, Status tempStatus) throws JMSException{
		book.setStatus(tempStatus); 
		
		String libname;
		if(configuration.getStompTopicName().equals("/topic/70742.book.all"))
			libname = "library-a";
		else
			libname = "library-b";
		producer.producer(libname+":"+book.getIsbn());
		
	return book;
}
    
  }
