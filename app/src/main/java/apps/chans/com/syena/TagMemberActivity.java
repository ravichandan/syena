package apps.chans.com.syena;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import java.io.IOException;

import apps.chans.com.syena.datasource.DataSource;
import apps.chans.com.syena.entities.Member;
import apps.chans.com.syena.entities.Watch;
import apps.chans.com.syena.web.request.TagByCodeRequest;
import apps.chans.com.syena.web.response.TagByCodeResponse;
import apps.chans.com.syena.web.response.TagCodeGenerationResponse;


/**
 * Created by sitir on 26-01-2017.
 */

public class TagMemberActivity extends AppCompatActivity {

    //TODO remove this temporary 'result' intent
    private Intent result;
    private DataSource dataSource = DataSource.instance;
    private String LOG_TAG = TagMemberActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_member_activity);
    }

    public void generateCode(View view) {
        final View popupView = getLayoutInflater().inflate(R.layout.generate_code_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, false);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        Button closeButton = (Button) popupView.findViewById(R.id.generateCodeCloseButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

            }
        });

        dataSource.sendStringRequest(Request.Method.GET, getString(R.string.get_tag_code_url, dataSource.getEmail()), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(LOG_TAG, "Received response from server " + response);
                if (StringUtils.isBlank(response)) {
                    Log.d(LOG_TAG, "Empty response received from server");
                    return;
                }
                ObjectMapper mapper = new ObjectMapper();
                TextView tagCodeResponseLabel = (TextView) popupView.findViewById(R.id.tagCodeResponseLabel);

                try {
                    TagCodeGenerationResponse tagCodeGenerationResponse = mapper.readValue(response, TagCodeGenerationResponse.class);
                    switch (tagCodeGenerationResponse.getStatus()) {
                        case TagCodeGenerationResponse.SUCCESS:
                            TextView codeText = (TextView) popupView.findViewById(R.id.codeText);
                            codeText.setText(tagCodeGenerationResponse.getTagCode());
                            break;
                        case TagCodeGenerationResponse.INVALID_EMAIL:
                            tagCodeResponseLabel.setText(getString(R.string.err_text_member_not_found));
                            break;
                        case TagCodeGenerationResponse.INVALID_INSTALLATION_ID:
                            tagCodeResponseLabel.setText(getString(R.string.err_text_member_not_found));
                            break;
                        case TagCodeGenerationResponse.NO_VALID_MEMBER:
                            tagCodeResponseLabel.setText(getString(R.string.err_text_member_not_found));
                            break;
                        case TagCodeGenerationResponse.NO_VALID_MEMBER_TXN:
                            tagCodeResponseLabel.setText(getString(R.string.err_text_member_not_found));
                            break;
                        case TagCodeGenerationResponse.INACTIVE_MEMBER:
                            tagCodeResponseLabel.setText(getString(R.string.err_text_member_not_active));
                            break;
                        case TagCodeGenerationResponse.SYSTEM_ERROR:
                            tagCodeResponseLabel.setText(getString(R.string.err_text_system_error));
                        default:
                            tagCodeResponseLabel.setText(getString(R.string.err_text_system_error));
                            break;
                    }
                } catch (IOException e) {
                    Log.d(LOG_TAG + "-ERROR", dataSource.getStackTrace(e));
                    tagCodeResponseLabel.setText(getString(R.string.err_text_system_error));
                    return;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TextView tagCodeResponseLabel = (TextView) popupView.findViewById(R.id.tagCodeResponseLabel);
                tagCodeResponseLabel.setText(error.getLocalizedMessage());
            }
        });
    }

    public void scanCode(View view) {
        final View popupView = getLayoutInflater().inflate(R.layout.scan_code_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        Button closeButton = (Button) popupView.findViewById(R.id.scanCodeCancelButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

            }
        });

        final EditText scanCodeText = (EditText) popupView.findViewById(R.id.scanCodeText);
        //TODO validate scanCodeText
        final Button submitButton = (Button) popupView.findViewById(R.id.scanCodeOkButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "In setOnClickListener********************************"+dataSource.getEmail());

                //tagMemberWithCode(scanCodeText);
                submitButton.setEnabled(false);
                TagByCodeRequest tagByCodeRequest = new TagByCodeRequest();
                tagByCodeRequest.setEmail(dataSource.getEmail());
                tagByCodeRequest.setTagCode(scanCodeText.getText().toString());
                final String url = getString(R.string.post_tag_url);
                dataSource.sendJsonRequest(Request.Method.POST, getString(R.string.post_tag_url), tagByCodeRequest, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        TextView tagByCodeResponseLabel = (TextView) popupView.findViewById(R.id.tagByCodeResponseLabel);
                        if (response == null) {
                            Log.d(LOG_TAG, "Empty response received from server");
                            submitButton.setEnabled(true);
                            tagByCodeResponseLabel.setText("Empty response received from server");
                            return;
                        }
                        Log.d(LOG_TAG, dataSource.getEmail() + " " + url + " Received response from server " + response.toString());

                        ObjectMapper mapper = new ObjectMapper();

                        try {
                            TagByCodeResponse tagByCodeResponse = mapper.readValue(response.toString(), TagByCodeResponse.class);

                            switch (tagByCodeResponse.getStatus()) {
                                case TagByCodeResponse.SUCCESS:
                                    dataSource.addToWatch(tagByCodeResponse.getEmail());
                                    popupWindow.dismiss();
                                    setResult(RESULT_OK, result);
                                    finish();
                                    break;
                                case TagByCodeResponse.INVALID_EMAIL:
                                    submitButton.setEnabled(true);
                                    tagByCodeResponseLabel.setText(getString(R.string.err_text_member_not_found));
                                    break;
                                case TagCodeGenerationResponse.INVALID_INSTALLATION_ID:
                                    submitButton.setEnabled(true);
                                    tagByCodeResponseLabel.setText(getString(R.string.err_text_member_not_found));
                                    break;
                                case TagCodeGenerationResponse.NO_VALID_MEMBER:
                                    submitButton.setEnabled(true);
                                    tagByCodeResponseLabel.setText(getString(R.string.err_text_member_not_found));
                                    break;
                                case TagCodeGenerationResponse.NO_VALID_MEMBER_TXN:
                                    submitButton.setEnabled(true);
                                    tagByCodeResponseLabel.setText(getString(R.string.err_text_member_not_found));
                                    break;
                                case TagCodeGenerationResponse.INACTIVE_MEMBER:
                                    submitButton.setEnabled(true);
                                    tagByCodeResponseLabel.setText(getString(R.string.err_text_member_not_active));
                                    break;
                                case TagCodeGenerationResponse.SYSTEM_ERROR:
                                    submitButton.setEnabled(true);
                                    tagByCodeResponseLabel.setText(getString(R.string.err_text_system_error));
                                default:
                                    submitButton.setEnabled(true);
                                    tagByCodeResponseLabel.setText(getString(R.string.err_text_system_error));
                                    break;
                            }
                        } catch (IOException e) {
                            Log.d(LOG_TAG + "-ERROR", dataSource.getStackTrace(e));
                            tagByCodeResponseLabel.setText(getString(R.string.err_text_system_error));
                            submitButton.setEnabled(true);
                            return;
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        TextView tagByCodeResponseLabel = (TextView) popupView.findViewById(R.id.tagByCodeResponseLabel);
                        tagByCodeResponseLabel.setText(error.getLocalizedMessage());
                        submitButton.setEnabled(true);

                    }
                });
            }
        });
    }

    private void tagMemberWithCode(EditText text) {
        System.out.println("In tagMemberWithCode********************************");
        Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("In onActivityResult*****************************");

        if (requestCode == 1 && resultCode == RESULT_OK) {
            result = data;
            Uri contact = data.getData();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getContentResolver().query(contact, projection, null, null, null);
            cursor.moveToFirst();

            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            Member m1 = new Member(name);
            if (dataSource.currentMember.getWatchMap().get(m1) == null) {
                Watch w = new Watch(dataSource.currentMember, m1);
                w.getWatchStatus().setDescription(getResources().getString(R.string.defaultDetailedMessage));
                dataSource.currentMember.getWatchMap().put(m1, w);
            }
            if (!MainActivity.dataSource.getWatchList().contains(dataSource.currentMember.getWatchMap().get(m1)))
                MainActivity.dataSource.getWatchList().add(dataSource.currentMember.getWatchMap().get(m1));
            cursor.close();
            setResult(RESULT_OK, result);
            finish();
        }
    }
}
