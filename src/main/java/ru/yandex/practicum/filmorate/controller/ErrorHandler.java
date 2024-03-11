package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.ItemAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.ItemNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.ErrorResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(final ValidationException e) {
        log.warn(e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(final MethodArgumentNotValidException e) {
        List<String> errorMessages = e.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
        log.warn(errorMessages.toString(), e);
        return new ErrorResponse(errorMessages.toString());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleItemAlreadyExist(final ItemAlreadyExistException e) {
        log.warn(e.getMessage(), e);
        return new ErrorResponse(String.format("Объект с id=%s уже существует", e.getId()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleItemNotFound(final ItemNotFoundException e) {
        log.warn(e.getMessage(), e);
        return new ErrorResponse(String.format("Объект с id=%s не найден", e.getId()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntimeError(final Throwable e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }
}
