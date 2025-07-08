package com.ollie.mcsoc_hunt.exceptions.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ollie.mcsoc_hunt.exceptions.TaskNotFoundException;
import com.ollie.mcsoc_hunt.exceptions.TeamNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.logging.Logger;

@RestControllerAdvice
public class ExceptionManager {

    @Autowired
    ObjectMapper objectMapper;

    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<ObjectNode> handleTeamNotFound(TeamNotFoundException exception) {
        ObjectNode errorJson = objectMapper.createObjectNode();
        errorJson.put("message", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorJson);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ObjectNode> handleTaskNotFound(TaskNotFoundException exception) {
        ObjectNode errorJson = objectMapper.createObjectNode();
        errorJson.put("message", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorJson);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String msg = String.format("Invalid value '%s' for parameter '%s'", exception.getValue(), exception.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        Logger.getLogger("Exception Manager").warning(e.getMessage() + " " + e.getCause() + " " + Arrays.toString(e.getStackTrace()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body("Validation failed: " +
                e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

}
