## LockFree Library
#### 1. Introduction
LockFree library utilizes non-blocking Atomic classes to implement concurrent List, Queue and Map. 

#### 2. Jar Artifact
Group Id: com.mfk.lockfree <br/> 
Artifact Id: lockfree-library <br/>
Latest Version: 1.0

#### 3. Features
#### 3.1 Linked-Array Data Structure
In a typical Linked List, the new objects are appended to the <i>tail pointer</i>, allowing it to increase in size dynamically in O(1). 
The problem is that the Linked List is not CPU Cache friendly because CPU needs to re-load the L1/L2/L3 caches 
when accessing individual entry in a Linked List.

Arrays, on the other hand, provide stride-1 performance (if accessed linearly), but they are fixed size and, therefore, cannot 
be increased in size dynamically. Java's <i>ArrayList</i> solves this problem by creating a new array and copying over all the 
elements from the old list to the new one, but this effort is not so efficient when dealing with large amount of elements, 
millions or billions.         

The Linked-Array data structure gives the benefits of both worlds, Array and Linked List. This data structure is the 
combination of Linked List and Array data structures, referred to as <i>Linked Array</i> data structure. 
The objects are stored in fixed size arrays, referred to as <i>Fragments</i>, which are then chained together to form linked arrays. 
The fragment size is configurable; default value is 1000. All collections in this library uses this data structure internally.            

#### 3.2 LockFree (Atomic classes)
Atomic classes are used for concurrency, which are CAS based (if supported by underlying CPU). 
Please see JMH Benchmarking results to see the comparison between various other collections with the data structures 
offered in this library.      

#### 4. List
A non-blocking concurrent list which is highly optimized for concurrent append operation.    

#### 4.1. Example
Following code snippet is an example to append new objects
```
    LockFreeList<DataObject> list = LockFreeList.newList();
```   
   
#### 4.2. JMH Benchmarking
```
java -jar jmh-benchmark/target/jmh-benchmark.jar "com.mfk.lockfree.benchmark.LockFreeQueueBenchmark.*"
```

#### 5. Map
A concurrent map which provides thread-safe operations using Lock-Free algorithms (Atomic classes). 
This map relies on the same rules for {@code equals} and {@code hashCode} as in Java to put and 
lookup elements. Please do internet search to learn more about hashCode contract in Java.  

#### 5.1. Insertion/Put
Insertion is thread-safe which allows to insert/put elements concurrently. It is highly 
recommended to provide efficient and consistent implementation of _hashCode_ method. 

One important difference from Java's Map is that the put operation will not overwrite the existing value if put 
method is called multiple times for the same key, instead the value objects are retained and can be retrieved by 
using _getAll_ method. 

```
    final LockFreeMap<String, String> map = LockFreeMap.newMap();
    map.put("key1", "value1");
    map.put("key1", "newValue1");

    final List<String> values = map.getAll("key1").collect(Collectors.toList());
    assertEquals(Arrays.asList("value1", "newValue1"), values);
```

#### 5.2. Retrieval
Assuming _equals_ method is implemented properly, the _value_ objects can be retrieved by using the same key, as shown below
```
    final LockFreeMap<String, String> map = LockFreeMap.newMap();
    map.put("key1", "value1");
    assertEquals("value1", map.get("key1"));    
```

_getAll_ method can be used to retrieve all previously put values for the same key, as shown below  
```
    LockFreeMap<String, String> map = LockFreeMap.newMap();
    map.put("key1", "value1");
    map.put("key1", "newValue1");

    final List<String> values = map.getAll("key1").collect(Collectors.toList());
    
    // should return all previously set values for the key 'key1'
    assertEquals(Arrays.asList("value1", "newValue1"), values);

    // get method will return the last set value.  
    final Optional<String> value = map.get("key1");
    assertTrue(value.isPresent());
    assertEquals("newValue1", "newValue1", value.get());
```

If there exists multiple values for the same key, then _get_ method will return the value which was last set.
However, in highly concurrent environment, the term 'last' could be relative. In that case _getAll_ should be used 
to get other potential values.  