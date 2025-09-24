package com.gothamdude.csv.processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessMaster {
    private Integer processId;
    private String processName;
    private String processDescription;
    private String businessDateType;
    private String fileType;
    private String fileName;
    private String filePath;
    private LocalDateTime createdTs;
    private String createdBy;
    private LocalDateTime updatedTs;
    private String updatedBy;
    private Boolean isActive;

    public ProcessMaster(String processName, String processDescription,
                         String businessDateType, String fileType, String fileName, String filePath) {
        this.processName = processName;
        this.processDescription = processDescription;
        this.businessDateType = businessDateType;
        this.fileType = fileType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.createdTs = LocalDateTime.now();
        this.createdBy = "admin_oltpdb";
        this.updatedTs = LocalDateTime.now();
        this.updatedBy = "admin_oltpdb";
        this.isActive = true;
    }


}