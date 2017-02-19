package apps.chans.com.syena.web;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by sitir on 09-02-2017.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken= FirebaseInstanceId.getInstance().getToken();
        Log.d("*************   ", "Refreshed token: "+refreshedToken);
        System.out.println("*************   Refreshed token: "+refreshedToken);
        super.onTokenRefresh();
    }
}
