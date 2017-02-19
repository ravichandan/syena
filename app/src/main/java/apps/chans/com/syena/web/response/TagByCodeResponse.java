package apps.chans.com.syena.web.response;

public class TagByCodeResponse {
	public static final int SYSTEM_ERROR = -1;
	public static final int SUCCESS = 0;

	public static final int NO_VALID_MEMBER = 1;
	public static final int NO_VALID_MEMBER_TXN = 2;
	public static final int INVALID_EMAIL = 3;
	public static final int INVALID_TAG_CODE= 4;
	public static final int INVALID_INSTALLATION_ID= 5;
	public static final int INACTIVE_MEMBER=6;
	
	private String email;
	private int status;

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
}
