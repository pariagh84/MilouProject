package aut.ap.validation;

public interface Validator<T> {
    void validate(T t) throws Exception;
}