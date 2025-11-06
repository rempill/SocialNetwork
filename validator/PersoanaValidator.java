package validator;

import domain.Persoana;
import domain.User;
import errors.ValidationError;

/**
 * Validator for {@link domain.Persoana} objects.
 * Delegates common User validation to {@link UserValidator} and then performs
 * Persoana-specific checks.
 */
public class PersoanaValidator implements ValidationStrategy<Persoana> {
    private final ValidationStrategy<User> userValidator= new UserValidator();

    /**
     * Validate the provided Persoana. Collects validation problems and throws
     * {@link ValidationError} with a combined message if any checks fail.
     *
     * @param persoana the Persoana to validate
     * @throws ValidationError when validation fails
     */
    @Override
    public void validate(Persoana persoana) {
        String errors = "";
        userValidator.validate(persoana);
        if(persoana.getNume()==null || persoana.getNume().isEmpty()){
            errors+="Nume is null or empty!\n";
        }
        if(persoana.getPrenume()==null || persoana.getPrenume().isEmpty()) {
            errors += "Prenume is null or empty!\n";
        }
        if(persoana.getOcupatie()==null || persoana.getOcupatie().isEmpty()) {
            errors += "Ocupatie is null or empty!\n";
        }
        if(persoana.getDataNasterii()==null) {
            errors += "Data Nasterii is null!\n";
        }
        if(persoana.getNivelEmpatie()<1 || persoana.getNivelEmpatie()>10) {
            errors += "Nivel Empatie is out of range (1-10)!\n";
        }
        if(!errors.isEmpty()) {
            throw new ValidationError(errors);
        }
    }
}
