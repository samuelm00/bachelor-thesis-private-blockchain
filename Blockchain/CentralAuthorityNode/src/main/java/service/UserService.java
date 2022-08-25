package service;

import lombok.RequiredArgsConstructor;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder bCryptPasswordEncoder;

  public User save(User user) {
    if (userRepository.findByPublicKey(user.getPublicKey()).isPresent()) {
      throw new IllegalArgumentException("User already exists");
    }
    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    return userRepository.save(user);
  }

  public Optional<UserDetails> findByPublicKey(String publicKey) {
    Optional<User> user = userRepository.findByPublicKey(publicKey);
    if (user.isEmpty()) {
      return Optional.empty();
    }
    Collection<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    return Optional.of(new org.springframework.security.core.userdetails.User(publicKey, user.get().getPassword(), authorities));
  }
}
