package bustrack.example.bustrack.exception;


public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message)  {
        super(message);
    }
}