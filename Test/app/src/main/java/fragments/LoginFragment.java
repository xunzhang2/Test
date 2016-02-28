package fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import helpers.PreferenceService;
import com.example.anna.test.R;


public class LoginFragment extends Fragment {
    private EditText etUsername = null;
    PreferenceService preferenceService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_login, container, false);
        etUsername = (EditText) v.findViewById(R.id.etUsername);

        v.findViewById(R.id.btnLoginSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save(v);
            }
        });
        return v;
    }

    public void save(View v) {

        String username = etUsername.getText().toString();
        preferenceService = new PreferenceService(getActivity().getApplicationContext());
        preferenceService.save(username);
        Toast.makeText(getActivity().getApplicationContext(), "Saved successfully", Toast.LENGTH_LONG).show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
