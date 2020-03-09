/*
 * Copyright (c) 2020, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.bidms.app.sgs.executor;

import edu.berkeley.bidms.app.sgs.config.SgsConfiguration;
import edu.berkeley.bidms.app.sgs.config.properties.SgsConfigProperties;
import edu.berkeley.bidms.app.sgs.service.RegistrySqlTextService;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Provides common hashing operations for implementations of {@link
 * SorHashExecutor}.
 */
@Service
public class HashExecutorService extends ExecutorService implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private SgsConfiguration sgsConfiguration;
    private JdbcTemplate registryJdbcTemplate;
    private RegistrySqlTextService registrySqlTextService;

    public HashExecutorService(SgsConfiguration sgsConfiguration, SgsConfigProperties sgsConfigProps, JdbcTemplate registryJdbcTemplate, RegistrySqlTextService registrySqlTextService) {
        super(sgsConfigProps);
        this.sgsConfiguration = sgsConfiguration;
        this.registryJdbcTemplate = registryJdbcTemplate;
        this.registrySqlTextService = registrySqlTextService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Queries the registry for the last time a SOR was queried by getting
     * the {@code max(timeMarker)} time from the {@code SORObjectChecksum}
     * table for a particular SOR.
     *
     * @param sorId sorId of the SOR.
     * @return The {@code max(timeMarker)} value from the {@code
     * SORObjectChecksum} table for the SOR.
     */
    public Timestamp getLastTimeMarker(int sorId) {
        return registryJdbcTemplate.queryForObject(
                registrySqlTextService.lastTimeMarkerForSorSql("SELECT max(timeMarker) FROM SorObjectChecksum WHERE sorId=?"),
                Timestamp.class,
                sorId
        );
    }

    private Driver getRegistryJdbcDriver() throws SQLException {
        return DriverManager.getDriver(sgsConfiguration.getJdbcUrl());
    }

    private boolean isRegistryPostgreSQLDriver() throws SQLException {
        return org.postgresql.Driver.class.isAssignableFrom(getRegistryJdbcDriver().getClass());
    }

    private BaseConnection getRegistryPostgreSQLBaseConnection() throws SQLException, HashExecutorException {
        Connection conn = DriverManager.getConnection(sgsConfiguration.getJdbcUrl(), sgsConfiguration.getJdbcConnectionProperties());
        if (!(conn instanceof BaseConnection)) {
            conn.close();
            throw new HashExecutorException("Connection is not an instance of PostgreSQL's BaseConnection class");
        }
        return (BaseConnection) conn;
    }

    /**
     * Copy a CSV file to the {@code SORObjectChecksum} table.  This takes
     * advantage of PostgreSQL's {@code COPY} feature.
     *
     * @param sorId   sorId of the SOR.
     * @param csvFile {@link File} object for the CSV file containing the
     *                hash values.
     * @return Number of entries from the file successfully written to the
     * table.
     * @throws IOException           Error reading the CSV file.
     * @throws SQLException          Error writing to the {@code
     *                               SORObjectChecksum} table.
     * @throws HashExecutorException If an error occurred.
     */
    public long copyFileToChecksumTable(int sorId, File csvFile) throws IOException, SQLException, HashExecutorException {
        // PostgreSQL supports COPY (copying a file to a table)
        boolean isPostgreSQL = isRegistryPostgreSQLDriver();
        try (Connection conn = isPostgreSQL ? getRegistryPostgreSQLBaseConnection() : registryJdbcTemplate.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            if (isPostgreSQL) {
                try (PreparedStatement ps = conn.prepareStatement("set statement_timeout to 0")) {
                    ps.execute();
                }
            }
            try (FileReader reader = new FileReader(csvFile)) {
                try {
                    try (PreparedStatement ps = conn.prepareStatement(registrySqlTextService.deleteSorObjectChecksumForSorSql("DELETE FROM SorObjectChecksumQuery WHERE sorId=?"))) {
                        ps.setInt(1, sorId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(registrySqlTextService.deleteAllSorObjectChecksumsForSorSql("DELETE FROM SorObjectChecksum WHERE sorId=?"))) {
                        ps.setInt(1, sorId);
                        ps.executeUpdate();
                    }

                    long count = 0;
                    if (isPostgreSQL) {
                        CopyManager cm = new CopyManager((BaseConnection) conn);
                        count = cm.copyIn(registrySqlTextService.copySorObjectChecksumsForSorSql("COPY SORObjectChecksum(sorId,sorObjKey,hash,hashVersion,timeMarker,numericMarker) FROM STDIN (FORMAT CSV, DELIMITER '|')"), reader);
                        conn.commit();
                    } else {
                        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                // sorId|sorObjKey|hash|hashVersion|timeMarker|numericMarker
                                String[] fields = line.split("\\|");
                                try (PreparedStatement ps = conn.prepareStatement(registrySqlTextService.insertSorObjectChecksumForSorSql("INSERT INTO SORObjectChecksum(sorId,sorObjKey,hash,hashVersion,timeMarker,numericMarker) VALUES(?,?,?,?,?,?)"))) {
                                    ps.setInt(1, Integer.parseInt(fields[0])); // sorId
                                    ps.setString(2, fields[1]); // sorObjKey
                                    ps.setLong(3, Long.parseLong(fields[2])); // hash
                                    ps.setInt(4, Integer.parseInt(fields[3])); // hashVersion
                                    ps.setString(5, fields[4]); // timeMarker
                                    ps.setLong(6, Long.parseLong(fields[5])); // numericMarker
                                    ps.executeUpdate();
                                    count++;
                                }
                            }
                        }
                    }

                    return count;
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }
        }
    }

    /**
     * Insert or update a row in the {@code SORObjectChecksum} table.
     *
     * @param sorId         sorId of the SOR.
     * @param sorObjKey     Primary key of the SOR object.
     * @param hash          The hash value.
     * @param timeMarker    The timeMarker value (last modified time of the
     *                      SOR object, if supported).
     * @param numericMarker The numeric marker.  Often the epoch of the
     *                      timeMarker.
     */
    public void updateSORObjectChecksum(int sorId, String sorObjKey, long hash, Timestamp timeMarker, long numericMarker) {
        // check to see if SORObjectChecksum row already exists
        boolean alreadyExists = registryJdbcTemplate.queryForList(registrySqlTextService.checkSorObjectChecksumExistenceSql("SELECT sorObjKey FROM SORObjectChecksum WHERE sorId=? AND sorObjKey=?"), String.class, sorId, sorObjKey).size() > 0;
        if (!alreadyExists) {
            registryJdbcTemplate.update(registrySqlTextService.insertSorObjectChecksumSql("INSERT INTO SORObjectChecksum (sorId, sorObjKey, hash, hashVersion, timeMarker, numericMarker) VALUES(?,?,?,1,?,?)"), sorId, sorObjKey, hash, timeMarker, numericMarker);
        } else {
            registryJdbcTemplate.update(registrySqlTextService.updateSorObjectChecksumSql("UPDATE SORObjectChecksum SET hash=?, timeMarker=?, numericMarker=? WHERE sorId=? AND sorObjKey=?"), hash, timeMarker, numericMarker, sorId, sorObjKey);
        }
    }
}
