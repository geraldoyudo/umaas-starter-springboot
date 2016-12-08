package com.gerald.starter.springboot.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;


@Component
@Data
public class AppAuthenticationProvider implements UserDetailsService, AuthenticationProvider{
	
	@Value("${umaas.core:http://test.isslserv.com:8070/umaas-core}")
	private String umaasCoreUrl;
	@Value("${umaas.manager.access.id:583c25b987e76b8908b3e640}")
	private String accessCodeId;
	@Value("${umaas.manager.access.code:1234}")
	private String accessCode;
	@Value("${umaas.manager.domain.id:583c259187e76b8908b3e63f}")
	private String domainId;
	@Autowired
	private RestTemplate restTemplate;
	@Getter
	private HttpHeaders headers;
	
	/*
	 * Initialize header with access credentials to api
	 */
	@PostConstruct
	public void init(){
		headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString(
				String.format("%s:%s", accessCodeId, accessCode).getBytes()));
		headers.setContentType(MediaType.APPLICATION_JSON);
	}
	
	/*
	 * Override authentication method by making a call to api
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if(!(authentication instanceof UsernamePasswordAuthenticationToken))
			return null;
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		checkAuthentication(username, password);
	    AppUser user  = getUser(username);
	    checkUser(user);
  	    Authentication auth = new UsernamePasswordAuthenticationToken(user, password, getAuthorities(user.getRoles()));
		return auth;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return getUser(username);
	}
	
	
	private void checkAuthentication (String username,String password)throws AuthenticationException{
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(umaasCoreUrl +
				"/domain/auth/authenticate")
		       .queryParam("user", username)
		       .queryParam("password", password)
		       .queryParam("domain", domainId);
		HttpEntity<?> entity = new HttpEntity(headers);
		AuthenticationResult result = restTemplate.exchange(
		        builder.build().encode().toUri(), 
		        HttpMethod.GET, 
		        entity, 
		        AuthenticationResult.class).getBody();
		
		if(!result.isAuthenticated()){
			throw new BadCredentialsException(Arrays.toString(result.getMessages().toArray()));
		}
		
	}
	
	private AppUser getUser(String username){
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(umaasCoreUrl +
				"/domain/appUsers/search/findByUsernameAndDomain")
		       .queryParam("username", username)
		       .queryParam("domain", domainId);
		HttpEntity<?> entity = new HttpEntity(headers);
		AppUser user = restTemplate.exchange(
		        builder.build().encode().toUri(), 
		        HttpMethod.GET, 
		        entity, 
		        AppUser.class).getBody();
		return user;
	}
	
	private Collection<? extends GrantedAuthority> getAuthorities(List<String> roles) {
		return roles.stream()
				.map((r) ->{ return new SimpleGrantedAuthority("ROLE_" + r.toUpperCase());} )
				.collect(Collectors.toList());
	}
	
	private void checkUser(UserDetails user) throws AuthenticationException {
		if(user == null){
			throw new UsernameNotFoundException("Username does not exist");
		}
		if(!user.isEnabled()){
			throw new DisabledException("Your account is disabled");
		}
		if(!user.isAccountNonLocked()){
			throw new LockedException("Your account has been locked");
		}
		if(!user.isCredentialsNonExpired()){
			throw new CredentialsExpiredException("Your password is expired. Please change");
		}
	}

	
	@Data
	public static class AuthenticationResult {
		@JsonProperty("auth")
		private boolean isAuthenticated;
		@JsonProperty(value = "roles", required = false)
		private Set<String> roles = new HashSet<>();
		@JsonProperty(value = "messages", required = false)
		private Set<String> messages = new HashSet<>();
	}
	
}
