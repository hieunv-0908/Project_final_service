package re.project_final_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.access-expiration}")
	private long jwtExpirationAccessMs;

	@Value("${jwt.refresh-expiration}")
	private long jwtExpirationRefreshMs;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}

	public String generateAccessToken(Authentication authentication) {
		String username = authentication.getName();
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtExpirationAccessMs);
		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(now)
				.setExpiration(expiry)
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public String getUsernameFromJwt(String token) {
		return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
			return true;
		} catch (ExpiredJwtException e) {
			throw new RuntimeException("TOKEN_EXPIRED");
		} catch (MalformedJwtException e) {
			throw new RuntimeException("TOKEN_MALFORMED");
		} catch (SignatureException e) {
			throw new RuntimeException("TOKEN_INVALID_SIGNATURE");
		} catch (Exception e) {
			throw new RuntimeException("TOKEN_INVALID");
		}
	}

	public Date getExpirationDate(String token) {

		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getExpiration();
	}
}


