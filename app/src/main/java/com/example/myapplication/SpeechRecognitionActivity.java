package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.model.Disease;
import com.example.myapplication.model.Pokemon;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.example.myapplication.fixtures.MessagesFixtures;
import com.example.myapplication.model.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceContext;
import ai.api.AIServiceContextBuilder;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.ResponseMessage;
import de.hdodenhof.circleimageview.CircleImageView;

public class SpeechRecognitionActivity extends BaseSpeechRecognitionActivity
        implements MessageInput.InputListener,
        MessageInput.AttachmentsListener,
        MessageInput.TypingListener {

    private static final String TAG = "SpeechRecogActivity";

    //Firebase
    private FirebaseFirestore diagnosedb;
    private FirebaseFunctions mFunctions;

    //Volley
    public static RequestQueue mRequestQueue;

    // Android client
    private AIRequest aiRequest;
    private AIDataService aiDataService;
    private AIServiceContext customAIServiceContext;
    private String uuid = UUID.randomUUID().toString();

    private LinearLayout mbottomLinearLayout;
    private BottomSheetBehavior sheetBehavior;
    private MessageInput input;
    private TextView mbotlastseentxt;
    private ImageButton mbackBtn;
    private ImageButton mmoreBtn;
    private CircleImageView mprofilepic;

    private Boolean EyeDiseaseGeneralFallback = false;
    private Boolean endOfQuery = false;
    private Boolean followUpQuery = false;
    private Boolean ageInput = false;

    private Map<String, Object> docData = new HashMap<>();
    private String resolvedtitle = "";
    private String responseString = "";
    private String imgpredictedLabel = "";

    public static void open(Context context) {
        context.startActivity(new Intent(context, SpeechRecognitionActivity.class));
    }

    private MessagesList messagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_messages);

        mRequestQueue = Volley.newRequestQueue(getApplicationContext());

        mFunctions = FirebaseFunctions.getInstance();
        diagnosedb = FirebaseFirestore.getInstance();

        final ai.api.android.AIConfiguration config = new ai.api.android.AIConfiguration("c591a661d0044c8992ab2f1fc9afc315",
                AIConfiguration.SupportedLanguages.English,
                ai.api.android.AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(config);
        customAIServiceContext = AIServiceContextBuilder.buildFromSessionId(uuid);// helps to create new session whenever app restarts
        aiRequest = new AIRequest();

        mbotlastseentxt = (TextView) findViewById(R.id.botlastseentxt);

        mbackBtn = (ImageButton) findViewById(R.id.backBtn);
        mmoreBtn = (ImageButton) findViewById(R.id.moreBtn);
        mprofilepic = (CircleImageView) findViewById(R.id.profilepic);


        mbottomLinearLayout = (LinearLayout) findViewById(R.id.bottomLinearLayout);
        // init the bottom sheet behavior
        sheetBehavior = BottomSheetBehavior.from(mbottomLinearLayout);

        this.messagesList = (MessagesList) findViewById(R.id.messagesList);
        initAdapter();

        input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(this);
        input.setTypingListener(this);
        input.setAttachmentsListener(this);

       // predictData("2aac48f5-c288-4787-8a27-3edcf399ce9d");


        mbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mmoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(SpeechRecognitionActivity.this, mmoreBtn);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.newchat:

                                break;
                            case R.id.about:

                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        });

    }

    //Add response buttons
    private void addButtons(String btnName, int totaladdedView) {

        Button b = new Button(this);
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        rl.addRule(RelativeLayout.ALIGN_TOP);
        b.setLayoutParams(rl);
        b.setPadding(0, 0, 0, 10);
        b.setText(btnName);
        b.setTextColor(getResources().getColor(R.color.colorAccent));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float[] outerRadii = new float[8];
            Arrays.fill(outerRadii, 10 / 2);

            RoundRectShape shape = new RoundRectShape(outerRadii, null, null);
            ShapeDrawable mask = new ShapeDrawable(shape);

            ColorStateList stateList = ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));

            b.setBackground(new RippleDrawable(stateList, getResources().getDrawable(R.drawable.circle_transparent_btn), mask));

        } else {
            b.setBackgroundColor(getResources().getColor(R.color.white));
        }

        (mbottomLinearLayout).addView(b);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnName.equals("View Report")) {
                    Intent diseasereportintent = new Intent(SpeechRecognitionActivity.this, DiseaseReport.class);
                    diseasereportintent.putExtra("response",responseString);
                    startActivity(diseasereportintent);
                }
                else if(btnName.equals("Retry")) {
                    //Re-predict
                    predictData(uuid);
                }
                else if(btnName.equals("Take Picture")) {
                    //Re-predict
                    Intent pictureintent = new Intent(SpeechRecognitionActivity.this, ImgRecognitionActivity2.class);
                    startActivityForResult(pictureintent, 1);
                }
                else {
                    buttonPressed(b, totaladdedView);
                }
            }
        });

    }

    private void buttonPressed(Button b, int totaladdedView) {
        String btnText = b.getText().toString().toLowerCase();

    //    Toast.makeText(SpeechRecognitionActivity.this, btnText , Toast.LENGTH_SHORT).show();

        //Remove all views except TextInput
        mbottomLinearLayout.removeViews(1,totaladdedView);

        //Hide Bottom View
        sheetBehavior.setHideable(true);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Send response
        if (EyeDiseaseGeneralFallback && (btnText.equals("yes"))) {
            EyeDiseaseGeneralFallback = false;
            aiRequest.setQuery("eyediseasegeneralyes");
        }
        else if (btnText.equals("ok")) {
            aiRequest.setQuery("yes");
        }
        else if (btnText.equals("skip")) {
            if (!imgpredictedLabel.equals("")) {
                aiRequest.setQuery(imgpredictedLabel);
            }
            else {
                aiRequest.setQuery(" ");
            }
        }
        else {
            aiRequest.setQuery(btnText);
        }
        RequestTask requestTask = new RequestTask(SpeechRecognitionActivity.this, aiDataService, customAIServiceContext);
        requestTask.execute(aiRequest);

        //Put to HashMap
        if (!resolvedtitle.equals("")){
            //If it is a follow up intent, save the key as btn text
            if (followUpQuery) {

                followUpQuery = false;
                docData.remove(resolvedtitle);

                //Only save hashmap when user selected others but "None of the above"
                if (!btnText.equals("none of the above")) {
                    //Capitalize first letter
                    String btnTextname = btnText.substring(0,1).toUpperCase() + btnText.substring(1).toLowerCase();
                    docData.put(btnTextname, 1);
                }

            }
            //If it is NOT follow up, save hashmap if user response is ok or yes
            else {
                if ((btnText.equals("yes") || btnText.equals("ok"))) {
                    docData.put(resolvedtitle, 1);
                }
            }
        }

        messagesAdapter.addToStart(MessagesFixtures.getTextMessage(btnText.substring(0, 1).toUpperCase() + btnText.substring(1), false), true);
        mbotlastseentxt.setTypeface(null, Typeface.BOLD_ITALIC);
        mbotlastseentxt.setText("Typing...");
    }

    private void showTextInput() {
        input.setVisibility(View.VISIBLE);
        //Show Bottom View
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public boolean onSubmit(CharSequence input) {

        //Hide keyboard after pressing submit
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            //imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
        sheetBehavior.setHideable(true);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Send response
        aiRequest.setQuery(input.toString());
        RequestTask requestTask = new RequestTask(SpeechRecognitionActivity.this, aiDataService, customAIServiceContext);
        requestTask.execute(aiRequest);

        //Put to HashMap
        if (!resolvedtitle.equals("")){
            //If input is age number, convert to Integer
            if (ageInput) {
                docData.put(resolvedtitle, Integer.parseInt(input.toString()));
                ageInput = false;
            }
            else {
                docData.put(resolvedtitle, input.toString());
            }

        }

        mbotlastseentxt.setTypeface(null, Typeface.BOLD_ITALIC);
        mbotlastseentxt.setText("Typing...");

        super.messagesAdapter.addToStart(MessagesFixtures.getTextMessage(input.toString(), false), true);
        return true;
    }

    private void onSkip(String titlename) {
        sheetBehavior.setHideable(true);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        aiRequest.setQuery("yes");
        RequestTask requestTask = new RequestTask(SpeechRecognitionActivity.this, aiDataService, customAIServiceContext);
        requestTask.execute(aiRequest);

        //Put to HashMap
        if (!resolvedtitle.equals("")){
            docData.put(titlename, 1);
        }

        mbotlastseentxt.setTypeface(null, Typeface.BOLD_ITALIC);
        mbotlastseentxt.setText("Typing...");
    }

    public void callback(AIResponse aiResponse) {
        if (aiResponse != null && !endOfQuery) {

            //Get query title
            String intentname = aiResponse.getResult().getMetadata().getIntentName();
            String[] separatedTitle = intentname.split(" - ");
            String titlename = separatedTitle[separatedTitle.length-1].replace(" ?", "");

            Log.e(TAG, "intentname = " + intentname);

            if (intentname.contains("Symptom")){
                if (titlename.equals("End")) {
                    endOfQuery = true;
                    resolvedtitle = separatedTitle[separatedTitle.length-2];
                }
                else if (titlename.equals("FollowUp")) {
                    followUpQuery = true;
                }
                else {
                    resolvedtitle = titlename;
                }
            }
            else {
                endOfQuery = false;
                followUpQuery = false;
                resolvedtitle = "";
            }

            // Get Fulfilment Text
            int messageCount = aiResponse.getResult().getFulfillment().getMessages().size();

            if (messageCount > 1) {

                ResponseMessage.ResponseSpeech sysresponseMessage = (ResponseMessage.ResponseSpeech) aiResponse.getResult().getFulfillment().getMessages().get(messageCount-1);
                String systemReply = sysresponseMessage.getSpeech().toString().replace("[", "").replace("]", "");
                String[] separatedReply = systemReply.split(", ");

                //Check and Display Input Type
                Log.e(TAG, separatedReply[1]);
                if (separatedReply[1].equals("TextInput")) {
                    showTextInput();
                }
                else if (separatedReply[1].equals("RadioBox")) {
                    input.setVisibility(View.GONE);
                    for (int i = 2; i < separatedReply.length; i++) {
                        addButtons(separatedReply[i], separatedReply.length - 2);
                    }
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else if (separatedReply[1].equals("Skip")) {
                    onSkip(titlename);
                    return;
                }

                //Show Text Response
                for (int i = 0; i < messageCount -1 ; i++) {
                    ResponseMessage.ResponseSpeech responseMessage = (ResponseMessage.ResponseSpeech) aiResponse.getResult().getFulfillment().getMessages().get(i);
                    Log.e(TAG, "responseMessage: " + responseMessage.getSpeech());
                    String botReply = responseMessage.getSpeech().toString().replace("[", "").replace("]", "");

                    //Display messages or images
                    if (botReply.contains("Image")) {
                        String[] botReplies = botReply.split(", ");
                        String imgurl = botReplies[1];
                        mbotlastseentxt.setTypeface(null, Typeface.NORMAL);
                        mbotlastseentxt.setText("Online");
                        messagesAdapter.addToStart(MessagesFixtures.getImageMessage(true, imgurl), true);
                    }
                    else if (botReply.contains("Age")) {
                        ageInput = true;
                        input.getInputEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                    }
                    else if (botReply.contains("EyeDiseaseGeneral")) {
                        EyeDiseaseGeneralFallback = true;
                    }
                    else {
                        mbotlastseentxt.setTypeface(null, Typeface.NORMAL);
                        mbotlastseentxt.setText("Online");
                        messagesAdapter.addToStart(MessagesFixtures.getTextMessage(botReply, true), true);
                    }
                }
            }
            else if (messageCount == 1){
                String botReply = aiResponse.getResult().getFulfillment().getSpeech();
                messagesAdapter.addToStart(MessagesFixtures.getTextMessage(botReply, true), true);
            }

        }
        else if (endOfQuery) {
            String botReply = "Thanks for your time and cooperation! Please bear with me for a while I am generating your report.";
            messagesAdapter.addToStart(MessagesFixtures.getTextMessage(botReply, true), true);
            Log.e(TAG, "hash doc = " + docData);
            diagnosedb.collection("diagnose").document(uuid)
                .set(docData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        predictData(uuid);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        String botReply = "Unable to Response. Please try again.";
                        messagesAdapter.addToStart(MessagesFixtures.getTextMessage(botReply, true), true);
                    }
                });
        }
        else {
            Log.d(TAG, "Bot Reply: Null");
            String botReply = "Unable to Response. Please try again.";
            messagesAdapter.addToStart(MessagesFixtures.getTextMessage(botReply, true), true);
        }
    }

    private void predictData(String uuid) {

        final Map<String, Object> postData = new HashMap<>();
        postData.put("uuid", uuid);

        Gson gson = new Gson();
        final String json = gson.toJson(postData);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://us-central1-tflow-bbd2b.cloudfunctions.net/disease_diagnose", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "LOG_VOLLEY = "+ response);
              //  Toast.makeText(getApplicationContext(), "LOG_VOLLEY = " + response, Toast.LENGTH_LONG).show();
                if (response != null) {
                    //Make something with response
                    responseString = response;
                    messagesAdapter.addToStart(MessagesFixtures.getReportMessage("generatedreport", true, "You may download report now."), true);
                    addButtons("View Report", 1);
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    mbotlastseentxt.setTypeface(null, Typeface.NORMAL);
                    mbotlastseentxt.setText("Online");
                }
                else {
                  //  Toast.makeText(getApplicationContext(), "Failed to generate report", Toast.LENGTH_LONG).show();
                    messagesAdapter.addToStart(MessagesFixtures.getReportMessage("failedreport", true, "Failed to generate report"), true);
                    addButtons("Retry", 1);
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    mbotlastseentxt.setTypeface(null, Typeface.NORMAL);
                    mbotlastseentxt.setText("Online");
                }

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               // Toast.makeText(getApplicationContext(), "VolleyError = " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "VolleyError = "+ error);
              //  Toast.makeText(getApplicationContext(), "Failed to generate report", Toast.LENGTH_LONG).show();
                messagesAdapter.addToStart(MessagesFixtures.getReportMessage("failedreport", true, "Failed to generate report"), true);
                addButtons("Retry", 1);
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                mbotlastseentxt.setTypeface(null, Typeface.NORMAL);
                mbotlastseentxt.setText("Online");
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return json == null ? null : json.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                   // Toast.makeText(getApplicationContext(), "UnsupportedEncodingException = " + uee.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "UnsupportedEncodingException = "+ uee.getMessage());
                 //   Toast.makeText(getApplicationContext(), "Failed to generate report", Toast.LENGTH_LONG).show();
                    messagesAdapter.addToStart(MessagesFixtures.getReportMessage("failedreport", true, "Failed to generate report"), true);
                    addButtons("Retry", 1);
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    mbotlastseentxt.setTypeface(null, Typeface.NORMAL);
                    mbotlastseentxt.setText("Online");
                    return null;
                }
            }

        };
        mRequestQueue.add(stringRequest);


    }

    private void initAdapter() {
        super.messagesAdapter = new MessagesListAdapter<>(super.senderId, super.imageLoader);
        super.messagesAdapter.enableSelectionMode(this);
        super.messagesAdapter.setLoadMoreListener(this);
        super.messagesAdapter.registerViewClickListener(R.id.messageText,
                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
                    @Override
                    public void onMessageViewClick(View view, Message message) {
                        if (message.getId().equals("generatedreport")) {
                            Intent diseasereportintent = new Intent(SpeechRecognitionActivity.this, DiseaseReport.class);
                            diseasereportintent.putExtra("response",responseString);
                            startActivity(diseasereportintent);
                        }
                       // Toast.makeText(SpeechRecognitionActivity.this, message.getId() + " avatar click", Toast.LENGTH_SHORT).show();
                    }
                });
        this.messagesList.setAdapter(super.messagesAdapter);

        aiRequest.setQuery("start symptom checker");
        RequestTask requestTask = new RequestTask(SpeechRecognitionActivity.this, aiDataService, customAIServiceContext);
        requestTask.execute(aiRequest);
        mbotlastseentxt.setTypeface(null, Typeface.BOLD_ITALIC);
        mbotlastseentxt.setText("Typing...");

    }

    @Override
    public void onAddAttachments() {
        super.messagesAdapter.addToStart(
                MessagesFixtures.getImageMessage(true, ""), true);
    }

    @Override
    public void onStartTyping() {
        Log.v("Typing listener", getString(R.string.start_typing_status));
    }

    @Override
    public void onStopTyping() {
        Log.v("Typing listener", getString(R.string.stop_typing_status));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                ArrayList<String> labellist = data.getStringArrayListExtra("labellist");
                ArrayList<String> probabilitylist = data.getStringArrayListExtra("probabilitylist");
                String imageurl = data.getStringExtra("imageurl");
                Log.v("labellist", String.valueOf(labellist.size()) );
                Log.v("probabilitylist", probabilitylist.toString());
                Log.v("imageurl", imageurl);

                //Convert string array to int array
                ArrayList<Integer> intprobabilitylist = new ArrayList<Integer>();
                for (int i = 0; i < probabilitylist.size(); i++) {
                    intprobabilitylist.add(Integer.valueOf(probabilitylist.get(i)));
                }

                //Find maximum number
                int max = intprobabilitylist.get(0);
                for(int i=0;i<intprobabilitylist.size();i++)
                    max = Math.max(intprobabilitylist.get(i),max);

                //Get the position in array
                int maxposition = intprobabilitylist.indexOf(max);
                //Get the corresponding label
                imgpredictedLabel = labellist.get(maxposition);
             
                //Add image message
                messagesAdapter.addToStart(MessagesFixtures.getImageMessage(false, imageurl), true);

                //Remove all views except TextInput
                mbottomLinearLayout.removeViews(1,2);
                //Hide Bottom View
                sheetBehavior.setHideable(true);
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                //Send request
                aiRequest.setQuery("take picture");
                RequestTask requestTask = new RequestTask(SpeechRecognitionActivity.this, aiDataService, customAIServiceContext);
                requestTask.execute(aiRequest);
                mbotlastseentxt.setTypeface(null, Typeface.BOLD_ITALIC);
                mbotlastseentxt.setText("Typing...");

            }
        }
    }
}
