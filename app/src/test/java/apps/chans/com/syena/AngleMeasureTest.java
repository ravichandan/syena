package apps.chans.com.syena;

import org.junit.Test;

/**
 * Created by sitir on 17-03-2017.
 */

public class AngleMeasureTest {

    @Test
    public void measureAngle(){
        //System.out.println(angleFromCoordinate(16.3041505,80.4264179,16.3053441,80.4257411));
        System.out.println(angleFromCoordinate(16.3041505,80.4264179,16.3041505,80.4264179));
        System.out.println(angleFromCoordinate(16.3041505,80.4264179,16.3124287,80.4194815));
        System.out.println(angleFromCoordinate(16.3124287, 80.4194815, 16.3041505, 80.4264179));

        System.out.println(origCalc(16.3041505,80.4264179,16.3041505,80.4264179));
        System.out.println(origCalc(16.3041505,80.4264179,16.3124287,80.4194815));
        System.out.println(origCalc(16.3124287, 80.4194815, 16.3041505, 80.4264179));




    }

    private double angleFromCoordinate(double lat1, double long1, double lat2,
                                                                  double long2) {

    double dLon = (long2 - long1);

    double y = Math.sin(dLon) * Math.cos(lat2);
    double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
            * Math.cos(lat2) * Math.cos(dLon);

    double brng = Math.atan2(y, x);

    brng = Math.toDegrees(brng);
    brng = (brng + 360) % 360;
    brng = 360 - brng; // count degrees counter-clockwise - remove to make clockwise

    return brng;
}
    private double origCalc(double lat1, double lon1, double lat2,
                            double lon2){
        double dLon = (lon2 - lon1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        brng = 360 - brng; // count degrees counter-clockwise - remove to make clockwise

        float degree = (float) brng ;//- (dataSource.currentMember.getDegree() * (float) (180.0f / Math.PI));//-(float) brng ;
        //Log.d(LOG_TAG,"degree "+degree);

        if (degree < 0) degree = degree + 360;
        return brng;
    }
}
