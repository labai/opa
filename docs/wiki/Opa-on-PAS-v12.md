## Run on PAS server v12 using http connection

Using OPA on Pacific AppServer (PAS) v12.0.

For development Pacific AppServer can be obtained from 
https://www.progress.com/oedk 
with Developer kit 


#### Connection string

For connection should be used "http" or "https".
Here we use http.

Connection example

      http://localhost:8810/apsv

More info in [article](https://knowledgebase.progress.com/articles/Article/How-to-connect-to-the-Pacific-AppServer-from-Open-Client-or-the-Sonic-ESB-Native-Adapter)  

#### Additional jars

The old good opa can be used for PAS 12.0, just:

1) Need to use `o4glrt.jar` from OE 12.0 install pack

2) Need to add another OE jar from install pack:
    - oeauth-12.0.0.jar
    
3) Need to add few Apache libs:
    - org.apache.httpcomponents : httpcore : 4.4.5
    - org.apache.httpcomponents : httpclient : 4.5.2
    - org.apache.httpcomponents : httpasyncclient : 4.1.2
    
    (they are provided in install pack, but it would be wise to take them independently as maven dependency)

