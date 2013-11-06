cmpe273-assignment2
===================
Purpose

The primary goal of this assignment is to understand all key aspects of messaging paradigm and how to use messaging in the application integration. In addition, you will be learned how to call a REST API from an offline scheduler process.

Use cases

When a user call the update book API of any library instance, the library instance should trigger sending a message to the designated queue.
The procurement service should periodically wake up, pull the book orders from the queue, aggregate them, and finally submit a single order to the publisher.
The procurement service should also notify to the libraries whenever the new books are arrived from the publisher (via GET pull) depending on the book category (STOMP topic) they have subscribed. Don’t need to check on whether or not the arrival from the publisher contains the books you ordered (from POST). The procurement service just publishes messages based on the subscribed topics listed on the message broker.

