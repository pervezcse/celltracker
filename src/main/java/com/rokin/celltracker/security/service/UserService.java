package com.rokin.celltracker.security.service;

import com.rokin.celltracker.domain.Client;
import com.rokin.celltracker.repository.ClientRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

  @Resource
  private ClientRepository clientRepository;

  @Override
  public UserDetails loadUserByUsername(String username) {
    Optional<Client> client = clientRepository.findByEmailOrCellNo(username, username);
    List<SimpleGrantedAuthority> roles = client.isPresent()
        ? client.get().getRoles().stream().map(r -> new SimpleGrantedAuthority(r.toString()))
            .collect(Collectors.toList())
        : null;
    boolean userEnabled = client.isPresent() && client.get().isEnabled();

    return new UserDetails() {

      private static final long serialVersionUID = 1L;

      @Override
      public String getUsername() {
        return username;
      }

      @Override
      public String getPassword() {
        return client.isPresent() ? client.get().getPassword() : null;
      }

      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
      }

      @Override
      public boolean isEnabled() {
        return userEnabled;
      }

      @Override
      public boolean isCredentialsNonExpired() {
        return true;
      }

      @Override
      public boolean isAccountNonLocked() {
        return true;
      }

      @Override
      public boolean isAccountNonExpired() {
        return true;
      }
    };
  }
}
