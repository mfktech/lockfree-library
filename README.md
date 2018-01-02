## Lock-Free Library
### 1. Introduction
LockFree collections utilize non-blocking Atomic classes to implement concurrent List, Queue and Map. 

### 2. Maven
Group Id: com.mfk.lockfree <br/> 
Artifact Id: lockfree-library <br/>
Version: 1.0

### 3. List
A non-blocking concurrent list which is highly optimized for concurrent append operation.    

#### 3.1 Data Structure
Underlying data structure is the combination of linked list and array data structures. The objects are stored in fixed size arrays, referred to as <i>Fragments</i>, which are then chained together to form "linked arrays". The fragment size is configurable with the default value of 1000.<br/>
The data structure allows us to combine the benefits of both worlds, arrays and linked-list. In a typical linked list, the new object is appended to the tail and moving the tail pointer to the new that new object, which allows it to increase in size indefintely.          
   

### 4. JMH Benchmarking
java -jar jmh-benchmark/target/jmh-benchmark.jar "com.mfk.lockfree.benchmark.LockFreeQueueBenchmark.*"