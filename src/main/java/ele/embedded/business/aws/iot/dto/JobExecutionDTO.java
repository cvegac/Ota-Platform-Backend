package ele.embedded.business.aws.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.iot.model.JobExecutionSummaryForThing;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionDTO {
    private String jobId;
    private String status;
    private Instant queuedAt;
    private Instant startedAt;
    private Instant lastUpdatedAt;

    public JobExecutionDTO(JobExecutionSummaryForThing summaryForThing) {
        this.jobId = summaryForThing.jobId();
        var summary = summaryForThing.jobExecutionSummary();
        this.status = summary.status().toString();
        this.queuedAt = summary.queuedAt();
        this.startedAt = summary.startedAt();
        this.lastUpdatedAt = summary.lastUpdatedAt();
    }
}
