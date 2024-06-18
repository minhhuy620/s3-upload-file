package be.upload_s3.service;


import be.upload_s3.dtos.UserResponseDto;
import be.upload_s3.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    boolean existsUsersByEmail(String email);
    User findByEmail(String email);
    UserResponseDto findUserByUsername(String username);

}
