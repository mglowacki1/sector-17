package pl.sector17.sector17.service;

import antlr.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.sector17.sector17.model.Role;
import pl.sector17.sector17.model.User;
import pl.sector17.sector17.model.VerificationToken;
import pl.sector17.sector17.repository.RoleRepository;
import pl.sector17.sector17.repository.TokenRepository;
import pl.sector17.sector17.repository.UserRepository;

import java.sql.Timestamp;
import java.util.*;

@Service
public class UserDetailsServiceImp implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private TokenRepository tokenRepository;

    public void saveUser(User user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName("user");
        user.setRoles(new HashSet<>(Arrays.asList(userRole)));
        userRepository.save(user);
    }

    public void saveToken(VerificationToken token){
        tokenRepository.save(token);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if(user != null) {
            List<GrantedAuthority> authorities = getUserAuthority(user.getRoles());
            return buildUserForAuthentication(user, authorities);
        } else {
            throw new UsernameNotFoundException("username not found");
        }
    }

    public User loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if(user != null) {
            return user;
        } else {
            throw new UsernameNotFoundException("email not found");
        }
    }


    private List<GrantedAuthority> getUserAuthority(Set<Role> userRoles) {
        Set<GrantedAuthority> roles = new HashSet<>();

        userRoles.forEach((role) -> {
            roles.add(new SimpleGrantedAuthority(role.getName()));
        });

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>(roles);
        return grantedAuthorities;
    }

    private UserDetails buildUserForAuthentication(User user, List<GrantedAuthority> authorities) {
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.isEnabled(), true, true, true, authorities);
    }

    public boolean checkTokenExpiration(String tokenvalue){
        VerificationToken token = tokenRepository.findByToken(tokenvalue);
        Timestamp expiryDate = token.getExpiryDate();
        if (expiryDate.after(new Timestamp(System.currentTimeMillis()))){
            return true;
        } else{
            return false;
        }
    }

    public void confirmUser(String tokenvalue) {
        VerificationToken token = tokenRepository.findByToken(tokenvalue);
        User user = token.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(token);
    }

    public void deleteToken(String tokenvalue){
        VerificationToken token = tokenRepository.findByToken(tokenvalue);
        tokenRepository.delete(token);
    }
}
