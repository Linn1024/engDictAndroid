package com.example.dictionary

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*


class SheetsQuickstart (val CREDENTIALS_FILE_PATH : InputStream, val TOKENS_DIRECTORY_PATH : File, var table : MutableList<DictWord>) :
    AsyncTask<Context, Void, Void?>() {
    private val APPLICATION_NAME = "Google Sheets API Java Quickstart"
    private val JSON_FACTORY: JsonFactory =
        JacksonFactory.getDefaultInstance()
    //private const val TOKENS_DIRECTORY_PATH = "/assets/tokens"

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES =
        Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY)
    //private const val CREDENTIALS_FILE_PATH = getAssets().open("credentials.json")

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport, context: Context): Credential {
        // Load client secrets.
        Log.i("Linn's log", "We are in credentials")
        val `in` = CREDENTIALS_FILE_PATH
        val clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))
        Log.i("Linn's log", "Client's secrets are done")
        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(TOKENS_DIRECTORY_PATH))
            .setAccessType("offline")
            .build()
        Log.i("Linn's log", "Flow is done ${flow.authorizationServerEncodedUrl}")
        //val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        Log.i("Linn's log", "Reciever is done ${clientSecrets.details}")
        val ans = GoogleCredential.Builder()
            .setClientSecrets(
                clientSecrets
            )
            .setTransport(HTTP_TRANSPORT)
            .setJsonFactory(JSON_FACTORY)
            .build()
        //val ans = AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
        /*val ab: AuthorizationCodeInstalledApp =
            object : AuthorizationCodeInstalledApp(flow, LocalServerReceiver()) {
                @Throws(IOException::class)
                override fun onAuthorization(authorizationUrl: AuthorizationCodeRequestUrl) {
                    val url = authorizationUrl.build()
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(browserIntent)
                }
            }*/
        Log.i("Linn's log", "Code is done")

        //val ans = ab.authorize("user").setAccessToken("user")

        Log.i("Linn's log", "Creds are done")
        return ans
    }

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */

    fun downloadTable(context: Context) {
        // Build a new authorized API client service.
        val properties = Properties()
        val assetManager = context.assets
        val inputStream = assetManager.open("assets/secretKeys.properties")
        properties.load(inputStream)
        Log.i("Linn's log", "We start")
        val HTTP_TRANSPORT = com.google.api.client.http.javanet.NetHttpTransport()
        val spreadsheetId = properties.getProperty("spreadsheetID")
        val range = "A1:C600"
        Log.i("Linn's log", "We continue")
        val service = Sheets.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            getCredentials(HTTP_TRANSPORT, context)
        )
            .setApplicationName(APPLICATION_NAME)
            .build()
        val myValues = service.spreadsheets().values()[spreadsheetId, range]
        myValues.key = properties.getProperty("myKey")
        Log.i("Linn's log", "We made service ${myValues.key}.")
        val response =
            myValues.execute()
        Log.i("Linn's log", "We got response")
        val values = response.getValues()
        val n = (values[0][1] as String).toInt()
        val newTable : ArrayList<DictWord> = ArrayList()
        if (values == null || values.isEmpty()) {
            println("No data found.")
        } else {
            values.drop(1).forEach{newTable.add(DictWord(it[0].toString().trim(), it[1].toString().trim(),
                (it[2] as String).toDouble()
            ))}
        }

        Log.i("Linn's log", newTable.size.toString())
        table.clear()
        table.addAll(newTable.subList(0, n).toMutableList())
    }


    override fun doInBackground(vararg params: Context?): Void? {
        Log.i("Linn's log", "at least i am here")
        params[0]?.let { downloadTable(it) }
        return null
    }
}
