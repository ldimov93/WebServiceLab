package bethae.tacoma.uw.edu.webservicelab.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bethany on 4/20/16.
 */
public class Course implements Serializable {

    public static final String ID = "id";
    public static final String SHORT_DESC = "shortDesc";
    public static final String LONG_DESC = "longDesc";
    public static final String PRE_REQS = "prereqs";

    String mCourseID;
    String mShortDescription;
    String mLongDescription;
    String mPrereqs;

    public Course(String course, String shortDesc, String longDesc, String prereq) {
        mCourseID = course;
        mShortDescription = shortDesc;
        mLongDescription = longDesc;
        mPrereqs = prereq;
    }

    public String getCourseID() {
        return mCourseID;
    }

    public void setCourseID(String courseID) {
        mCourseID = courseID;
    }

    public String getShortDescription() {
        return mShortDescription;
    }

    public void setShortDescription(String shortDescription) {
        mShortDescription = shortDescription;
    }

    public String getLongDescription() {
        return mLongDescription;
    }

    public void setLongDescription(String longDescription) {
        mLongDescription = longDescription;
    }

    public String getPrereqs() {
        return mPrereqs;
    }

    public void setPrereqs(String prereqs) {
        mPrereqs = prereqs;
    }


    /**  * Parses the json string, returns an error message if unsuccessful.  * Returns course list if success.
     * @param courseJSON  * @return reason or null if successful.
     */
    public static String parseCourseJSON(String courseJSON, List<Course> courseList) {
        String reason = null;
        if (courseJSON != null) {
            try {
                JSONArray arr = new JSONArray(courseJSON);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Course course = new Course(obj.getString(Course.ID), obj.getString(Course.SHORT_DESC)
                            , obj.getString(Course.LONG_DESC), obj.getString(Course.PRE_REQS));
                    courseList.add(course);
                }
            } catch (JSONException e) {
                reason =  "Unable to parse data, Reason: " + e.getMessage();
            }
        }
        return reason;
    }


}
