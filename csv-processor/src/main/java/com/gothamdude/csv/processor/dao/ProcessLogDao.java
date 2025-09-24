package com.gothamdude.csv.processor.dao;

import com.gothamdude.csv.processor.model.ProcessLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class ProcessLogDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<ProcessLog> rowMapper = new ProcessLogRowMapper();

    public ProcessLog save(ProcessLog processLog) {
        if (processLog.getRunId() == null) {
            log.debug("Inserting new process log for processId: {}, status: {}",
                    processLog.getProcessId(), processLog.getProcessStatus());
            return insert(processLog);
        } else {
            log.debug("Updating existing process log: runId={}, status={}",
                    processLog.getRunId(), processLog.getProcessStatus());
            return update(processLog);
        }
    }

    private ProcessLog insert(ProcessLog pl) {
        String sql = """
            INSERT INTO PROCESS_LOG (PROCESS_ID, BUSINESS_DATE, PROCESS_STATUS, 
                                   CREATED_TS, CREATED_BY, UPDATED_TS, UPDATED_BY) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        log.debug("Inserting process log with SQL: {}", sql);
        log.debug("Parameters: processId={}, businessDate={}, status={}",
                pl.getProcessId(), pl.getBusinessDate(), pl.getProcessStatus());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, pl.getProcessId());
            ps.setDate(2, Date.valueOf(pl.getBusinessDate()));
            ps.setString(3, pl.getProcessStatus());
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(pl.getCreatedTs()));
            ps.setString(5, pl.getCreatedBy());
            ps.setTimestamp(6, java.sql.Timestamp.valueOf(pl.getUpdatedTs()));
            ps.setString(7, pl.getUpdatedBy());
            return ps;
        }, keyHolder);

        log.debug("Insert affected {} rows", rowsAffected);

        Integer runId = keyHolder.getKey().intValue();
        pl.setRunId(runId);

        log.debug("Generated run ID: {}", runId);

        return pl;
    }

    private ProcessLog update(ProcessLog pl) {
        String sql = """
            UPDATE PROCESS_LOG SET 
                PROCESS_STATUS = ?, UPDATED_TS = ?, UPDATED_BY = ?
            WHERE RUN_ID = ?
            """;

        log.debug("Updating process log with SQL: {}", sql);
        log.debug("Parameters: runId={}, newStatus={}", pl.getRunId(), pl.getProcessStatus());

        int rowsAffected = jdbcTemplate.update(sql,
                pl.getProcessStatus(), java.sql.Timestamp.valueOf(pl.getUpdatedTs()),
                pl.getUpdatedBy(), pl.getRunId());

        log.debug("Update affected {} rows", rowsAffected);

        return pl;
    }

    public List<ProcessLog> findByProcessIdOrderByCreatedTsDesc(Integer processId) {
        String sql = "SELECT * FROM PROCESS_LOG WHERE PROCESS_ID = ? ORDER BY CREATED_TS DESC";

        log.debug("Finding process logs by processId: {} with SQL: {}", processId, sql);

        List<ProcessLog> results = jdbcTemplate.query(sql, rowMapper, processId);

        log.debug("Found {} process log entries for processId: {}", results.size(), processId);

        return results;
    }

    public List<ProcessLog> findByProcessIdAndBusinessDate(Integer processId, LocalDate businessDate) {
        String sql = "SELECT * FROM PROCESS_LOG WHERE PROCESS_ID = ? AND BUSINESS_DATE = ?";

        log.debug("Finding process logs by processId: {} and businessDate: {} with SQL: {}",
                processId, businessDate, sql);

        List<ProcessLog> results = jdbcTemplate.query(sql, rowMapper, processId, Date.valueOf(businessDate));

        log.debug("Found {} process log entries for processId: {} on date: {}",
                results.size(), processId, businessDate);

        return results;
    }

    public Optional<ProcessLog> findByRunId(Integer runId) {
        String sql = "SELECT * FROM PROCESS_LOG WHERE RUN_ID = ?";

        log.debug("Finding process log by runId: {} with SQL: {}", runId, sql);

        List<ProcessLog> results = jdbcTemplate.query(sql, rowMapper, runId);

        if (results.isEmpty()) {
            log.debug("No process log found with runId: {}", runId);
            return Optional.empty();
        }

        ProcessLog result = results.get(0);
        log.debug("Found process log: runId={}, processId={}, status={}",
                result.getRunId(), result.getProcessId(), result.getProcessStatus());

        return Optional.of(result);
    }

    private static class ProcessLogRowMapper implements RowMapper<ProcessLog> {
        @Override
        public ProcessLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ProcessLog.builder()
                    .runId(rs.getInt("RUN_ID"))
                    .processId(rs.getInt("PROCESS_ID"))
                    .businessDate(rs.getDate("BUSINESS_DATE").toLocalDate())
                    .processStatus(rs.getString("PROCESS_STATUS"))
                    .createdTs(rs.getTimestamp("CREATED_TS").toLocalDateTime())
                    .createdBy(rs.getString("CREATED_BY"))
                    .updatedTs(rs.getTimestamp("UPDATED_TS").toLocalDateTime())
                    .updatedBy(rs.getString("UPDATED_BY"))
                    .build();
        }
    }
}