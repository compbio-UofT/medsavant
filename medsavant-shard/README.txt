MedSavant Shard

MedSavant Shard module contains most of the functionality related to sharding including the configuration for storing variants in multiple databases. The configuration can be found in medsavant-shard/src/main/resources/hibernate*.cfg.xml files and has the following form:

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-configuration PUBLIC "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
        "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
  <session-factory>
    <property name="hibernate.connection.driver_class">com.mysql.jdbc.Driver</property>
    <property name="hibernate.connection.url">jdbc:mysql://localhost:5029/fhsall5_shard00</property>
    <property name="hibernate.connection.username">root</property>
    <property name="connection.password">root</property>
    <property name="connection.pool_size">1</property>
    <property name="hibernate.dialect">org.hibernate.dialect.MySQLDialect</property>
    <property name="hibernate.connection.shard_id">0</property>
    <property name="hibernate.shard.enable_cross_shard_relationship_checks">true</property>
  </session-factory>
</hibernate-configuration>

Each hibernate*.cfg.xml file contains configuration for a single shard. In order to configure the module for more shards, simply add more hibernate*.cfg.xml to medsavant-shard/src/main/resources/ in sequence. These files will be autodetected when the server is started. Each shard has to be located in its own database and the databases for different shards may be on the same or multiple DB servers - just specify the connection properties in the configuration file and assign a unique shard_id to each shard.

The module contains a simple testsuite requiring working shards configuration. If you don't have the database ready when building the module, build it with the following command:

mvn clean install -DskipTests