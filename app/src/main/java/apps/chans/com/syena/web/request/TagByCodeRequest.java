package apps.chans.com.syena.web.request;

public class TagByCodeRequest {

	private String requester;
	private String tagCode;

	/**
	 * @return the REQUESTER
	 */
	public String getRequester() {
		return requester;
	}

	/**
	 * @param REQUESTER
	 *            the REQUESTER to set
	 */
	public void setRequester(String requester) {
		this.requester = requester;
	}

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

}
