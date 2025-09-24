package com.gothamdude.csv.processor.dao;

import com.gothamdude.csv.processor.model.ProcessMaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class ProcessMasterDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<ProcessMaster> rowMapper = new ProcessMasterRowMapper();

    public Optional<ProcessMaster> findByProcessNameAndActive(String processName) {
        String sql = "SELECT * FROM PROCESS_MASTER WHERE PROCESS_NAME = ? AND IS_ACTIVE = true";

        log.debug("Executing query: {} with processName: {}", sql, processName);

        List<ProcessMaster> results = jdbcTemplate.query(sql, rowMapper, processName);

        log.debug("Query returned {} results", results.size());

        if (results.isEmpty()) {
            log.debug("No active process found with name: {}", processName);
            return Optional.empty();
        }

        ProcessMaster result = results.get(0);
        log.debug("Found active process: processId={}, processName={}", result.getProcessId(), result.getProcessName());

        return Optional.of(result);
    }

    public Optional<ProcessMaster> findByProcessName(String processName) {
        String sql = "SELECT * FROM PROCESS_MASTER WHERE PROCESS_NAME = ?";

        log.debug("Executing query: {} with processName: {}", sql, processName);

        List<ProcessMaster> results = jdbcTemplate.query(sql, rowMapper, processName);

        log.debug("Query returned {} results for process: {}", results.size(), processName);

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<ProcessMaster> findAll() {
        String sql = "SELECT * FROM PROCESS_MASTER ORDER BY PROCESS_NAME";

        log.debug("Executing query to find all processes: {}", sql);

        List<ProcessMaster> results = jdbcTemplate.query(sql, rowMapper);

        log.debug("Found {} total processes", results.size());

        return results;
    }

    public ProcessMaster save(ProcessMaster processMaster) {
        if (processMaster.getProcessId() == null) {
            log.debug("Inserting new process master: {}", processMaster.getProcessName());
            return insert(processMaster);
        } else {
            log.debug("Updating existing process master: processId={}", processMaster.getProcessId());
            return update(processMaster);
        }
    }

    private ProcessMaster insert(ProcessMaster pm) {
        String sql = """
            INSERT INTO PROCESS_MASTER (PROCESS_NAME, PROCESS_DESCRIPTION, BUSINESS_DATE_TYPE, 
                                       FILE_TYPE, FILE_NAME, FILE_PATH, CREATED_TS, CREATED_BY, 
                                       UPDATED_TS, UPDATED_BY, IS_ACTIVE) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        log.debug("Inserting process master with SQL: {}", sql);
        log.debug("Parameters: processName={}, fileType={}, fileName={}",
                pm.getProcessName(), pm.getFileType(), pm.getFileName());

        int rowsAffected = jdbcTemplate.update(sql,
                pm.getProcessName(), pm.getProcessDescription(), pm.getBusinessDateType(),
                pm.getFileType(), pm.getFileName(), pm.getFilePath(),
                pm.getCreatedTs(), pm.getCreatedBy(), pm.getUpdatedTs(), pm.getUpdatedBy(),
                pm.getIsActive());

        log.debug("Insert affected {} rows", rowsAffected);

        // Get the generated ID
        Integer id = jdbcTemplate.queryForObject("SELECT LASTVAL()", Integer.class);
        pm.setProcessId(id);

        log.debug("Generated process ID: {}", id);

        return pm;
    }

    private ProcessMaster update(ProcessMaster pm) {
        String sql = """
            UPDATE PROCESS_MASTER SET 
                PROCESS_DESCRIPTION = ?, BUSINESS_DATE_TYPE = ?, FILE_TYPE = ?, 
                FILE_NAME = ?, FILE_PATH = ?, UPDATED_TS = ?, UPDATED_BY = ?, IS_ACTIVE = ?
            WHERE PROCESS_ID = ?
            """;

        log.debug("Updating process master with SQL: {}", sql);
        log.debug("Parameters: processId={}, isActive={}", pm.getProcessId(), pm.getIsActive());

        int rowsAffected = jdbcTemplate.update(sql,
                pm.getProcessDescription(), pm.getBusinessDateType(), pm.getFileType(),
                pm.getFileName(), pm.getFilePath(), pm.getUpdatedTs(), pm.getUpdatedBy(),
                pm.getIsActive(), pm.getProcessId());

        log.debug("Update affected {} rows", rowsAffected);

        return pm;
    }

    private static class ProcessMasterRowMapper implements RowMapper<ProcessMaster> {
        @Override
        public ProcessMaster mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ProcessMaster.builder()
                    .processId(rs.getInt("PROCESS_ID"))
                    .processName(rs.getString("PROCESS_NAME"))
                    .processDescription(rs.getString("PROCESS_DESCRIPTION"))
                    .businessDateType(rs.getString("BUSINESS_DATE_TYPE"))
                    .fileType(rs.getString("FILE_TYPE"))
                    .fileName(rs.getString("FILE_NAME"))
                    .filePath(rs.getString("FILE_PATH"))
                    .createdTs(rs.getTimestamp("CREATED_TS").toLocalDateTime())
                    .createdBy(rs.getString("CREATED_BY"))
                    .updatedTs(rs.getTimestamp("UPDATED_TS").toLocalDateTime())
                    .updatedBy(rs.getString("UPDATED_BY"))
                    .isActive(rs.getBoolean("IS_ACTIVE"))
                    .build();
        }
    }
}
