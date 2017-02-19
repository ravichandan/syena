package apps.chans.com.syena.web.response;

public class LocationResponse {

	public static final int ERROR = -1;
	public static final int SUCCESS = 0;

	public static final int NO_VALID_MEMBER = 1;
	public static final int INVALID_WATCH = 2;
	public static final int INVALID_EMAIL = 3;
	public static final int INVALID_INSTALLATION_ID = 4;

	private double distanceApart;

	private double latitude;

	private double longitude;

	private double altitude;

	private String watchStatus;

	private int status;

	/**
	 * @return the distanceApart
	 */
	public double getDistanceApart() {
		return distanceApart;
	}

	/**
	 * @param distanceApart
	 *            the distanceApart to set
	 */
	public void setDistanceApart(double distanceApart) {
		this.distanceApart = distanceApart;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the altitude
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * @param altitude
	 *            the altitude to set
	 */
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	/**
	 * @return the watchStatus
	 */
	public String getWatchStatus() {
		return watchStatus;
	}

	/**
	 * @param watchStatus
	 *            the watchStatus to set
	 */
	public void setWatchStatus(String watchStatus) {
		this.watchStatus = watchStatus;
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
