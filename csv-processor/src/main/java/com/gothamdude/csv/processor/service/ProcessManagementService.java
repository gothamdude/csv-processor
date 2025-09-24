package com.gothamdude.csv.processor.service;

import com.gothamdude.csv.processor.dao.ProcessLogDao;
import com.gothamdude.csv.processor.dao.ProcessMasterDao;
import com.gothamdude.csv.processor.exception.DataProcessingException;
import com.gothamdude.csv.processor.model.ProcessLog;
import com.gothamdude.csv.processor.model.ProcessMaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class ProcessManagementService {

    @Autowired
    private ProcessMasterDao processMasterDao;

    @Autowired
    private ProcessLogDao processLogDao;

    /**
     * Initialize a process by creating a process log entry with IP status
     */
    public ProcessLog initializeProcess(String processName) {
        log.info("Initializing process: {}", processName);

        // Find process master
        Optional<ProcessMaster> processMasterOpt = processMasterDao.findByProcessNameAndActive(processName);
        if (!processMasterOpt.isPresent()) {
            log.error("Process not found or inactive: {}", processName);
            throw new DataProcessingException("Process not found or inactive: " + processName);
        }

        ProcessMaster processMaster = processMasterOpt.get();
        log.info("Found process master: processId={}, description={}",
                processMaster.getProcessId(), processMaster.getProcessDescription());

        // Create process log entry with IP (In Progress) status
        ProcessLog processLog = new ProcessLog(processMaster.getProcessId(), "IP");
        processLog = processLogDao.save(processLog);

        log.info("Process initialized with run ID: {} for processId: {}",
                processLog.getRunId(), processMaster.getProcessId());

        return processLog;
    }

    /**
     * Finalize a process by updating the process log with final status
     */
    public void finalizeProcess(ProcessLog processLog, String finalStatus, String errorMessage) {
        log.info("Finalizing process run ID: {} with status: {}", processLog.getRunId(), finalStatus);

        processLog.updateStatus(finalStatus);
        processLogDao.save(processLog);

        if ("FA".equals(finalStatus)) {
            if (errorMessage != null) {
                log.error("Process failed with error: {}", errorMessage);
            } else {
                log.error("Process failed with unknown error");
            }
        } else if ("SU".equals(finalStatus)) {
            log.info("Process completed successfully");
        }
    }

    /**
     * Get process master by name
     */
    public Optional<ProcessMaster> getProcessMaster(String processName) {
        log.info("Retrieving process master for: {}", processName);
        return processMasterDao.findByProcessNameAndActive(processName);
    }

    /**
     * Get process execution history
     */
    public List<ProcessLog> getProcessHistory(String processName) {
        log.info("Retrieving process history for: {}", processName);

        Optional<ProcessMaster> processMasterOpt = processMasterDao.findByProcessName(processName);
        if (!processMasterOpt.isPresent()) {
            log.error("Process not found: {}", processName);
            throw new DataProcessingException("Process not found: " + processName);
        }

        ProcessMaster processMaster = processMasterOpt.get();
        List<ProcessLog> history = processLogDao.findByProcessIdOrderByCreatedTsDesc(processMaster.getProcessId());

        log.info("Retrieved {} process log entries for process: {}", history.size(), processName);

        return history;
    }

    /**
     * Check if process is currently running
     */
    public boolean isProcessRunning(String processName) {
        log.info("Checking if process is running: {}", processName);

        List<ProcessLog> history = getProcessHistory(processName);
        boolean isRunning = !history.isEmpty() && "IP".equals(history.get(0).getProcessStatus());

        log.info("Process {} is running: {}", processName, isRunning);

        return isRunning;
    }

    /**
     * Get last execution status
     */
    public String getLastExecutionStatus(String processName) {
        log.info("Getting last execution status for process: {}", processName);

        List<ProcessLog> history = getProcessHistory(processName);
        String status = history.isEmpty() ? "NEVER_RUN" : history.get(0).getProcessStatus();

        log.info("Last execution status for process {}: {}", processName, status);

        return status;
    }
}
