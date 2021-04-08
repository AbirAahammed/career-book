package redbull.ecard.UILayer.Profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.view.Display;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import redbull.ecard.DataLayer.Address;
import redbull.ecard.DataLayer.Contact;
import redbull.ecard.DataLayer.Profile;
import redbull.ecard.LogicLayer.CardDatabaseConnector;
import redbull.ecard.R;

import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import static redbull.ecard.LogicLayer.CardDatabaseConnector.getCachedUserProfile;

public class ProfileFragment extends Fragment{
    private ProfileViewModel profileViewModel;
    public ImageView hImageViewSemafor;
    private ImageView qrCodeIV;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    private static View rootView;
    private static boolean generatedProfile = false;
    private static CardDatabaseConnector connector;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        rootView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        ProfileDisplaySetup();

        qrCodeIV = (ImageView) rootView.findViewById(R.id.imageView2);// this is where the QR gonna be
        Display display = getActivity().getWindowManager().getDefaultDisplay();// display QR
        // This QR code is a bit messy, we can clean it up into methods
        int dimen = 550;// this is QR dimension
        Profile user = getCachedUserProfile();
        if(user != null){
            qrgEncoder = new QRGEncoder(user.getUID(),null,QRGContents.Type.TEXT, dimen);// now we can generate QR code
            bitmap = qrgEncoder.getBitmap();// get bot map
            qrCodeIV.setImageBitmap(bitmap);//put qr image to qrCodeIV
        }else{
            //Todo: Ashcrynous issue
        }
        return rootView;
    }

    // Setup the display for the users profile
    // Static so that we can call it on the success of the setup of the database
    public static void ProfileDisplaySetup()
    {
        // Custom exception needed
        if (rootView == null)
            return;

        if (connector == null)
            connector = new CardDatabaseConnector();

        // Setup events attached to the profile
        InitializeProfile(rootView);
        ProfileEventSetup(rootView);
    }

    // Initialize the profile to what it should be initially
    private static void InitializeProfile(View root)
    {
        Profile userInfo = getCachedUserProfile();
        generatedProfile = userInfo != null;

        if (!generatedProfile)
        {
            // Do nothing if the profile is not setup yet
        }
        else {
            String currentPhone = userInfo.getContact().getCellPhone();
            String currentEmail = userInfo.getContact().getEmailAddress();

            // Set the text for all the preview
            SetViewText((TextView) root.findViewById(R.id.descriptionPreview), userInfo.getDescription());
            SetViewText((TextView) root.findViewById(R.id.namePreview), userInfo.getName().toString());
            SetViewText((TextView) root.findViewById(R.id.phoneNumPreview), currentPhone);
            SetViewText((TextView) root.findViewById(R.id.emailPreview), currentEmail);

            SetViewText((TextView) root.findViewById(R.id.descriptionInput), userInfo.getDescription());
            SetViewText((TextView) root.findViewById(R.id.phoneInput), currentPhone);
            SetViewText((TextView) root.findViewById(R.id.emailInput), currentEmail);
            SetViewText((TextView) root.findViewById(R.id.addressInput), userInfo.getAddress().getFormattedAddress());

            SetViewText((TextView) root.findViewById(R.id.addressPreview), userInfo.getAddress().getFormattedAddress());
            SetViewText((TextView) root.findViewById(R.id.servicePreview), userInfo.getService());

            // Set the background template
            setTemplate((new CardDatabaseConnector()).fetchTemplate());
        }
    }

    // Set the text of a specific view object
    private static void SetViewText(TextView textObj, String newText)
    {
        // Custom exception needed, this is an error
        if (textObj == null)
            return;

        // Do nothing if the text is null or empty, since this may remove the hint and put irrelevant text on the screen
        if (newText == null || newText == "")
            return;

        textObj.setText(newText);
    }

    // Set the text of a specific view object
    private static void SetViewText(EditText textObj, String newText)
    {
        // Custom exception needed, this is an error
        if (textObj == null)
            return;

        textObj.setText(newText);
    }

    // Setup the event listeners for the view
    private static void ProfileEventSetup(View root)
    {
        // TODO create custom exceptions for this
        if (connector == null)
            return;

        // Text changing events
        SetTextEvent(root, (EditText) root.findViewById(R.id.descriptionInput), (TextView) root.findViewById(R.id.descriptionPreview), AdjustableViews.DESCRIPTION);
        SetTextEvent(root, (EditText) root.findViewById(R.id.phoneInput), (TextView) root.findViewById(R.id.phoneNumPreview), AdjustableViews.PHONE);
        SetTextEvent(root, (EditText) root.findViewById(R.id.emailInput), (TextView) root.findViewById(R.id.emailPreview), AdjustableViews.EMAIL);
        SetTextEvent(root, (EditText) root.findViewById(R.id.addressInput), (TextView) root.findViewById(R.id.addressPreview), AdjustableViews.ADDRESS);

        // Spinner events
        SetSpinnerEvent((Spinner)root.findViewById(R.id.serviceSpinnerProfile), (TextView) root.findViewById(R.id.servicePreview));

        // Template & save events
        setOnClickEventSave((Button) root.findViewById(R.id.save_changes));
        setOnClickEventTemplates ((Button) root.findViewById(R.id.template_1), 1);
        setOnClickEventTemplates ((Button) root.findViewById(R.id.template_2), 2);
    }

    // Set on click events for the
    private static void setOnClickEventTemplates (Button button, int templateNum)
    {
        // Add template changes
        if (!button.hasOnClickListeners())
        {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick (View v)
                {
                    (new CardDatabaseConnector()).TemplateUpdate(templateNum);
                    setTemplate (templateNum);
                }
            });
        }
    }

    private static void setTemplate(int template)
    {
        ((ImageView)rootView.findViewById(R.id.previewBackground)).setImageResource(template (new CardDatabaseConnector().fetchTemplate()));
    }

    // Save all the input from the user whom changes their profile, onto the database
    private static void setOnClickEventSave (Button button)
    {
        if (!button.hasOnClickListeners())
        {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick (View v)
                {
                    (new CardDatabaseConnector()).profileUpdate();
                }
            });
        }
    }

    private static void SetSpinnerEvent(Spinner spinnerObj, TextView targetView)
    {
        // Add an on change listener to the spinner to update the users profile
        if (!spinnerObj.hasOnClickListeners())
        {
            spinnerObj.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View selectedItemView, int position, long id) {
                    String newService = parent.getItemAtPosition(position).toString();

                    // Update cached profiles value
                    (new CardDatabaseConnector()).updateService(newService);

                    // Change the preview's text
                    targetView.setText(newService);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {
                    // Do nothing if nothing is selected
                }
            });
        }
    }

    // Create a text event that will update a preview, then return the View type that was changed and update the database
    private static void SetTextEvent(View objView, EditText inputView, TextView targetView, AdjustableViews type)
    {
        if (!inputView.hasOnClickListeners())
            inputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // After they have updated their service information, update the card preview and the information on the database
                String value = s.toString();

                CardDatabaseConnector connector = new CardDatabaseConnector();
                Profile profile = getCachedUserProfile();

                // Do nothing if cache is not yet registered
                if (profile == null)
                    return;

                Contact oldContact = profile.getContact();

                switch (type)
                {
                    case EMAIL:
                        Contact nContactE = new Contact (oldContact.getCellPhone(), oldContact.getHomePhone(), value);

                        // The email was valid
                        if (nContactE.validEmail()) {
                            // Update the text of the preview
                            targetView.setText(value);

                            connector.updateContact(nContactE);
                        }
                        break;
                    case DESCRIPTION:
                        // Description can be anything, always valid
                        targetView.setText(value);
                        connector.updateDescription(value);
                        break;
                    case PHONE:
                        Contact nContactP = new Contact (value, oldContact.getHomePhone(), oldContact.getEmailAddress());

                        if (nContactP.validCell())
                        {
                            connector.updateContact(nContactP);

                            // Update the text of the preview
                            targetView.setText(value);
                        }

                        break;
                    case ADDRESS:
                        Address nAddress = new Address();
                        nAddress.setHouseNumber(value);

                        if (nAddress.isValid())
                        {
                            connector.updateHouseAddress(value);
                            targetView.setText(value);
                        }
                        break;
                }
            }
        });
    }


    // Retrieve the template resource from its template number
    private static int template(int cardTemplateNum)
    {
        int ret = -1; // Invalid template
        Log.d ("Test", "" + cardTemplateNum);
        switch (cardTemplateNum)
        {
            case 1:
                ret = R.drawable.template2v2;
                break;
            case 2:
                ret = R.drawable.template3;;
        }

        return ret;
    }
}

