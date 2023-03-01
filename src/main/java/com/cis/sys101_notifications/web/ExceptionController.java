package com.cis.sys101_notifications.web;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

	/*@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<String> customException(CustomException e) {
		String message = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
		return ResponseEntity.status(HttpStatus.CONFLICT).body(message);

	}

	@ExceptionHandler(value = IdentifierGenerationException.class)
	public ResponseEntity<String> exception(ConstraintViolationException e) {
		String message = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
		return new ResponseEntity<>(message, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
		String message = NestedExceptionUtils.getMostSpecificCause(e).getMessage();

		return new ResponseEntity<>("Error! " + message, HttpStatus.NOT_FOUND);
	}*/

}
