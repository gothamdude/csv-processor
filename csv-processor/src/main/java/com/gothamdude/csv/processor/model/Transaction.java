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
public class Transaction {
    private String transactionId;
    private String ticker;
    private Integer quantity;
    private String direction;
    private String employeeId;
    private LocalDate transactionDate;
    private LocalDateTime transactionTimestamp;
    private String transactionStatus;
}
