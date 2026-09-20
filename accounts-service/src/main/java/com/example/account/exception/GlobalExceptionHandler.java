package com.example.account.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	private static final String TIMESTAMP = "timestamp";

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
		log.warn("Malformed JSON request payload or parse error: {}", ex.getMostSpecificCause().getMessage());

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"Invalid JSON payload format or unparseable field values (e.g. invalid date or number format)."
		);
		problemDetail.setTitle("Malformed JSON Request");
		problemDetail.setType(URI.create("https://api.example.com/errors/bad-request"));
		problemDetail.setProperty(TIMESTAMP, Instant.now());
		return problemDetail;
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
		log.warn("Unmapped endpoint requested: [{}] {}", ex.getHttpMethod(), ex.getResourcePath());

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND,
				"The requested endpoint URL '" + ex.getResourcePath() + "' does not exist."
		);
		problemDetail.setTitle("Endpoint Not Found");
		problemDetail.setType(URI.create("https://api.example.com/errors/not-found"));
		problemDetail.setProperty(TIMESTAMP, Instant.now());
		return problemDetail;
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
		log.warn("Access denied error: {}", ex.getMessage());

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.FORBIDDEN,
				"You do not have permission to access this resource."
		);
		problemDetail.setTitle("Access Denied");
		problemDetail.setType(URI.create("https://api.example.com/errors/forbidden"));
		problemDetail.setProperty(TIMESTAMP, Instant.now());
		return problemDetail;
	}

	@ExceptionHandler({UserAlreadyExistsException.class, UserNotFoundException.class})
	public ProblemDetail handleDomainExceptions(RuntimeException ex) {
		HttpStatus status = switch (ex) {
			case UserAlreadyExistsException _ -> HttpStatus.CONFLICT;
			case UserNotFoundException _      -> HttpStatus.NOT_FOUND;
			default                            -> HttpStatus.INTERNAL_SERVER_ERROR;
		};

		if (status.is5xxServerError()) {
			log.error("Unhandled domain exception occurred", ex);
		} else {
			log.warn("Domain rule violation [{}]: {}", status.value(), ex.getMessage());
		}

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
		problemDetail.setTitle("Domain Rule Violation");
		problemDetail.setType(URI.create("https://api.example.com/errors/" + status.value()));
		problemDetail.setProperty(TIMESTAMP, Instant.now());
		return problemDetail;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
		var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.toList();

		log.warn("Request payload validation failed for object '{}': {}",
				ex.getBindingResult().getObjectName(), fieldErrors);

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"Validation failed for request body"
		);
		problemDetail.setTitle("Invalid Request Payload");
		problemDetail.setProperty("errors", fieldErrors);
		problemDetail.setProperty(TIMESTAMP, Instant.now());
		return problemDetail;
	}
}