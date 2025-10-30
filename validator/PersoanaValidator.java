package validator;

import domain.Persoana;
import domain.User;
import errors.ValidationError;

public class PersoanaValidator implements ValidationStrategy<Persoana> {
    private final ValidationStrategy<User> userValidator= new UserValidator();
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
