package apps.chans.com.syena;

import org.junit.Test;

/**
 * Created by sitir on 12-03-2017.
 */

public class DistanceCalculationTest {

    @Test
    public  void mainTest( ) {
        System.out.println("Distance: " + calculateDistance(41.507483, 38.504048, -99.436554, -98.315949, 1.0, 1.0));
        System.out.println("Distance: " + calculateDistance(16.3041231040323, 16.3041231040323, 80.42640301741469, 80.43640301741469, 0, 0));

        System.out.println("Distance 3: " + calculateDistance(16.3041505, 16.3029532, 80.4264179, 80.426109, 0, 0));

    }//16.3029532,80.426109

    public static double calculateDistance(double lat1, double lat2, double lon1,
                                           double lon2, double el1, double el2) {

        final int radius = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = radius * c * 1000; // convert to meters

    /*double height = el1 - el2;

    distance = Math.pow(distance, 2) + Math.pow(height, 2);

    return Math.sqrt(distance);*/
        return distance;
    }
}
