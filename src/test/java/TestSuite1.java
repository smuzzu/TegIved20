/**
 * TestNG suite for NASA rest API
 *
 * @author  Sebastian M
 * @version 1.0
 * @since   2023-03-10
 */

import org.apache.http.impl.client.CloseableHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.testng.Assert;


import java.util.ArrayList;
import java.util.HashMap;

public class TestSuite1 {


    static String nasaKey;
    public static String martianDateUrl;
    public static String heartDateUrl;
    public static ArrayList<String> expectedPhotoList;
    ArrayList<JSONObject> jsonObjectMartSolPhotoArray;
    ArrayList<String> reducedMartianSolPhotoList;
    ArrayList<String> reducedEarthDatePhotoList;

    /**
     * This method gets an array list containing the urls of all photos taken by "Curiosity" making rest
     * calls on NASA public API.
     *
     * @param url is the url used.  it is recommended using martianDateUrl or heartDateUrl vars which have
     *            the right format
     * @return an ArrayList<JSONObject> containing all the photos
     */
    private static ArrayList<JSONObject> getAlPhotosOnADay(String url){
        CloseableHttpClient client = HttpLib.buildHttpClient();
        int page=0;
        boolean finished=false;
        ArrayList<JSONObject> allThePhotos = new ArrayList<JSONObject>();
        while (!finished){
            String pagedUrl=url+"&page="+page;
            JSONObject jsonObject = HttpLib.getJsonObject(pagedUrl,client,true);
            JSONArray jsonArray = jsonObject.getJSONArray("photos");
            if (jsonArray.length()==0){
                finished=true;
                continue;
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                allThePhotos.add(jsonArray.getJSONObject(i));
            }
            page++;
        }
        return allThePhotos;
    }

    @BeforeSuite
    public void init(){
        nasaKey = "0bezqFqxxgP4NCOGr9gEbYtgLWuFbKugnH34bQds";
        martianDateUrl="https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?api_key="+nasaKey+"&sol=";
        heartDateUrl="https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?api_key="+nasaKey+"&earth_date=";

        expectedPhotoList = new ArrayList<String>();
        expectedPhotoList.add("http://mars.jpl.nasa.gov/msl-raw-images/proj/msl/redops/ods/surface/sol/01000/opgs/edr/fcam/FLB_486265257EDR_F0481570FHAZ00323M_.JPG");
        expectedPhotoList.add("http://mars.jpl.nasa.gov/msl-raw-images/proj/msl/redops/ods/surface/sol/01000/opgs/edr/fcam/FRB_486265257EDR_F0481570FHAZ00323M_.JPG");
        expectedPhotoList.add("http://mars.jpl.nasa.gov/msl-raw-images/proj/msl/redops/ods/surface/sol/01000/opgs/edr/rcam/RLB_486265291EDR_F0481570RHAZ00323M_.JPG");
        expectedPhotoList.add("http://mars.jpl.nasa.gov/msl-raw-images/proj/msl/redops/ods/surface/sol/01000/opgs/edr/rcam/RRB_486265291EDR_F0481570RHAZ00323M_.JPG");
        expectedPhotoList.add("http://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/1000MR0044631300503690E01_DXXX.jpg");
        expectedPhotoList.add("http://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/1000ML0044631300305227E03_DXXX.jpg");
        expectedPhotoList.add("http://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/1000MR0044631290503689E01_DXXX.jpg");
        expectedPhotoList.add("http://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/1000ML0044631290305226E03_DXXX.jpg");
        expectedPhotoList.add("http://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/1000MR0044631280503688E0B_DXXX.jpg");
        expectedPhotoList.add("http://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/1000ML0044631280305225E03_DXXX.jpg");
    }

    @AfterSuite
    public void end(){
        //driver.close();
    }

