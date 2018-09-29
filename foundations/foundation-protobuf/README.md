# Overview
There are many existing protobuf codecs, but not suit for us, so we create new one:
- official, must generate code
  - 40+ lines proto file, generate 5000+ java code
  - bind business model to protobuf, but our business can switch different codecs dynamically
  - strong type, there is no business jar in edge service, no strong types
- protostuff
  - strong type
  - map codec is not compatible to protobuf
- jackson
  - not support map/any type
  - performance is not so good
   
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
RootSerializer serializer = protoMapper.findRootSerializer("User");

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
RootDeserializer pojoDeserializer = protoMapper.createRootDeserializer(User.class, "User");
RootDeserializer mapDeserializer = protoMapper.createRootDeserializer(Map.class, "User");

User user = pojoDeserializer.deserialize(bytes);
Map<String, Object> map = mapDeserializer.deserialize(bytes);
```

# Features
- compare to official protobuf:
  - extend "any" type, for standard not support cases, use "json" schema to codec it.
- compare to protoStuff runtime:
  - for a proto message type, not only support strong type(Pojo), but alse support weak type(Map)
  - support "any" type
  - support generic pojo type, eg:CustomGeneric&lt;User&gt;
  - **NOT** support List<List<XXX>>/List<Map<X, Y>> any more, because protobuf specification not support it, and the parser can not parse the proto file
- compare to jackson protobuf:
  - can parse protobuf 3 proto file
  - support protobuf 3: map/any
- compare to all:
  - just pojo, no need any code generation and annotation
  - one model can serialize to different version proto file to support different version server
  - support text data come from http,can serrialize from different data type
    - number fields (int32/int64 and so on)
      - number
      - String
      - String[]
    - string fields
      - string
      - string[]
    - bool fields
      - boolean
      - string
      - string[]
    - enum fields
      - enum
      - number
      - string
      - string[]
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
3.jackson
  not support map, so skip map in map/mixed test
4.serialize result size
  ScbStrong/ScbWeak/Protobuf have the same and smaller size, because skip all default/null value
  
Empty:
               Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  :250        250        235        156        437        
ser len       :36         0          0          0          56         
deser time(ms):125        15         0          257        483        
deser-ser len :36         0          0          0          56         

Scalars:
               Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  :235        264        218        156        413        
ser len       :53         21         21         21         73         
deser time(ms):156        63         94         225        469        
deser-ser len :53         21         21         21         73         

SimpleList:
               Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  :266        250        220        172        440        
ser len       :68         32         32         32         88         
deser time(ms):234        94         109        265        499        
deser-ser len :68         32         32         32         88         

PojoList:
               Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  :297        343        235        187        543        
ser len       :56         20         20         20         76         
deser time(ms):211        126        168        298        610        
deser-ser len :56         20         20         20         76         

Map:
               Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  :404        512        424        533        403        
ser len       :92         54         54         54         56         
deser time(ms):500        343        406        750        359        
deser-ser len :92         54         54         54         56         

Mixed:
               Protostuff ScbStrong  ScbWeak    Protobuf   Jackson    
ser time(ms)  :579        704        547        579        625        
ser len       :161        127        127        127        125        
deser time(ms):736        623        766        1015       798        
deser-ser len :161        127        127        127        125      
```