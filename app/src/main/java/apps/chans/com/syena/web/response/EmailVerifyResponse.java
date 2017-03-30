package apps.chans.com.syena.web.response;

/**
 * Created by sitir on 02-02-2017.
 */

public class EmailVerifyResponse {

    public static final int ERROR = -1;
    public static final int SUCCESS = 0;

    public static final int NO_VALID_MEMBER = 1;
    public static final int NO_VALID_MEMBER_TXN = 2;
    public static final int INVALID_EMAIL = 3;
    public static final int SENDING_EMAIL_FAILED = 4;
    public static final int NEW_ENTRY = 6;
    public static final int NEW_DEVICE = 7;

    private String email;

    private int status;

    private String installationId;

    private boolean emailSent;

    private Error error;

    private MemberViewObject memberViewObject;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return the error
     */
    public Error getError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(Error error) {
        this.error = error;
    }

    /**
     * @return the installationId
     */
    public String getInstallationId() {
        return installationId;
    }

    /**
     * @param installationId the installationId to set
     */
    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    /**
     * @return the emailSent
     */
    public boolean isEmailSent() {
        return emailSent;
    }

    /**
     * @param emailSent the emailSent to set
     */
    public void setEmailSent(boolean emailSent) {
        this.emailSent = emailSent;
    }

    public MemberViewObject getMemberViewObject() {
        return memberViewObject;
    }

    public void setMemberViewObject(MemberViewObject memberViewObject) {
        this.memberViewObject = memberViewObject;
    }

    public static class Error {

        int errorCode;

        String errorMessage;

        /**
         * @return the errorCode
         */
        public int getErrorCode() {
            return errorCode;
        }

        /**
         * @param errorCode the errorCode to set
         */
        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        /**
         * @return the errorMessage
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * @param errorMessage the errorMessage to set
         */
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

    }
}