    /**
     * This test retrieves the first 10 Mars photos made by "Curiosity" on 1000 Martian sol.
     *
     */
    @Test public void Test001_Retrieve_10_Photos_On_1000_Martian_Sol(){
        int martianDay=1000;
        jsonObjectMartSolPhotoArray = getAlPhotosOnADay(martianDateUrl+martianDay);
        reducedMartianSolPhotoList = new ArrayList<String>();
        for (int i=0; i<10; i++){
            String photoUlr = jsonObjectMartSolPhotoArray.get(i).getString("img_src");
            reducedMartianSolPhotoList.add(photoUlr);
        }
        for (String photoUrl: reducedMartianSolPhotoList){
            Assert.assertTrue(expectedPhotoList.contains(photoUrl),photoUrl+" is in Mart Sol list but not in the expected results list");
        }
        for (String photoUrl: expectedPhotoList){
            Assert.assertTrue(reducedMartianSolPhotoList.contains(photoUrl),photoUrl+" is in the expected results list but is not in the Mart Solt list");
        }
    }

    /**
     * This test retrieves the first 10 Mars photos made by "Curiosity" on Earth date
     * equal to 1000 Martian sol.
     */
    @Test public void Test002_Retrieve_10_Photos_On_EarthDate_Equal_To_1000_Martian_Sol(){
        String earthDate ="2015-05-30";
        ArrayList<JSONObject> jsonObjectEarthDatePhotoArray = getAlPhotosOnADay(heartDateUrl+earthDate);
        reducedEarthDatePhotoList = new ArrayList<String>();
        for (int i=0; i<10; i++){
            String photoUlr = jsonObjectEarthDatePhotoArray.get(i).getString("img_src");
            reducedEarthDatePhotoList.add(photoUlr);
        }
        for (String photoUrl: reducedEarthDatePhotoList){
            Assert.assertTrue(expectedPhotoList.contains(photoUrl),photoUrl+" is in Earth Date list but not in the expected results list");
        }
        for (String photoUrl: expectedPhotoList){
            Assert.assertTrue(reducedEarthDatePhotoList.contains(photoUrl),photoUrl+" is in the expected results list but is not in the Heart Date list");
        }
    }

    /**
     * This test compares the first 10 Mars photos made by "Curiosity" on 1000 sol and
     * on Earth date equal to 1000 Martian sol.
     */
    @Test public void Test003_Retrieve_and_compare_photos_made_on_1000_sol_and_on_Earth_date_equal_to_1000_Martian_sol(){
        for (String photoUrl: reducedMartianSolPhotoList){
            Assert.assertTrue(reducedEarthDatePhotoList.contains(photoUrl),photoUrl+" is in Martian Sol list but not in Heart Date list");
        }
        for (String photoUrl: reducedEarthDatePhotoList){
            Assert.assertTrue(reducedMartianSolPhotoList.contains(photoUrl),photoUrl+" is in Earth Date list but not in Martian Sol list");
        }
    }


    /**
     * This test validates that the amounts of pictures that each "Curiosity" camera took on 1000 Mars sol
     * is not greater than 10 times the amount taken by other cameras on the same date.
     */
    @Test public void Test004_Picture_Aumonts_by_Camera_Validation_On_1000_Martian_Sol(){
        HashMap<String,Integer> photosByCameraMap = new HashMap();
        for (JSONObject photoObject: jsonObjectMartSolPhotoArray){
            JSONObject cameraObject = photoObject.getJSONObject("camera");
            String camera = cameraObject.getString("name");
            if (!photosByCameraMap.keySet().contains(camera)){
                photosByCameraMap.put(camera,1);
            } else {
                Integer total = (Integer)photosByCameraMap.get(camera);
                photosByCameraMap.replace(camera,++total);
            }
        }

        for (String camera1: photosByCameraMap.keySet()){
            Integer total1=photosByCameraMap.get(camera1);
            for (String camera2: photosByCameraMap.keySet()){
                if (camera1.equals(camera2)){
                    continue;
                }
                Integer total2=photosByCameraMap.get(camera2);
                Assert.assertTrue(total1 <= (total2 * 10), camera1
                        +" took "+total1+" photos, which us more than 10 times "+camera2+" which took only "
                        +total2+" photos");
            }
        }


    }


}