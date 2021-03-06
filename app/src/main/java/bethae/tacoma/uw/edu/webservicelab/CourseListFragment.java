package bethae.tacoma.uw.edu.webservicelab;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

//import bethae.tacoma.uw.edu.webservicelab.dummy.DummyContent;
//import bethae.tacoma.uw.edu.webservicelab.dummy.DummyContent.DummyItem;
import bethae.tacoma.uw.edu.webservicelab.model.Course;
import bethae.tacoma.uw.edu.webservicelab.data.CourseDB;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CourseListFragment extends Fragment {


    private static final String COURSE_URL = "http://cssgate.insttech.washington.edu/~ldimov/test.php?cmd=courses";

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private RecyclerView mRecyclerView;
    private CourseDB mCourseDB;
    private List<Course> mCourseList;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CourseListFragment() {
    }

//    // TODO: Customize parameter initialization
//    @SuppressWarnings("unused")
//    public static CourseListFragment newInstance(int columnCount) {
//        CourseListFragment fragment = new CourseListFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_COLUMN_COUNT, columnCount);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
        }



        FloatingActionButton floatingActionButton = (FloatingActionButton)
                getActivity().findViewById(R.id.fab);
        floatingActionButton.show();

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            DownloadCoursesTask task = new DownloadCoursesTask();
            task.execute(new String[]{COURSE_URL});
        }
        else {
            Toast.makeText(view.getContext(),
                    "No network connection available. Cannot display courses",
                    Toast.LENGTH_SHORT) .show();

            if (mCourseDB == null) {
                mCourseDB = new CourseDB(getActivity());
            }
            if (mCourseList == null) {
                mCourseList = mCourseDB.getCourses();
            }
            mRecyclerView.setAdapter(new MyCourseRecyclerViewAdapter(mCourseList, mListener));

        }

        try {
            InputStream inputStream = getActivity().openFileInput(
                    getString(R.string.LOGIN_FILE));

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                Toast.makeText(getActivity(), stringBuilder.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Course course);


    }

    private class DownloadCoursesTask extends AsyncTask<String, Void, String> {

        @Override protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection)
                            urlObject.openConnection();
                    InputStream content = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    response = "Unable to download the list of courses, Reason: "
                            + e.getMessage();
                }
                finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {     // Something wrong with the network or the URL.
         if (result.startsWith("Unable to")) {
            Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_LONG).show();
             return;
         }
            mCourseList = new ArrayList<Course>();
         result = Course.parseCourseJSON(result, mCourseList);
         // Something wrong with the JSON returned.

          if (result != null) {
              Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_LONG).show();
              return;
          }     // Everything is good, show the list of courses.
           if (!mCourseList.isEmpty()) {

               mRecyclerView.setAdapter(new MyCourseRecyclerViewAdapter(mCourseList, mListener));

                if(mCourseDB == null){
                    mCourseDB = new CourseDB(getActivity());
                }

                // Delete old data so that you can refresh the local
                // database with the network data.
                mCourseDB.deleteCourses();

                //Also, add to the local database
                for(int i =0; i<mCourseList.size(); i++){
                    Course course = mCourseList.get(i);
                    mCourseDB.insertCourse(course.getCourseID(),
                            course.getShortDescription(),
                            course.getLongDescription(),
                            course.getPrereqs());

                }

           }
        }


    }
}
