package com.example.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NoResourceFoundException.class)
	public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND,
				"The requested endpoint URL '" + ex.getResourcePath() + "' does not exist."
		);
		problemDetail.setTitle("Endpoint Not Found");
		problemDetail.setType(URI.create("https://api.example.com/errors/not-found"));
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.FORBIDDEN,
				"You do not have permission to access this resource."
		);
		problemDetail.setTitle("Access Denied");
		problemDetail.setType(URI.create("https://api.example.com/errors/forbidden"));
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}


	@ExceptionHandler({UserAlreadyExistsException.class, UserNotFoundException.class})
	public ProblemDetail handleDomainExceptions(RuntimeException ex) {
		HttpStatus status = switch (ex) {
			case UserAlreadyExistsException _ -> HttpStatus.CONFLICT;
			case UserNotFoundException _      -> HttpStatus.NOT_FOUND;
			default                            -> HttpStatus.INTERNAL_SERVER_ERROR;
		};

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
		problemDetail.setTitle("Domain Rule Violation");
		problemDetail.setType(URI.create("https://api.example.com/errors/" + status.value()));
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed for request body");
		problemDetail.setTitle("Invalid Request Payload");

		var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.toList();

		problemDetail.setProperty("errors", fieldErrors);
		problemDetail.setProperty("timestamp", Instant.now());
		return problemDetail;
	}
}