package com.example.internship.security;

import com.example.internship.entity.User;
import com.example.internship.entity.UserRole;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {

  private Integer userId;
  private String username;
  private String password;
  private String email;
  private UserRole role;
  private boolean active;
  private Collection<? extends GrantedAuthority> authorities;

  public static UserPrincipal create(User user) {
    List<GrantedAuthority> authorities =
        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

    return UserPrincipal.builder()
        .userId(user.getUserId())
        .username(user.getUsername())
        .password(user.getPasswordHash())
        .email(user.getEmail())
        .role(user.getRole())
        .active(user.getIsActive())
        .authorities(authorities)
        .build();
  }

  public static UserPrincipal current() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
      throw new AccessDeniedException("Unauthorized access");
    }
    return principal;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return active;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return active;
  }
}
