package aut.ap.exceptions;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String message) {
        super(message);
    }
    public InvalidEmailException() { super("Email must End with @milou.com ."); }
}
