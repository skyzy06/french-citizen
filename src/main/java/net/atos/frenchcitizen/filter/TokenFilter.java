package net.atos.frenchcitizen.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import net.atos.frenchcitizen.exception.NotFoundException;
import net.atos.frenchcitizen.helper.TokenHelper;
import net.atos.frenchcitizen.model.Citizen;
import net.atos.frenchcitizen.service.CitizenService;
import net.atos.frenchcitizen.service.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TokenFilter extends OncePerRequestFilter {
    private final static Logger LOG = LoggerFactory.getLogger(TokenFilter.class);

    @Autowired
    private TokenHelper tokenHelper;

    @Autowired
    private CitizenService citizenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getTokenBearerValue(request);
            DecodedJWT decodedJWT = tokenHelper.decode(token);
            if (tokenHelper.isValid(decodedJWT)) {
                long id = Long.parseLong(decodedJWT.getSubject());
                Citizen citizen = citizenService.findCitizenById(id).orElseThrow(() -> new NotFoundException(null, null));
                UserDetails userDetails = UserDetailsImpl.build(citizen);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            LOG.error("Unable to log the user");
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenBearerValue(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        String bearerPrefix = "Bearer ";

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(bearerPrefix)) {
            return headerAuth.substring(bearerPrefix.length());
        }

        return null;
    }
}
