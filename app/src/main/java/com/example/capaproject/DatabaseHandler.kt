package com.example.capaproject

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.example.capaproject.SurveyReaderContract.SurveyEntry
import com.example.capaproject.WorkReaderContract.WorkEntry
import com.example.capaproject.DefaultReaderContract.DefaultEntry
import com.example.capaproject.SchoolReaderContract.SchoolEntry
import com.example.capaproject.UserPrefsContract.UserPrefsEntry
import com.example.capaproject.UserHistoryContract.UserHistoryEntry
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

//import com.example.capaproject.StateReaderContract.StateEntry

private object SurveyReaderContract{
    object SurveyEntry : BaseColumns{
        const val TABLE_NAME = "Survey"
        const val COLUMN_QUESTION = "Question"
        const val COLUMN_ANSWER = "Answer"
    }
}

private object UserHistoryContract{
    object UserHistoryEntry : BaseColumns{
        const val TABLE_NAME = "UserHistory"
        const val COLUMN_DATE_TIME = "DateTime"
        const val COLUMN_STATE = "State"
        const val COLUMN_LATITUDE = "Latitude"
        const val COLUMN_LONGITUDE = "Longitude"
    }
}

private object WorkReaderContract{
    object WorkEntry : BaseColumns{
        const val TABLE_NAME = "Work"
        const val COLUMN_PACKAGE = "Package"
        const val COLUMN_CLASS = "Class"
        const val COLUMN_WEIGHT = "Weight"
    }
}

private object DefaultReaderContract{
    object DefaultEntry : BaseColumns{
        const val TABLE_NAME = "DefaultTable"
        const val COLUMN_PACKAGE = "Package"
        const val COLUMN_CLASS = "Class"
        const val COLUMN_WEIGHT = "Weight"
    }
}

private object SchoolReaderContract{
    object SchoolEntry : BaseColumns{
        const val TABLE_NAME = "School"
        const val COLUMN_PACKAGE = "Package"
        const val COLUMN_CLASS = "Class"
        const val COLUMN_WEIGHT = "Weight"
    }
}

private object UserPrefsContract{
    object UserPrefsEntry : BaseColumns{
        const val TABLE_NAME = "UserPrefs"
        const val COLUMN_WIDGET = "Widget"
        const val COLUMN_PACKAGE = "Package"
        const val COLUMN_CLASS = "Class"
    }
}

