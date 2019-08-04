# OPA - Openedge Procedure Adapter

----
## ABOUT

OPA is adapter for OpenEdge ABL (Progress 4GL) procedure call from Java.

#### Problem to solve

To call procedures from Java, Progress offers to use ProxyGen. It requires few steps:

1. To have prepared ProxyGen project.
2. Compile .p code into .r code.
3. Move these .r code to some place where ProxyGen project will see them.
4. Generate ugly java classes.
5. Move these classes to your Java project.

If procedure parameters (input/output parameters, temp-table) is changed, then it require to repeat all steps again.

#### Solution

OPA offers flexible and easy way to call of OpenEdge procedures with runtime parameter mapping.

OPA uses dedicated class with Java annotations to describe parameters and temp-table fields (Java list of entities). 
When need to call OpenEdge ABL procedure, just create instance of that class, assign values to input parameters, fill List (temp-table) and call OpenEdge procedure.
OPA automatically (on the fly) maps input parameters from Java class to OpenEdge procedure, calls that procedure and fills output parameters with procedure result.

----
## USAGE


It is required to add dependency in pom.xml, to use OPA:
```xml
<dependency>
    <groupId>com.github.labai</groupId>
    <artifactId>opa</artifactId>
    <version>1.2.5</version>
</dependency>
```
Also `o4glrt.jar` from OpenEdge install dir must be accessible. 
It is good idea to add this jar as artifact into maven repository and use as dependency in pom.xml.

For using with OpenEdge PAS 12.0 see in 
[Opa-on-PAS-v12](docs/wiki/Opa-on-PAS-v12.md) in wiki.
  

---
## MORE INFO

- It is free.
- Is in production since 2014.
- Supports connection pool.
- Supports wide range of types, used in Java and Progress.
- Temp-table can be automatically mapped to List.

----
## SAMPLES

#### Sample 1: with simple parameters
```java
public class OpaDemo {
    public static void main(String[] args) throws OpaException {
        // in real app OpaServer should be as service (single per application)
        OpaServer server = new OpaServer("AppServer://progress.app.server/asprosv", "-", "-", SessionModel.STATE_FREE);
        BankCodeOpp opp = new BankCodeOpp();
        opp.bankCode = "112233";
        server.runProc(opp);
        System.out.println("name:" + opp.bankName);
    }

    @OpaProc(proc="get_bank_by_code.p")
    private static class BankCodeOpp {

        @OpaParam(io=IN)
        public String bankCode;

        @OpaParam(io=OUT)
        public String bankName;

        @OpaParam(io=OUT)
        public Integer errorCode;

        @OpaParam(io=OUT)
        public String errorMessage;
    }
}
```
OpenEdge procedure parameters:
```
define input  parameter bankCode     as character initial ?.  
define output parameter bankName     as character initial ?.  
define output parameter errorCode    as integer   initial ?.  
define output parameter errorMessage as character initial ?.  
```

#### Sample 2: with output temp-table
```java
public class OpaDemo2 {

    public static void main(String[] args) throws OpaException {
        // in real app OpaServer should be as service (single per application)
        OpaServer server = new OpaServer("AppServer://progress.app.server/asprosv", "-", "-", SessionModel.STATE_FREE);

        GetCustomerAccountsOpp opp = new GetCustomerAccountsOpp();
        opp.customerId = "123456";
        server.runProc(opp, "get_customer_accounts.p");
        for (Account ac: opp.accounts) {
            System.out.println("acc: " + ac.iban + "-" + ac.currency);
        }
    }

    // Openedge Procedure Parameters definition
    @OpaProc
    public static class GetCustomerAccountsOpp {

        @OpaParam
        public String customerId;

        @OpaParam(table=Account.class, io=IoDir.OUT)
        public List<Account> accounts;

        @OpaParam(io=IoDir.OUT)
        public String errorCode;

        @OpaParam(io=IoDir.OUT)
        public String errorMessage;
    }

    // entity (temp-table)
    @OpaTable
    public static class Account {
        @OpaField
        public String iban;
        @OpaField
        public String currency;
        @OpaField
        public BigDecimal amount;
    }
}
```

More samples can be found in OPA tests (https://github.com/labai/opa/tree/master/src/test/java/com/github/labai/opa)
