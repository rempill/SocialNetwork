package repo;

import domain.User;

public interface UserRepository extends Repository<Integer, User>{
    // Additional user-specific methods can be added here
    // Good for SRP :) dont want to need to remake repo when i wanna add
    //new functionality to user repo and i only have the generic
    //repo interface
}
