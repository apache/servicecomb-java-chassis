suppose jar named perf.jar
1.copy perf.jar to different directory

2.create microservice.yaml in each directory
# change microserviceName to be perf1/perf2/perf3, and so on
service_description:
  name: perf1
cse:
  references:
    # use which transport to invoke
    transport: rest
    
# sync mode consumer count
sync-count: 10
# async mode consumer count
async-count: 20
# use sync mode or not
# sync: /v1/syncQuery/{id}?step={step}&all={all}&fromDB={fromDB}
# async:/v1/asyncQuery/{id}?step={step}&all={all}&fromDB={fromDB}
sync: false
# producer microserviceName
producer: perf1
id: 1
# every producer determine:
#   1)if step equals all, then this is final step, direct return or query from db
#   2)otherwise inc step and invoke next microservice
#   3)if self name if perf1, then next microservice is perf2
step: 1
all: 1
fromDB: false
response-size: 1

# redis parameter
redis:
  client:
    count: 8
  host:
  port:
  password: 
  
3.start producers
java -jar perf.jar

4.start consumer
java -jar perf.jar -c