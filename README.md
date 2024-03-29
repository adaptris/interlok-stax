# interlok-stax

[![GitHub tag](https://img.shields.io/github/tag/adaptris/interlok-stax.svg)](https://github.com/adaptris/interlok-stax/tags)
[![license](https://img.shields.io/github/license/adaptris/interlok-stax.svg)](https://github.com/adaptris/interlok-stax/blob/develop/LICENSE)
[![Actions Status](https://github.com/adaptris/interlok-stax/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/adaptris/interlok-stax/actions)
[![codecov](https://codecov.io/gh/adaptris/interlok-stax/branch/develop/graph/badge.svg)](https://codecov.io/gh/adaptris/interlok-stax)
[![CodeQL](https://github.com/adaptris/interlok-stax/workflows/CodeQL/badge.svg)](https://github.com/adaptris/interlok-stax/security/code-scanning)
[![Known Vulnerabilities](https://snyk.io/test/github/adaptris/interlok-stax/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/adaptris/interlok-stax?targetFile=build.gradle)
[![Closed PRs](https://img.shields.io/github/issues-pr-closed/adaptris/interlok-stax)](https://github.com/adaptris/interlok-stax/pulls?q=is%3Apr+is%3Aclosed)

The suggested name was `animated-dollop`

# Provides XML support via STaX

Might provide better performance characteristics when working with large XML files where you don't need full XPath support via xpath-message-splitter.

* stax-path-splitter -> splits an XML file using STaX events based on a configured path.
* stax-xml-start-document / stax-xml-write-element / stax-xml-end-document -> allows you to build up a document using STaX events in an iterative manner.
* com.adaptris.stax.* -> Used to write documents in a streaming fashion where required (e.g. CSV).

# Example Use Case

Pretty contrived, but you should get the idea. If you had a huge XML document that you wanted to split and map into a slightly different format (the mapping presumably doesn't need the whole XML in order to succeed) then you could :

```
 <stax-xml-start-document/>
 <advanced-message-splitter-service>
   <splitter class="stax-path-splitter">
     <path>/Root/Envelope</path>
     <encoding>UTF-8</encoding>
     <message-factory class="default-message-factory"/>
     <copy-object-metadata>true</copy-object-metadata>
   </splitter>
   <service class="service-list">
     <services>
       <!-- Map the split message into something else -->
       <xml-transform-service/>
       <stax-xml-write-element/>
     </services>
   </service>
 </advanced-message-splitter-service>
 <stax-xml-end-document/>
```

If you had a huge CSV file that needed to be converted into XML, you could do this...

```
 <stax-xml-start-document/>
 <advanced-message-splitter-service>
   <splitter class="line-count-splitter">
     <keep-header-lines>1</keep-header-lines>
     <split-on-line>1</split-on-line>
     <copy-object-metadata>true</copy-object-metadata>
     <message-factory class="default-message-factory"/>
   </splitter>
   <service class="service-list">
     <services>
       <simple-csv-to-xml-transform>
         <unique-record-names>false</unique-record-names>
         <element-names-from-first-record>true</element-names-from-first-record>
       </simple-csv-to-xml-transform>
       <stax-xml-write-element/>
     </services>
   </service>
 </advanced-message-splitter-service>
 <stax-xml-end-document/>
```
