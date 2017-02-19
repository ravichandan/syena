package apps.chans.com.syena.web.response;

public class TagCodeGenerationResponse {
	public static final int SYSTEM_ERROR = -1;
	public static final int SUCCESS = 0;

	public static final int NO_VALID_MEMBER = 1;
	public static final int NO_VALID_MEMBER_TXN = 2;
	public static final int INVALID_EMAIL = 3;
	public static final int INVALID_INSTALLATION_ID=4;
	public static final int INACTIVE_MEMBER = 5;

	
	private String tagCode;

	private int status;

	/**
	 * @return the tagCode
	 */
	public String getTagCode() {
		return tagCode;
	}

	/**
	 * @param tagCode
	 *            the tagCode to set
	 */
	public void setTagCode(String tagCode) {
		this.tagCode = tagCode;
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
