package apps.chans.com.syena.web.response;

/**
 * Created by sitir on 02-02-2017.
 */

public class PinValidationResponse {

	public static final int SUCCESS = 0;
	public static final int NO_VALID_MEMBER = 1;
	public static final int NO_VALID_MEMBER_TXN = 2;
	public static final int INVALID_EMAIL = 3;
	public static final int INVALID_INSTALLATION_ID = 4;
	public static final int INVALID_PIN = 5;

	private int status;

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
