package cloud;

import com.auth0.jwt.algorithms.Algorithm;

public final class Cons {
	public static final class Env {
		public static final boolean IS_RELEASE = false;
	}

	public static final class Token {
		public static final String SECRET = "jfaksdjfiaosbjxcvbnfng";

		public static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

		public static final String KEY_USERNAME = "key_username";
	}

	public static final class Path {
		public static final String ROOT_DIR = FileUtil.getWholePath(System.getProperty("user.dir"), "CloudDriver");
		public static final String DATA_DIR = FileUtil.getWholePath(ROOT_DIR,"data");
		public static final String TEMP_DIR = FileUtil.getWholePath(ROOT_DIR,"temp");
		public static final String TEMP_UPLOAD_DIR = FileUtil.getWholePath(TEMP_DIR,"upload");
	}
}
