package apps.chans.com.syena.web.request;


/**
 * Created by sitir on 01-02-2017.
 */


public class EmailVerifyRequest {

	private String email;

	private String instanceId;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
}
