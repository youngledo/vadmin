package io.github.youngledo.vadmin.springboot.error;

import io.github.youngledo.vadmin.contracts.error.BusinessFailure;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BusinessFailureAdvice {
    private final ProblemDetailMapper mapper;

    public BusinessFailureAdvice(ProblemDetailMapper mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(BusinessFailure.class)
    public ResponseEntity<ProblemDetail> handle(BusinessFailure failure) {
        var detail = mapper.map(failure);
        return ResponseEntity.status(detail.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(detail);
    }
}
