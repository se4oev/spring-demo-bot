package ru.se4oev.springdemobot.model;

import org.springframework.data.repository.CrudRepository;

/**
 * Created by karpenko on 10.10.2022.
 * Description:
 */
public interface UserRepository extends CrudRepository<User, Long> {

}
