package apps.chans.com.syena.web.request;

public class WatchAccessRequest {

	private String requester;

	private String target;

	private boolean flag;

	/**
	 * @return the requester
	 */
	public String getRequester() {
		return requester;
	}

	/**
	 * @param requester
	 *            the requester to set
	 */
	public void setRequester(String requester) {
		this.requester = requester;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @return the flag
	 */
	public boolean isFlag() {
		return flag;
	}

	/**
	 * @param flag
	 *            the flag to set
	 */
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
}
