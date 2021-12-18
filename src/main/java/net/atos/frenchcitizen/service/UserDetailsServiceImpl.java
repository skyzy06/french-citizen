package net.atos.frenchcitizen.service;

import net.atos.frenchcitizen.model.Citizen;
import net.atos.frenchcitizen.repository.CitizenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    CitizenRepository repository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<Citizen> citizen = repository.findByUsername(s);
        return citizen.map(UserDetailsImpl::build).orElse(null);
    }
}
