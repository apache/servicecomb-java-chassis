# About edge service API compatibility

* Edge service use the latest version of the microservice meta. e.g. For business 1.0.0, 1.1.0, 2.0.0 have the following APIs:

    * 1.0.0: /business/v1/add
    * 1.1.0: /business/v1/add, /business/v1/dec
    * 2.0.0: /business/v2/add, /business/v2/dec

    If users invoke /business/v1/add, edge service will give NOT FOUND, because 2.0.0 microservice meta do not have this API. Even using router to route all /business/v1/* requests to 1.1.0, path locating happens before load balance.

* It's very important to keep your API compatibility cross versions if these versions need work together. e.g.

    * 1.0.0: /business/v1/add
    * 1.1.0: /business/v1/add, /business/v1/dec
    * 2.0.0: /business/v1/add, /business/v1/dec, /business/v2/add, /business/v2/dec

    Together with router, /business/v1/add will go correctly to 1.0.0 or 1.1.0, and /business/v2/add will go correctly to 2.0.0. Without router, /business/v2/add may route to 1.0.0 or 1.1.0 and NOT FOUND is reported.


