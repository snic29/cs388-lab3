package com.codepath.nationalparks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.RequestParams
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import android.util.Log
import org.json.JSONArray
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// --------------------------------//
// CHANGE THIS TO BE YOUR API KEY  //
// --------------------------------//
private const val API_KEY = "Bvzp0tGYvcMAZd7hf2ReevjO1jSaAGmlVKCjo8P9"

/*
 * The class for the only fragment in the app, which contains the progress bar,
 * recyclerView, and performs the network calls to the National Parks API.

 */
class NationalParksFragment : Fragment(), OnListFragmentInteractionListener {
        /*
     * Constructing the view
     */
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_national_parks_list, container, false)
            val progressBar = view.findViewById<View>(R.id.progress) as ContentLoadingProgressBar
            val recyclerView = view.findViewById<View>(R.id.list) as RecyclerView
            val context = view.context

            recyclerView.layoutManager = LinearLayoutManager(context)

            updateAdapter(progressBar, recyclerView)
            return view
        }

    /*
     * Updates the RecyclerView adapter with new data.  This is where the
     * networking magic happens!
     */
    private fun updateAdapter(progressBar: ContentLoadingProgressBar, recyclerView: RecyclerView) {
        progressBar.show()

        // Create and set up an AsyncHTTPClient() here
        val client = AsyncHttpClient()
        val params = RequestParams()
        params["api_key"] = API_KEY

        // Using the client, perform the HTTP request
        client[
            "https://developer.nps.gov/api/v1/parks",
            params,
            object : JsonHttpResponseHandler(){
            /*
             * The onSuccess function gets called when
             * HTTP response status is "200 OK"
             */
            override fun onSuccess(
                statusCode: Int,
                headers: Headers,
                json: JsonHttpResponseHandler.JSON
            ) {
                // The wait for a response is over
                progressBar.hide()

                // Filter out the "data" JSON array and turn into a String
                val dataJSON = json.jsonObject.get("data") as JSONArray
                val parksRawJSON = dataJSON.toString()

                // Create a Gson instance to help parse the raw JSON
                val gson = Gson()

                // Tell Gson what type we’re expecting (a list of NationalPark objects)
                val arrayParkType = object : TypeToken<List<NationalPark>>() {}.type

                // Convert the raw JSON string into a list of actual NationalPark data models
                val models: List<NationalPark> = gson.fromJson(parksRawJSON, arrayParkType)

                recyclerView.adapter = NationalParksRecyclerViewAdapter(models, this@NationalParksFragment)

                // Look for this in Logcat:
                Log.d("NationalParksFragment", "response successful")
            }

            /*
             * The onFailure function gets called when
             * HTTP response status is "4XX" (eg. 401, 403, 404)
             */
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                errorResponse: String,
                t: Throwable?
            ) {
                // The wait for a response is over
                progressBar.hide()

                // If the error is not null, log it!
                t?.message?.let {
                    Log.e("NationalParksFragment", errorResponse)
                }
            }
        }]

    }

    /*
     * What happens when a particular park is clicked.
     */
    override fun onItemClick(item: NationalPark) {
        Toast.makeText(context, "Park Name: " + item.name, Toast.LENGTH_LONG).show()
    }

}
