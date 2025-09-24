package com.gothamdude.csv.processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessLog {
    private Integer runId;
    private Integer processId;
    private LocalDate businessDate;
    private String processStatus;
    private LocalDateTime createdTs;
    private String createdBy;
    private LocalDateTime updatedTs;
    private String updatedBy;

    public ProcessLog(Integer processId, String processStatus) {
        this.processId = processId;
        this.businessDate = LocalDate.now();
        this.processStatus = processStatus;
        this.createdTs = LocalDateTime.now();
        this.createdBy = "admin_oltpdb";
        this.updatedTs = LocalDateTime.now();
        this.updatedBy = "admin_oltpdb";
    }

    public void updateStatus(String newStatus) {
        this.processStatus = newStatus;
        this.updatedTs = LocalDateTime.now();
    }
}