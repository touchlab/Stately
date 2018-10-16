# Stately

Stately is a state utility library to support some multithreading features in Kotlin Multiplatform

Kotlin/Native has a fairly different model of concurrency than what JVM developers are used to. This includes very 
different rules around sharing state. While a lot of these changes will be good, there are some cases where state 
shared across threads is desired and not well supported out of the box.

As architectural models evolve, structures in this library may become less desired, but currently there is some need
to support global mutable state.

## Status

Super early. Still adding and testing structures.

## Collections

### Copy On Write

There is a simple "copy on write" list implementation that uses CopyOnWriteArrayList on JVM, and a list backed by
a frozen ArrayList on native. This is appropriate for situations where you need iterations to be stable, and have small
list sizes, infrequent writes, or ideally both. All edits to the underlying list trigger a new list and a copy operation.
Only list references are copied. The underlying data keeps the same object instances. All of these obviously need to be 
frozen immutables.

### Shared Linked List

Rather than copying the full list on each edit, this structure uses atomic references in the nodes to allow modification
that is reasonably performant in situations that may have larger lists and/or frequent updates. This is probably not
suitable in situations that require extreme performance, but currently the primary goal is correct functionality and 
not optimization.

This is *not* a lockless algorithm. There are lockless versions for linked lists, but for the sake of simplicity, the 
list currently doesn't attempt to use them. Updates lock, but generally don't do much during a change and should finish
quickly.

There are two iterating versions.

### SharedLinkedList

Iterators aren't stable in the sense that if the list is modified by another thread while you're iterating, the iterator
will reflect that change. It will function. For example, if you're currently "at" a node that gets removed, calling
next will give you what was next in the list. If a node in front of you gets removed, you'll get whatever was after it
when calling next. Iterators do not lock the list on access. As a result, full iterations are fast, but while iterating,
the state of the list may change.

### CopyOnWriteLinkedList

Iterators are stable, in the sense that whatever the state of the list when you create the iterator, that state remains
throughout the iteration, regardless of mutations to the linked list. This is accomplished by locking the list when
the iterator is created, and if there have been modifications since the last iterator was created, a new copy of the 
list is created. Multiple iterators created without modifications in the interim will get the same list copy.

In theory this is better than the other copy on write implementation as the 'copy' isn't created until you need one, 
but if there are large lists, when an iterator is requested, it'll lock out other access while the copy is made.

The implementation is relatively simple and might have some improvement potential.