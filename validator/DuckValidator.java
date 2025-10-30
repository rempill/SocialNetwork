package validator;

import domain.Duck;
import domain.User;
import domain.TipRata;
import errors.ValidationError;

public class DuckValidator implements ValidationStrategy<Duck> {
    private final ValidationStrategy<User> userValidator= new UserValidator();
    @Override
    public void validate(Duck duck){
        String errors="";
        userValidator.validate(duck);
        if(duck.getTipRata()==null) {
            errors += "Tip Rata is null!\n";
        }
        else{
            if(duck.getTipRata()!=TipRata.FLYING && duck.getTipRata()!=TipRata.SWIMMING && duck.getTipRata()!=TipRata.FLYING_AND_SWIMMING){
                errors += "Tip Rata is invalid!\n";
            }
        }
        if(duck.getViteza()<=0){
            errors+="Viteza must be positive!\n";
        }
        if(duck.getRezistenta()<=0){
            errors+="Rezistenta must be positive!\n";
        }
        if(!errors.isEmpty()){
            throw new ValidationError(errors);
        }
    }
}