private const val SURVEY_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${SurveyEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${SurveyEntry.COLUMN_QUESTION} TEXT," +
            "${SurveyEntry.COLUMN_ANSWER} TEXT)"

private const val USER_PREFS_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${UserPrefsEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${UserPrefsEntry.COLUMN_WIDGET} TEXT," +
            "${UserPrefsEntry.COLUMN_PACKAGE} TEXT," +
            "${UserPrefsEntry.COLUMN_CLASS} TEXT)"

private const val WORK_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${WorkEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${WorkEntry.COLUMN_PACKAGE} TEXT," +
            "${WorkEntry.COLUMN_CLASS} TEXT," +
            "${WorkEntry.COLUMN_WEIGHT} DOUBLE)"

private const val DEFAULT_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${DefaultEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${DefaultEntry.COLUMN_PACKAGE} TEXT," +
            "${DefaultEntry.COLUMN_CLASS} TEXT," +
            "${DefaultEntry.COLUMN_WEIGHT} DOUBLE)"

private const val SCHOOL_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${SchoolEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${SchoolEntry.COLUMN_PACKAGE} TEXT," +
            "${SchoolEntry.COLUMN_CLASS} TEXT," +
            "${SchoolEntry.COLUMN_WEIGHT} DOUBLE)"

private const val USER_HISTORY_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS ${UserHistoryEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${UserHistoryEntry.COLUMN_DATE_TIME} TEXT," +
            "${UserHistoryEntry.COLUMN_STATE} TEXT," +
            "${UserHistoryEntry.COLUMN_LATITUDE} DOUBLE," +
            "${UserHistoryEntry.COLUMN_LONGITUDE} DOUBLE)"

private const val SURVEY_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${SurveyEntry.TABLE_NAME}"
private const val WORK_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${WorkEntry.TABLE_NAME}"
private const val DEFAULT_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${DefaultEntry.TABLE_NAME}"
private const val SCHOOL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${SchoolEntry.TABLE_NAME}"
private const val USER_PREFS_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${UserPrefsEntry.TABLE_NAME}"
private const val USER_HISTORY_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${UserHistoryEntry.TABLE_NAME}"

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SURVEY_CREATE_ENTRIES)
        db.execSQL(WORK_CREATE_ENTRIES)
        db.execSQL(USER_PREFS_CREATE_ENTRIES)
        db.execSQL(USER_HISTORY_CREATE_ENTRIES)
        db.execSQL(SCHOOL_CREATE_ENTRIES)
        db.execSQL(DEFAULT_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SURVEY_DELETE_ENTRIES)
        db.execSQL(WORK_DELETE_ENTRIES)
        db.execSQL(USER_PREFS_DELETE_ENTRIES)
        db.execSQL(SCHOOL_DELETE_ENTRIES)
        //db.execSQL(USER_DATA_DELETE_ENTRIES)
        db.execSQL(DEFAULT_DELETE_ENTRIES)
        onCreate(db)
    }
    companion object{
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Database"
    }

    fun updateUserPrefs(prefs: UserPrefApps){
        updateUserPrefsData(prefs)
    }

    //Function to update user preferences for every state
    fun updateStatePrefs(prefs : UserPrefApps){
        updateWorkPrefs(prefs)
        updateSchoolPrefs(prefs)
    }

    //Function to update user preferences for Work state
    private fun updateWorkPrefs(prefs: UserPrefApps) {
        val db = this.writableDatabase
        db.execSQL(WORK_CREATE_ENTRIES)

        val clock = prefs.getAttr("Clock")
        val music = prefs.getAttr("Music")

        val clockPKG = clock.packageName
        val clockCLS = clock.className

        //var selection = "${WorkEntry.COLUMN_CLASS}=${clockCLS}"
        db.query(WorkEntry.TABLE_NAME, null, "${WorkEntry.COLUMN_CLASS}=${clockCLS}", null, null, null, null, null)

        val clockValues = ContentValues().apply{
            put(WorkEntry.COLUMN_PACKAGE, clockPKG)
            put(WorkEntry.COLUMN_CLASS, clockCLS)
        }

        db.update(WorkEntry.TABLE_NAME, clockValues, null, null)

        val musicPKG = music.packageName
        val musicCLS = music.className

        //selection = "SELECT * FROM ${WorkEntry.TABLE_NAME} WHERE ${WorkEntry.COLUMN_CLASS}=${musicCLS}"
        db.query(WorkEntry.TABLE_NAME, null, "${WorkEntry.COLUMN_CLASS}=${musicCLS}", null, null, null, null, null)

        val musicValues = ContentValues().apply{
            put(WorkEntry.COLUMN_PACKAGE, musicPKG)
            put(WorkEntry.COLUMN_CLASS, musicCLS)
        }

        db.update(WorkEntry.TABLE_NAME, musicValues, null, null)

        val selectQuery = "SELECT * FROM ${WorkEntry.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            //Log.d("test", cursor.getString(cursor.getColumnIndex(WorkEntry.COLUMN_CLASS)))
        }
        db.close()
    }

    private fun updateSchoolPrefs(prefs: UserPrefApps) {
        val db = this.writableDatabase
        db.execSQL(SCHOOL_CREATE_ENTRIES)

        val clock = prefs.getAttr("Clock")
        val music = prefs.getAttr("Music")

        val clockPKG = clock.packageName
        val clockCLS = clock.className

        val clockValues = ContentValues().apply{
            put(SchoolEntry.COLUMN_PACKAGE, clockPKG)
            put(SchoolEntry.COLUMN_CLASS, clockCLS)
        }

        db.replace(SchoolEntry.TABLE_NAME, null, clockValues)

        val musicPKG = music.packageName
        val musicCLS = music.className

        val musicValues = ContentValues().apply{
            put(SchoolEntry.COLUMN_PACKAGE, musicPKG)
            put(SchoolEntry.COLUMN_CLASS, musicCLS)
        }

        db.replace(SchoolEntry.TABLE_NAME, null, musicValues)

        val selectQuery = "SELECT * FROM ${SchoolEntry.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            Log.d("test", cursor.getString(cursor.getColumnIndex(SchoolEntry.COLUMN_CLASS)))
        }
        db.close()
    }

    //Adds or updates user preferences in database
    private fun updateUserPrefsData(prefs: UserPrefApps){
        val db = this.writableDatabase
        db.execSQL(USER_PREFS_CREATE_ENTRIES)

        val clock = prefs.getAttr("Clock")
        val music = prefs.getAttr("Music")
        val search = prefs.getAttr("Search")
        val email = prefs.getAttr("Email")
        val calendar = prefs.getAttr("Calendar")
        val notes = prefs.getAttr("Notes")
        val weather = prefs.getAttr("Weather")

        val clockPKG = clock.packageName
        val clockCLS = clock.className

        val musicPKG = music.packageName
        val musicCLS = music.className

        val searchPKG = search.packageName
        val searchCLS = search.className

        val emailPKG = email.packageName
        val emailCLS = email.className

        val calendarPKG = calendar.packageName
        val calendarCLS = calendar.className

        val notesPKG = notes.packageName
        val notesCLS = notes.className

        val weatherPKG = weather.packageName
        val weatherCLS = weather.className

        var values = ContentValues().apply {
            put(UserPrefsEntry.COLUMN_WIDGET, "Clock")
            put(UserPrefsEntry.COLUMN_PACKAGE, clockPKG)
            put(UserPrefsEntry.COLUMN_CLASS, clockCLS)
        }
        db.replace(UserPrefsEntry.TABLE_NAME, null, values)

        values.clear()

        values.put(UserPrefsEntry.COLUMN_WIDGET, "Music")
        values.put(UserPrefsEntry.COLUMN_PACKAGE, musicPKG)
        values.put(UserPrefsEntry.COLUMN_CLASS, musicCLS)

        db.replace(UserPrefsEntry.TABLE_NAME, null, values)

        values.clear()

        values.put(UserPrefsEntry.COLUMN_WIDGET, "Search")
        values.put(UserPrefsEntry.COLUMN_PACKAGE, searchPKG)
        values.put(UserPrefsEntry.COLUMN_CLASS, searchCLS)

        db.replace(UserPrefsEntry.TABLE_NAME, null, values)

        values.clear()

        values.put(UserPrefsEntry.COLUMN_WIDGET, "Email")
        values.put(UserPrefsEntry.COLUMN_PACKAGE, emailPKG)
        values.put(UserPrefsEntry.COLUMN_CLASS, emailCLS)

        db.replace(UserPrefsEntry.TABLE_NAME, null, values)

        values.clear()

        values.put(UserPrefsEntry.COLUMN_WIDGET, "Calendar")
        values.put(UserPrefsEntry.COLUMN_PACKAGE, calendarPKG)
        values.put(UserPrefsEntry.COLUMN_CLASS, calendarCLS)

        db.replace(UserPrefsEntry.TABLE_NAME, null, values)

        values.clear()

        values.put(UserPrefsEntry.COLUMN_WIDGET, "Notes")
        values.put(UserPrefsEntry.COLUMN_PACKAGE, notesPKG)
        values.put(UserPrefsEntry.COLUMN_CLASS, notesCLS)

        db.replace(UserPrefsEntry.TABLE_NAME, null, values)

        values.clear()

        values.put(UserPrefsEntry.COLUMN_WIDGET, "Weather")
        values.put(UserPrefsEntry.COLUMN_PACKAGE, weatherPKG)
        values.put(UserPrefsEntry.COLUMN_CLASS, weatherCLS)

        db.replace(UserPrefsEntry.TABLE_NAME, null, values)

        db.close()

        //updateStatePrefs(prefs)
    }

    fun getUserPrefs(): UserPrefApps{
        return getUserPrefsData()
    }

    //Gets user preference info from database and returns as UserPrefApps object
    private fun getUserPrefsData(): UserPrefApps{
        val db = this.readableDatabase
        db.execSQL(USER_PREFS_CREATE_ENTRIES)
        val prefs = UserPrefApps()

        val selectQuery = "SELECT * FROM ${UserPrefsEntry.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            var pkg = cursor.getString(cursor.getColumnIndex("Package"))
            val cls = cursor.getString(cursor.getColumnIndex("Class"))
            val compName = ComponentName(
                pkg,
                cls
            )
            when {
                cursor.getString(cursor.getColumnIndex("Widget")) == "Clock" -> prefs.clock = compName
                cursor.getString(cursor.getColumnIndex("Widget")) == "Music" -> prefs.music = compName
                /*else -> {
                    prefs.clock = ComponentName("", "")
                    prefs.music = ComponentName("", "")
                }*/
            }

            cursor.moveToNext()
        }
        cursor.close()
        db.close()

        return prefs
    }

    //Adds or updates work state info in database
    private fun updateWorkData(map: HashMap<AppWidgetProviderInfo?, Double>){
        val db = this.writableDatabase
        db.execSQL(WORK_DELETE_ENTRIES)
        db.execSQL(WORK_CREATE_ENTRIES)

        for(entry in map){
            val pkg = entry.key.packageName
            val cls = entry.key.className
            val weight = entry.value

            val values = ContentValues().apply{
                put(WorkEntry.COLUMN_PACKAGE, pkg)
                put(WorkEntry.COLUMN_CLASS, cls)
                put(WorkEntry.COLUMN_WEIGHT, weight)
            }
            db.insert(WorkEntry.TABLE_NAME, null, values)
        }
        db.close()
    }

    private fun updateDefaultData(map: HashMap<AppWidgetProviderInfo?, Double>){
        val db = this.writableDatabase
        db.execSQL(DEFAULT_DELETE_ENTRIES)
        db.execSQL(DEFAULT_CREATE_ENTRIES)

        val mapper = jacksonObjectMapper()
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        mapper.addMixIn(ComponentName::class.java,CNmixin::class.java)

        for(entry in map){
            val pkg = entry.key.packageName
            val cls = entry.key.className
            val weight = entry.value

            val values = ContentValues().apply{
                put(DefaultEntry.COLUMN_PACKAGE, pkg)
                put(DefaultEntry.COLUMN_CLASS, cls)
                put(DefaultEntry.COLUMN_WEIGHT, weight)
            }
            db.insert(DefaultEntry.TABLE_NAME, null, values)
        }
        db.close()
    }

    //Updates state info in corresponding table using passed string to check which state
    fun updateDatabaseState(stateName: String, map: HashMap<AppWidgetProviderInfo?, Double>){
        when(stateName){
            "atWork" -> updateWorkData(map)
            "default" -> updateDefaultData(map)
        }
    }

    fun updateDatabaseSurvey(userProfile: UserProfile){
        updateSurveyData(userProfile)
    }

    //Deletes all tables in database
    fun deleteData(){
        val db = this.writableDatabase
        onUpgrade(db, 1, 1)
        db.close()
    }

    //Adds or updates survey info in database
    private fun updateSurveyData(profile: UserProfile){
        val db = this.writableDatabase
        db.execSQL(SURVEY_CREATE_ENTRIES)

        for(entry in profile.getFieldNames()){
            val answer = profile.getField(entry)
            val values = ContentValues().apply{
                put(SurveyEntry.COLUMN_QUESTION, entry)
                put(SurveyEntry.COLUMN_ANSWER, answer)
            }
            db.replace(SurveyEntry.TABLE_NAME, null, values)
        }
        db.close()
    }

    //Uses passed string to get info from corresponding state table
    fun getStateData(stateName: String): HashMap<ComponentName, Double>? {
        return when (stateName) {
            "atWork" -> getWorkData()
            "default" -> getDefaultData()
            else -> return null
        }
    }

    private fun getDefaultData(): HashMap<ComponentName, Double> {
        val db = this.readableDatabase
        db.execSQL(DEFAULT_CREATE_ENTRIES)

        val map: HashMap<ComponentName, Double> = HashMap()
        val selectQuery = "SELECT * FROM ${DefaultEntry.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)
        cursor!!.moveToFirst()
        while(!cursor.isAfterLast){
            val pkg = cursor.getString(cursor.getColumnIndex("Package"))
            val cls = cursor.getString(cursor.getColumnIndex("Class"))
            val weight = cursor.getDouble(cursor.getColumnIndex("Weight"))
            val compName = ComponentName(
                pkg,
                cls
            )
            map[compName] = weight
            cursor.moveToNext()
        }
        cursor.close()
        db.close()
        return map
    }

    //Gets work state info from database and returns as HashMap
    private fun getWorkData(): HashMap<ComponentName, Double> {
        val db = this.readableDatabase
        db.execSQL(WORK_CREATE_ENTRIES)

        val map: HashMap<ComponentName, Double> = HashMap()
        val selectQuery = "SELECT * FROM ${WorkEntry.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)
        cursor!!.moveToFirst()
        while(!cursor.isAfterLast){
            val pkg = cursor.getString(cursor.getColumnIndex("Package"))
            val cls = cursor.getString(cursor.getColumnIndex("Class"))
            val weight = cursor.getDouble(cursor.getColumnIndex("Weight"))
            val compName = ComponentName(
                pkg,
                cls
            )
            map[compName] = weight
            cursor.moveToNext()
        }
        cursor.close()
        db.close()
        return map
    }

    fun getSurvey(): UserProfile {
        return getSurveyData()
    }

    //Gets survey info from database and returns as HashMap
    private fun getSurveyData(): UserProfile{
        val db = this.readableDatabase
        db.execSQL(SURVEY_CREATE_ENTRIES)

        var home = ""
        var work = ""
        var school = ""
        var gender = ""
        var birthday = ""

        val selectQuery = "SELECT * FROM ${SurveyEntry.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)
        cursor.moveToFirst()
        while(!cursor.isAfterLast){
            when(cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_QUESTION))){
                "Home" -> home = cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_ANSWER))
                "Work" -> work = cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_ANSWER))
                "School" -> school = cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_ANSWER))
                "Gender" -> gender = cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_ANSWER))
                "BirthDay" -> birthday = cursor.getString(cursor.getColumnIndex(SurveyEntry.COLUMN_ANSWER))
            }
            cursor.moveToNext()
        }
        cursor.close()
        db.close()
        return UserProfile(home, work, school, gender, birthday)
    }

    fun clearUserHistory(){
        val db = this.writableDatabase
        db.execSQL(USER_HISTORY_DELETE_ENTRIES)
        db.close()
    }

    fun updateUserHistory(userHistory: UserHistory){
        updateUserHistoryData(userHistory)
    }

    //Function to save user session information into database
    private fun updateUserHistoryData(userHistory: UserHistory){
        val db = this.writableDatabase
        db.execSQL(USER_HISTORY_CREATE_ENTRIES)

        val values = ContentValues().apply {
            put(UserHistoryEntry.COLUMN_DATE_TIME, userHistory.dateTime)
            put(UserHistoryEntry.COLUMN_STATE, userHistory.userState)
            put(UserHistoryEntry.COLUMN_LATITUDE, userHistory.latitude)
            put(UserHistoryEntry.COLUMN_LONGITUDE, userHistory.longitude)
        }
        db.insert(UserHistoryEntry.TABLE_NAME, null, values)

        val selectQuery = "SELECT * FROM ${UserHistoryEntry.TABLE_NAME}"
        val cursor = db.rawQuery(selectQuery, null)
        cursor.moveToLast()
        Log.d("test", cursor.getString(cursor.getColumnIndex(UserHistoryEntry.COLUMN_DATE_TIME)))
        Log.d("test", cursor.getString(cursor.getColumnIndex(UserHistoryEntry.COLUMN_STATE)))
        //cursor.moveToFirst()
        /*while(!cursor.isAfterLast){
            if(cursor.getString(cursor.getColumnIndex(UserHistoryEntry.COLUMN_STATE)) == stateName){
                db.insert(UserHistoryEntry.TABLE_NAME, null, values)
            }
            cursor.moveToNext()
        }*/
        cursor.close()
        db.close()
    }
}
