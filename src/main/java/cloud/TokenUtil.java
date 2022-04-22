package cloud;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

public class TokenUtil {

	// ³¬Ê±¼ä¸ô
	private static final Long TIMEOUT = 30L * (60 * 1000);

	public static String getToken(String username) {

		Long now = (new Date()).getTime();

		return JWT.create().withClaim(Cons.Token.KEY_USERNAME, username).withExpiresAt(new Date(now + TIMEOUT))
				.sign(Cons.Token.ALGORITHM);
	}

	public static boolean vaild(String token) {
		try {
			JWT.require(Cons.Token.ALGORITHM).build().verify(token);
		} catch (JWTVerificationException exception) {
			return false;
		}
		return true;
	}

	public static boolean timeout(String token) {
		return JWT.decode(token).getExpiresAt().before(new Date());
	}

	public static String getUsername(String token) {
		return JWT.decode(token).getClaim(Cons.Token.KEY_USERNAME).asString();
	}
}
