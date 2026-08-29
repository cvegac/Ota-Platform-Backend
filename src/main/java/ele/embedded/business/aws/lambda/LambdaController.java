package ele.embedded.business.aws.lambda;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("lambda")
public class LambdaController {

    @Autowired
    private LambdaService lambdaService;

    public record JobRequest(
            @NotBlank String name,
            @NotBlank String versionId,
            @NotNull Integer filesize,
            TargetType targetType
    ) { }

    @PostMapping("/job")
    public String createJob(@Valid @RequestBody JobRequest jobRequest) {
        return lambdaService.newJob(jobRequest);
    }
}
