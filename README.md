## LockFree Library
### 1. Introduction
LockFree library utilize non-blocking Atomic classes to implement concurrent List, Queue and Map. 

### 2. Jar Artifact
Group Id: com.mfk.lockfree <br/> 
Artifact Id: lockfree-library <br/>
Latest Version: 1.0

### 3. Features
### 3.1 Linked-Array Data Structure
In a typical Linked List, the new objects are appended to the <i>tail pointer</i>, allowing it to increase in size dynamically in O(1). 
The problem is that the Linked List is not CPU Cache friendly because CPU needs to re-load the L1/L2/L3 caches 
when accessing individual entries in a Linked List.

Arrays, on the other hand, provide stride-1 performance (if accessed linearly), but they are fixed size and, therefore, cannot 
be increased in size dynamically. Java's <i>ArrayList</i> solves this problem by creating a new array and copying over all the 
elements from the old list to the new one, but this effort is not so efficient when dealing with large amount of elements, 
millions or billions.         

The Linked-Array data structure gives the benefits of both worlds, Array and Linked List. data structure is the 
combination of Linked List and Array data structures, referred to as <i>Linked Array</i> data structure. 
The objects are stored in fixed size arrays, referred to as <i>Fragments</i>, which are then chained together to form linked arrays. 
The fragment size is configurable; default value is 1000. All collections in this library uses this data structure internally.            

### 3.2 LockFree (Atomic classes)


### 4. List
A non-blocking concurrent list which is highly optimized for concurrent append operation.    

   
### 4.1.  JMH Benchmarking
java -jar jmh-benchmark/target/jmh-benchmark.jar "com.mfk.lockfree.benchmark.LockFreeQueueBenchmark.*"