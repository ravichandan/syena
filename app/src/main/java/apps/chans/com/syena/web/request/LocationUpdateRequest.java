package apps.chans.com.syena.web.request;

import java.io.Serializable;

public class LocationUpdateRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3743097591720874219L;
	private String requester;
	private double latitude;
	private double longitude;
	private double altitude;

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

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "LocationUpdateRequest - Email : " + requester + ", latitude : " + latitude + ", longitude : " + longitude
				+ ", altitude : " + altitude;
	}
}
