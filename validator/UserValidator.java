package validator;

import domain.User;
import errors.ValidationError;

/**
 * Validator for the base {@link domain.User} type.
 * Performs basic checks on common User fields such as email, username and password.
 */
public class UserValidator implements ValidationStrategy<User> {
    /**
     * Validate the provided user.
     * Collects all validation problems into a single message and throws {@link ValidationError}
     * if any checks fail.
     *
     * @param user the user to validate
     * @throws ValidationError when the user is invalid
     */
    @Override
    public void validate(User user) throws ValidationError{
        String errors="";
        if(user==null){
            errors+="User is null!\n";
        }
        else{
            if(user.getEmail()==null){
                errors+="Email is null!\n";
            }
            else if(!user.getEmail().contains("@")){
                errors+="Email is invalid!\n";
            }
            if(user.getUsername()==null || user.getUsername().isEmpty()){
                errors+="Username is null or empty!\n";
            }
            if(user.getPassword()==null || user.getPassword().length()<8){
                errors+="Password is null or too short (min 8 characters)!\n";
            }
        }
        if(!errors.isEmpty()){
            throw new ValidationError(errors);
        }
    }
}
