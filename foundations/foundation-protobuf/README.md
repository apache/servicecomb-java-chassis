# Overview
There are many existing protobuf codecs, but all of them are not suit for us.  
ServiceComb data model is just POJO, not bind to any codec mechanism, so we had to create new one:  

|                                                          | ServiceComb   | protobuf | protostuff     | jackson |    
| -------------------------------------------------------- | :-----------: | :------: | :------------: | :-----: |   
| generate code | no need<br>just POJO | must<br>100+ lines IDL, generate 10000+ java code     | no need<br>POJO with Annotation(eg:@Tag) | no need |
| support null element in repeated field                   | no            | no       | not compatible | no      |
| support "oneOf"                                          | no            | yes      | no             | no      |
| support "packed"                                         | yes           | yes      | no             | no      |
| support "map"                                            | yes           | yes      | not compatible | no      |
| support "any"                                            | yes           | yes      | not compatible | no      |
| support "any" not defined in IDL                         | yes<br>extend based on "any" mechanism           | no       | not compatible | no      |
| support field: `List<List<X>>`                           | yes           | no       | not compatible | no      |
| support field: `List<Map<X, Y>>`                         | yes           | no       | not compatible | no      |
| support field: `Map<X, List<Y>>`                         | yes           | no       | not compatible | no      |
| support field: `Map<X, Map<Y, Z>>`                       | yes           | no       | not compatible | no      |
| support field: array                                     | yes           | no       | not compatible | no      |
| support generic POJO type, eg:`CustomGeneric<User>`      | yes           | no       | no             | no      |
| serialize/deserialize based on IDL                       | yes           | no       | no             | yes     |
| serialize not repeated number field ignore default value | yes           | yes      | no             | no      |
| serialize from map model                                 | yes           | no       | no             | no      |
| deserialize to map model                                 | yes           | no       | no             | no      |
   
# Usage
## Create factory  
  one factory instance globally is enough    
```java
ProtoMapperFactory factory = new ProtoMapperFactory();
```
## Load proto definition
  create mapper instance for each proto definition  
- load from classpath  
```java
ProtoMapper protoMapper = factory.createFromName("protobuf.proto");
```
- load from proto content 
```java
ProtoMapper protoMapper = factory.createFromContent(protoContent);
```
## Serialize
serializer is reusable and thread safe  
Assuming you have a proto definition
```proto
message User {
  string name = 1;
}
```
and a POJO class
```java
public class User {
  private String name;
  // getter and setter 
}
```
```java
RootSerializer serializer = protoMapper.createRootSerializer("User", User.class);

User user = new User();
user.setName("userName");
byte[] pojoBytes= serializer.serialize(user);

Map<String, Object> map = new HashMap<>();
map.put("name", "userName");
byte[] mapBytes = serializer.serialize(map);

// pojoBytes equals mapBytes
```
## Deserialize
deserializer is reusable and thread safe  
```java
RootDeserializer<User> pojoDeserializer = protoMapper.createRootDeserializer("User", User.class);
RootDeserializer<Map<String, Object>> mapDeserializer = protoMapper.createRootDeserializer("User", Map.class);

User user = pojoDeserializer.deserialize(bytes);
Map<String, Object> map = mapDeserializer.deserialize(bytes);
```

# Performance
```
1.protobuf
  in our real scenes
  business model never bind to transport, and can switch between different transports dynamically
  that means if we choose standard protobuf, must build protobuf models from business models each time
  so should be much slower than the test results
2.protoStuff
  some scenes, there is no field but have getter or setter, so we can not use unsafe to access field
  so we disable protoStuff unsafe feature
  
  for repeated fields, protoStuff have better performance, but not compatible to protobuf
  
3.jackson
  not support map/any/recursive, ignore related fields
4.serialize result size
  ScbStrong/ScbWeak/Protobuf have the same and smaller size, because skip all default/null value

Empty: 
                Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  : 519        515        240        288        1242       
ser len       : 36         0          0          0          56         
deser time(ms): 161        69         10         516        486        
deser->ser len: 36         0          0          0          56         
ser+deser(ms) : 680        584        250        804        1728       

Scalars: 
                Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  : 557        529        328        262        1357       
ser len       : 56         24         24         24         76         
deser time(ms): 181        141        115        527        504        
deser->ser len: 56         24         24         24         76         
ser+deser(ms) : 738        670        443        789        1861       

Pojo: 
                Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  : 571        574        276        309        1304       
ser len       : 46         10         10         10         66         
deser time(ms): 230        69         112        668        537        
deser->ser len: 46         10         10         10         66         
ser+deser(ms) : 801        643        388        977        1841       

SimpleList: 
                Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  : 590        609        296        637        1320       
ser len       : 68         32         32         32         88         
deser time(ms): 233        105        122        2226       541        
deser->ser len: 68         32         32         32         88         
ser+deser(ms) : 823        714        418        2863       1861       

PojoList: 
                Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  : 609        632        319        2777       1407       
ser len       : 56         20         20         20         76         
deser time(ms): 244        134        173        2287       679        
deser->ser len: 56         20         20         20         76         
ser+deser(ms) : 853        766        492        5064       2086       

Map: 
                Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  : 746        772        491        1079       1298       
ser len       : 92         54         54         54         56         
deser time(ms): 522        427        468        1031       422        
deser->ser len: 92         54         54         54         56         
ser+deser(ms) : 1268       1199       959        2110       1720       

Mixed: 
                Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  : 1686       1999       2034       2112       2537       
ser len       : 479        505        505        505        489        
deser time(ms): 1969       2154       2923       2984       3316       
deser->ser len: 479        505        505        505        489        
ser+deser(ms) : 3655       4153       4957       5096       5853         
```