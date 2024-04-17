package com.bhatman.astra;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;

public class AppUtil {
	private static Logger LOGGER = LoggerFactory.getLogger(AppUtil.class);

	public static final String KEYSPACE_NAME = "test_ks";
	public static final String LATENCY_TABLE = "LATENCY_CHECK";

	public static CqlSession getCQLSession(String scbPath, String clientId, String clientSecret) {
		CqlSession cqlSession = CqlSession.builder().withCloudSecureConnectBundle(Paths.get(scbPath))
				.withAuthCredentials(clientId, clientSecret).withKeyspace(KEYSPACE_NAME).build();

		return cqlSession;
	}

	public static void closeSession(CqlSession session, String dcName) {
		if (session != null) {
			session.close();
		}
		LOGGER.info("{}: Closed connection!", dcName);
	}

	public static String getDCName(CqlSession session) {
		PreparedStatement findDC = session.prepare(QueryBuilder.selectFrom("SYSTEM", "LOCAL").all().build());
		ResultSet rs = session.execute(findDC.bind());
		Row record = rs.one();

		return (null != record) ? record.getString("data_center") : "";
	}

	public static void createLatencyTableIfNotExists(CqlSession session, String dcName) {
		session.execute(SchemaBuilder.createTable(LATENCY_TABLE).ifNotExists().withPartitionKey("id", DataTypes.INT)
				.withClusteringColumn("key", DataTypes.INT).withColumn("value", DataTypes.TEXT)
				.withColumn("description", DataTypes.TEXT).build());
		session.execute(QueryBuilder.truncate(LATENCY_TABLE).build());
		LOGGER.info("{}: Table '{}' has been created (if not exists) OR truncated (if exists).", dcName, LATENCY_TABLE);
	}

}
