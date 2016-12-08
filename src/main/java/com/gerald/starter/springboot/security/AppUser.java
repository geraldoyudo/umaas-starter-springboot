package com.gerald.starter.springboot.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppUser implements UserDetails {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2955931888712168841L;
	@JsonProperty("externalId")
	private String id;
	private String email;
	private String username;
	private List<String> groups;
	private List<String> roles;
	private boolean disabled;
	private boolean locked;
	private boolean credentialsExpired;
	
	@JsonProperty("properties")
	private Map<String,Object> properties = new HashMap<>();
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getPassword() {
		return "";
	}
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	@Override
	public boolean isAccountNonLocked() {
		return !locked;
	}
	@Override
	public boolean isCredentialsNonExpired() {
		return !credentialsExpired;
	}
	@Override
	public boolean isEnabled() {
		return !disabled;
	}
}
