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

#### 4. Queue
A non-blocking and concurrent Queue which provides thread-safe operations using Lock-Free algorithms (Atomic classes).    

#### 4.1. Add/Append
Add operation allows elements to be appended to the tail concurrently. Following is an example
```
    LockFreeQueue<String> queue = LockFreeQueue.newQueue();
    queue.add("element1");
    queue.add("element2");
    
    assertEquals(2, queue.size());
```
 
#### 4.2. Poll
Poll operation will remove and return the head of the queue. If the queue is empty, then it will return 
_Optional.empty_ object. Following is an example 
```
    LockFreeQueue<String> queue = LockFreeQueue.newQueue();
    queue.add("element1");
    
    queue.poll().ifPresent(System.out::println);
```

#### 5. Map
A non-blocking and concurrent Map which provides thread-safe operations using Lock-Free algorithms (Atomic classes). 
This map relies on the same rules for _equals_ and _hashCode_ as in Java to put and 
lookup elements. Please do internet search to learn more about hashCode/equals contract in Java.  

#### 5.1. Insertion/Put
Insert or Put operation is thread-safe which allows us to insert/put elements concurrently. It is highly 
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
Assuming _equals_ method is implemented properly, the _value_ objects can be retrieved by using the same key, 
as shown below
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
    
    // getAll method should return all previously set values for the key 'key1'
    assertEquals(Arrays.asList("value1", "newValue1"), values);

    // get method will return the last set value.  
    final Optional<String> value = map.get("key1");
    assertTrue(value.isPresent());
    assertEquals("newValue1", "newValue1", value.get());
```

If there exists multiple values for the same key, then _get_ method will return the value which was put last.
However, in highly concurrent environment, the "last" element would be in-deterministic because of the race condition. 
In that case _getAll_ should be used to get all the values for that key.
     
#### 5.3. Deletion
_remove_ method can be used to remove all values for the given key, as shown below
```
    LockFreeMap<String, String> map = LockFreeMap.newMap();
    map.put("k1", "v1");
    map.put("k1", "v2");
    assertEquals(Arrays.asList("v1", "v2"), map.getAll("k1").collect(toList()));

    map.remove("k1");
    assertEquals(Collections.emptyList(), map.getAll("k1").collect(toList()));
```     

#### 5.4. LockFreeMap vs ConcurrentMap
JMH library is used to perform benchmarking between operations of _LockFreeMap_ and _ConcurrentHashMap_.

All the benchmarking classes pertaining to LockFreeMap can be found in _com.mfk.lockfree.benchmark.map_ package. 
If you are interested in running the benchmarking yourself, then execute the following command after mvn clean package:
  
```
java -jar jmh-benchmark/target/jmh-benchmark.jar "com.mfk.lockfree.benchmark.map.<class name>.*"
``` 


#### 5.4.1. Put Benchmarking
8 threads put 1000 elements concurrently. Elements produced unique hash code, so there would be 
less number of collisions. This was repeated 100 times.   

Following is the comparision of Average Response Time (avgt) using JMH benchmarking.

|Benchmark                        |Mode  |Cnt  |Score |  Error  |Units |
|---------------------------------|------|-----|------|---------|------|
|PutBenchmark.measureJavaMap      |avgt  | 100  |0.482 | ±0.002  |ms/op |
|PutBenchmark.measureLockFreeMap  |avgt  | 100  |0.491 | ±0.002  |ms/op |

#### 5.4.2. Put Benchmarking With Collisions
8 threads put 1000 elements concurrently. The _hashCode_ was poorly implemented to produce high rate of collisions. This was repeated 100 times.   

Following is the comparision of Average Response Time (avgt) using JMH benchmarking.

|Benchmark                                      |Mode  |Cnt  |Score   |  Error    |Units |
|-----------------------------------------------|------|-----|--------|-----------|------|
|PutWithCollisionsBenchmark.measureJavaMap      |avgt  | 100  |122.056 | ±1.222    |ms/op |
|PutWithCollisionsBenchmark.measureLockFreeMap  |avgt  | 100  |2.282   | ± 0.011   |ms/op |

#### 5.4.3. Get Benchmarking With Collisions
8 threads attempted to fetch elements 1 millions times concurrently.    

Following is the comparision of Average Response Time (avgt) using JMH benchmarking.

|Benchmark                        |Mode  |Cnt  |Score   |  Error    |Units |
|---------------------------------|------|-----|--------|-----------|------|
|GetBenchmark.measureJDKMap1      |avgt  | 100 |0.474   | ±0.044    |ms/op |
|GetBenchmark.measureLockFreeMap1 |avgt  | 100 |0.419   | ±0.001    |ms/op |

#### 6. Singleton Reference
Singleton Reference is a concurrent, non-blocking container of singleton which implements Singleton pattern using 
Atomic classes.

Assuming _HeavyObject_ is a user-defined class 
```
    SingleRef<HeavyObject> singleRef = new SingleRef<>(HeavyObject::new);
    HeavyObject singletonObj = singleRef.get();
```