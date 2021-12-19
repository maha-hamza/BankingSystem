# Getting Started

The System represents a part of banking system to transfer and deposit money into and between accounts under specific
criterias

P.S. To handle concurrency, DB level locking should be considered in addition usage of flag which is used to detect
current processing on the account and add the transaction as pending which will be invoked every time interval through
cron job, to grantee realistic results.

### Tech Used

* [Kotlin](https://kotlinlang.org/)
* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.6.0/reference/htmlsingle/#boot-features-developing-web-applications)
* [Flyway Migration](https://docs.spring.io/spring-boot/docs/2.6.0/reference/htmlsingle/#howto-execute-flyway-database-migrations-on-startup)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/2.6.0/reference/htmlsingle/#boot-features-jpa-and-spring-data)

### APIs

#### Account Controller

<table>
<tr><td>EndPoint</td><td>RequestType</td><td>RequestBody</td></tr>
<tr><td>/account/lock</td><td>Patch</td><td>{"iban": ""}</td></tr>
<tr><td>/account/unlock</td><td>Patch</td><td>{"iban": ""}</td></tr>
<tr><td>/account/create</td><td>Post</td><td>{"customerId":"", accountType:""}</td></tr>
<tr><td>/account/accountType/filter</td><td>Get</td><td>{"customerId": "","accountsType": ["","",...]}</td></tr>
<tr><td>/account/balance</td><td>Get</td><td>{"accountNumber": "","iban": ""}</td></tr>
<tr><td>/account/deposit</td><td>Post</td><td>{{"iban": "","amount": #}}</td></tr>
</table>

#### Transaction Controller

<table>
<tr><td>EndPoint</td><td>RequestType</td><td>RequestBody</td></tr>
<tr><td>/transaction/history</td><td>Get</td><td>{"iban": ""}</td></tr>
<tr><td>/transaction/transfer</td><td>Post</td><td>{"senderIban": "", "receiverIban":"", "amount":#}</td></tr>
</table>

#### Run Tests -> mvn clean install
### Run App -> mvn spring-boot:run

#### Enhancements

* Add more tests for Controllers
* Think of Other corner cases
* Dockerize the application and run tests via test containers


