# Holon platform JDBC Datastore

> Latest release: [5.1.3](#obtain-the-artifacts)

This is the reference __JDBC__ implementation of the [Holon Platform](https://holon-platform.com) `Datastore` API, using the Java `JDBC` API and the `SQL` language for data access and manipulation.

See the [Datastore API documentation](https://docs.holon-platform.com/current/reference/holon-core.html#Datastore) for information about the Holon Platform `Datastore` API.

The JDBC Datastore implementation relies on the following conventions regarding __DataTarget__ and __Path__ naming strategy:

* The [DataTarget](https://docs.holon-platform.com/current/reference/holon-core.html#DataTarget) _name_ is interpreted as the database _table_ (or _view_) name.
* The [Path](https://docs.holon-platform.com/current/reference/holon-core.html#Path) _name_ is interpreted as a table _column_ name.

As a _relational Datastore_, standard [relational expressions](https://docs.holon-platform.com/current/reference/holon-datastore-jdbc.html#Relational-expressions) are supported (alias, joins and sub-queries).

__Transactions__ support is ensured through the Holon Platform `Transactional` API.

The JDBC Datastore leverages on __dialects__ to transparently support different RDBMS vendors. Dialects for the following RDBMS are provided:

* MySQL
* MariaDB
* Oracle Database
* Microsoft SQL Server
* PostgreSQL
* IBM DB2
* IBM Informix
* SAP HANA
* H2
* HSQLDB
* Derby
* SQLite

A complete __Spring__ and __Spring Boot__ support is provided for JDBC Datastore integration in a Spring environment and for __auto-configuration__ facilities.

See the module [documentation](https://docs.holon-platform.com/current/reference/holon-datastore-jdbc.html) for details.

Just like any other platform module, this artifact is part of the [Holon Platform](https://holon-platform.com) ecosystem, but can be also used as a _stand-alone_ library.

See [Getting started](#getting-started) and the [platform documentation](https://docs.holon-platform.com/current/reference) for further details.

## At-a-glance overview

_JDBC Datastore operations:_
```java
Datastore datastore = JdbcDatastore.builder().dataSource(myDataSource).build();

datastore.save(TARGET, PropertyBox.builder(TEST).set(ID, 1L).set(VALUE, "One").build());

Stream<PropertyBox> results = datastore.query().target(TARGET).filter(ID.goe(1L)).stream(TEST);

List<String> values = datastore.query().target(TARGET).sort(ID.asc()).list(VALUE);

Stream<String> values = datastore.query().target(TARGET).filter(VALUE.startsWith("prefix")).restrict(10, 0).stream(VALUE);

long count = datastore.query(TARGET).aggregate(QueryAggregation.builder().path(VALUE).filter(ID.gt(1L)).build()).count();

Stream<Integer> months = datastore.query().target(TARGET).distinct().stream(LOCAL_DATE.month());

datastore.bulkUpdate(TARGET).filter(ID.in(1L, 2L)).set(VALUE, "test").execute();

datastore.bulkDelete(TARGET).filter(ID.gt(0L)).execute();
```

_Transaction management:_
```java
long updatedCount = datastore.withTransaction(tx -> {
	long updated = datastore.bulkUpdate(TARGET).set(VALUE, "test").execute().getAffectedCount();
			
	tx.commit();
			
	return updated;
});
```

_JDBC Datastore extension:_
```java
// Function definition
class IfNull<T> implements QueryFunction<T, T> {
  /* content omitted */		
}

// Function resolver
class IfNullResolver implements ExpressionResolver<IfNull, SQLFunction> {

  @Override
  public Optional<SQLFunction> resolve(IfNull expression, ResolutionContext context) throws InvalidExpressionException {
  return Optional.of(SQLFunction.create(args ->  "IFNULL(" + args.get(0) + "," + args.get(1) + ")"));
  }
	
}

// Datastore integration
Datastore datastore = JdbcDatastore.builder().withExpressionResolver(new IfNullResolver()).build();

Stream<String> values = datastore.query(TARGET).stream(new IfNull<>(VALUE, "(fallback)"));
```

See the [module documentation](https://docs.holon-platform.com/current/reference/holon-datastore-jdbc.html) for the user guide and a full set of examples.

## Code structure

See [Holon Platform code structure and conventions](https://github.com/holon-platform/platform/blob/master/CODING.md) to learn about the _"real Java API"_ philosophy with which the project codebase is developed and organized.

## Getting started

### System requirements

The Holon Platform is built using __Java 8__, so you need a JRE/JDK version 8 or above to use the platform artifacts.

A JDBC driver which supports the __JDBC API version 4.x__ or above is reccomended to use all the functionalities of the JDBC Datastore.

### Releases

See [releases](https://github.com/holon-platform/holon-datastore-jdbc/releases) for the available releases. Each release tag provides a link to the closed issues.

#### 5.1.x release notes

See [What's new in version 5.1.x](https://docs.holon-platform.com/current/reference/holon-datastore-jdbc.html#WhatsNew51x) to learn about the new features and API operations of the 5.1 minor release.

### Obtain the artifacts

The [Holon Platform](https://holon-platform.com) is open source and licensed under the [Apache 2.0 license](LICENSE.md). All the artifacts (including binaries, sources and javadocs) are available from the [Maven Central](https://mvnrepository.com/repos/central) repository.

The Maven __group id__ for this module is `com.holon-platform.jdbc` and a _BOM (Bill of Materials)_ is provided to obtain the module artifacts:

_Maven BOM:_
```xml
<dependencyManagement>
    <dependency>
        <groupId>com.holon-platform.jdbc</groupId>
        <artifactId>holon-datastore-jdbc-bom</artifactId>
        <version>5.1.3</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencyManagement>
```

See the [Artifacts list](#artifacts-list) for a list of the available artifacts of this module.

### Using the Platform BOM

The [Holon Platform](https://holon-platform.com) provides an overall Maven _BOM (Bill of Materials)_ to easily obtain all the available platform artifacts:

_Platform Maven BOM:_
```xml
<dependencyManagement>
    <dependency>
        <groupId>com.holon-platform</groupId>
        <artifactId>bom</artifactId>
        <version>${platform-version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencyManagement>
```

See the [Artifacts list](#artifacts-list) for a list of the available artifacts of this module.

### Build from sources

You can build the sources using Maven (version 3.3.x or above is recommended) like this: 

`mvn clean install`

> __NOTE:__ The `holon-datastore-jdbc-composer` artifact requires the Oracle JDBC driver as *optional* dependency to compile the Oracle SQLDialect class. Since the Oracle JDBC driver is not available from Maven Central, to compile the project you should manually download and install it in your local Maven repository or follow the [Oracle Maven repository setup instructions here](https://blogs.oracle.com/dev2dev/get-oracle-jdbc-drivers-and-ucp-from-oracle-maven-repository-without-ides).

## Getting help

* Check the [platform documentation](https://docs.holon-platform.com/current/reference) or the specific [module documentation](https://docs.holon-platform.com/current/reference/holon-datastore-jdbc.html).

* Ask a question on [Stack Overflow](http://stackoverflow.com). We monitor the [`holon-platform`](http://stackoverflow.com/tags/holon-platform) tag.

* Report an [issue](https://github.com/holon-platform/holon-datastore-jdbc/issues).

* A [commercial support](https://holon-platform.com/services) is available too.

## Examples

See the [Holon Platform examples](https://github.com/holon-platform/holon-examples) repository for a set of example projects.

## Contribute

See [Contributing to the Holon Platform](https://github.com/holon-platform/platform/blob/master/CONTRIBUTING.md).

[![Gitter chat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/holon-platform/contribute?utm_source=share-link&utm_medium=link&utm_campaign=share-link) 
Join the __contribute__ Gitter room for any question and to contact us.

## License

All the [Holon Platform](https://holon-platform.com) modules are _Open Source_ software released under the [Apache 2.0 license](LICENSE).

## Artifacts list

Maven _group id_: `com.holon-platform.jdbc`

Artifact id | Description
----------- | -----------
`holon-datastore-jdbc` | __JDBC__ `Datastore` API implementation
`holon-datastore-jdbc-composer` | The SQL composer engine based on the Java __JDBC__ API
`holon-datastore-jdbc-spring` | __Spring__ integration using the `@EnableJdbcDatastore` annotation
`holon-datastore-jdbc-spring-boot` | __Spring Boot__ integration for JDBC Datastore auto-configuration
`holon-starter-jdbc-datastore` | __Spring Boot__ _starter_ for the JDBC Datastore auto-configuration
`holon-starter-jdbc-datastore-hikaricp` | __Spring Boot__ _starter_ for the JDBC Datastore auto-configuration using the [HikariCP](https://github.com/brettwooldridge/HikariCP) _pooling_ DataSource  
`holon-datastore-jdbc-bom` | Bill Of Materials
`documentation-datastore-jdbc` | Documentation
