package re.project_final_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import re.project_final_service.repo.TokenBlackListRepo;
import org.springframework.web.filter.OncePerRequestFilter;
import re.project_final_service.service.impl.RedisBlacklistService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider tokenProvider;
	private final UserDetailsService userDetailsService;
	private final TokenBlackListRepo tokenBlackListRepo;
	private final RedisBlacklistService redisBlacklistService;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			if (redisBlacklistService.isBlacklisted(token)) {

				response.sendError(
						HttpServletResponse.SC_UNAUTHORIZED,
						"Token đã bị thu hồi"
				);
				return;
			}
			if (tokenProvider.validateToken(token)) {
				String username = tokenProvider.getUsernameFromJwt(token);
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
				Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		}
		filterChain.doFilter(request, response);
	}
}


