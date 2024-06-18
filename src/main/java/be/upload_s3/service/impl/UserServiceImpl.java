package be.upload_s3.service.impl;

import be.upload_s3.dtos.UserResponseDto;
import be.upload_s3.entity.User;
import be.upload_s3.repository.UserRepository;
import be.upload_s3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUserName(username);
    }

    @Override
    public boolean existsUsersByEmail(String email) {
        return userRepository.existsUsersByEmail(email);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserResponseDto findUserByUsername(String username) {
        User user = userRepository.findUserByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("Could not find this user");
        }
        return new UserResponseDto(user.getId(), user.getUserName(), user.getEmail(), user.getCreated_at());
    }
}
