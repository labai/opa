# OPA - Openedge Procedure Adapter

## About

OPA is adapter for OpenEdge ABL (Progress 4GL) procedure calling from java.

### Problem to solve
To call procedures from Java, Progress offers to use ProxyGen. It requires few steps:  
1) To have prepared ProxyGen project  
2) Compile .p code to .r code  
3) Move these .r code to some place where ProxyGen project will see them  
4) Generate java class with ugly interfaces  
5) Move these classes to your Java project  

If procedure parameters (input/output parameters, temp-table) have been changed, then you need to do all steps again.

### Solution

OPA offers flexible and easy call of OpenEdge procedures with runtime parameter mapping.

OPA uses dedicated class with Java annotations to describe parameters and temp-table fields (Java list of entities). When you need to call OpenEdge ABL procedure, you create instance of that class, assigns values to input parameters, fill List (temp-table) and call OpenEdge procedure. 

OPA automatically (on the fly) maps input parameters from Java class to OpenEdge procedure, calls that procedure and fills output parameters from procedure result.

## Usage

### Dependency

It is required to add dependency in pom.xml, to use OPA:

    <dependency>
        <groupId>com.github.labai</groupId>
        <artifactId>opa</artifactId>
        <version>1.1.2</version>
    </dependency>

Also `o4glrt.jar` from OpenEdge install dir must be accessible. I recommend to add it as artifact into you maven repository and use as dependency in pom.xml (e.g. `mvn install:install-file -Dfile=o4glrt.jar -DgroupId=com.progress.openedge -DartifactId=o4glrt -Dversion=10.1B03 -Dpackaging=jar`)

For using with OpenEdge PAS 12.0 see in 
[Opa-on-PAS-v12](Opa-on-PAS-v12)


### Samples

Sample 1: with simple parameters

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
             
            @OpaParam(io=IoDir.IN)
            public String bankCode;
             
            @OpaParam(io=IoDir.OUT)
            public String bankName;
             
            @OpaParam(io=IoDir.OUT)
            public Integer errorCode;
             
            @OpaParam(io= IoDir.OUT)
            public String errorMessage;
        }
    }


On OpenEdge side procedure parameters

    DEFINE INPUT  PARAMETER bankCode AS CHARACTER INITIAL ?.  
    DEFINE OUTPUT PARAMETER bankName AS CHARACTER INITIAL ?.  
    DEFINE OUTPUT PARAMETER err_code AS INTEGER   INITIAL ?.  
    DEFINE OUTPUT PARAMETER err      AS CHARACTER INITIAL ?.  

Sample 2: with output temp-table

    public class OpaDemo2 {
     
        public static void main(String[] args) throws OpaException {
            // in real app OpaServer should be as service (single per application)
            OpaServer server = new OpaServer("AppServer://progress.app.server/asprosv", "-", "-", SessionModel.STATE_FREE);
     
            GetCustomerAccountsOpp opp = new GetCustomerAccountsOpp();
            opp.customerId = "00000038";
            server.runProc(opp, "get_customer_accounts.p");
            for (Account ac: opp.accounts) {
                System.out.println("acc:" + ac.iban +"-"+ ac.currency);
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

More samples can be found in OPA project tests

## Technical details

#### Field type mapping

    Progress data | Default Java data      | Alternative Java    | Remarks
    type          | types (if datatype not | data types          |
                  | specified)             | (datatype required) |
    --------------+------------------------+---------------------+-------------------------------------
    DECIMAL       | BigDecimal             |                     |
    INTEGER       | Integer, int           |                     | for int null will be converted 0
    INT64         | Long, long             |                     | for long null will be converted 0
    CHARACTER     | String, enum           |                     | for enum "" and null will be converted to null,
                  |                        |                     |   but if not null - then value must be one of enum items (otherwise exception will be thrown)
    LOGICAL       | Boolean, boolean       |                     | for boolean null will be converted to false
    DATETIME      | Date                   |                     | Warn: Java date by default maps to Progress datetime (both are with time)
    DATE          |                        | Date                |
    DATETIMETZ    |                        | Date                |
    BLOB          |                        | byte[]              | in tt
    CLOB          |                        | String              | in tt
    LONGCHAR      |                        | String              | in params
    MEMPTR        |                        | byte[]              | in params
