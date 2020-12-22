package com.example.dictionary

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.annotation.IntegerRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.PrintWriter
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random.Default.nextDouble


class MainActivity : AppCompatActivity() {


    var table: MutableList<DictWord> = ArrayList()
    var n: Int = 0
    var weights: MutableList<Int> = ArrayList<Int>()
    var cur: Int = 0
    var countLastWords: Int = 0
    lateinit var showWordTextView: TextView
    lateinit var restTextView: TextView
    lateinit var translateWordTextView: TextView
    lateinit var frequencyTextView: TextView
    lateinit var tableFile: File

    fun newWord(w: Int) {
        //val showWordTextView = findViewById(R.id.wordText) as TextView
        //val showWordTextView = findViewById(R.id.wordText) as TextView
        var restSeq = 0
        if (restTextView.text.toString() == "") {
            restSeq = n
        } else {
            Log.i("Linn's log", restTextView.text.toString())
        }
        if (cur != -1) {
            if (w == -1) {
                weights[cur] /= 2
            }
            if (w == 1) {
                weights[cur] *= 2
            }
        }
        //print(len(weights) - weights.count(0))
        var totals = mutableListOf<Int>()
        var runningTotal = 0
        for (w in weights.subList(0, countLastWords)) {
            runningTotal += w
            totals.add(runningTotal)
        }
        val rnd = nextDouble() * runningTotal
        for ((i, e) in totals.withIndex()) {
            if (rnd < e) {
                cur = i
                break
            }
        }
        Log.i("Linn's log", "Winner is ${cur} with weight ${weights[cur]}")
        if (runningTotal == 0) {
            showWordTextView.text = "Thats'all folks!"
            return;
        }
        showWordTextView.text = table[cur].word
        translateWordTextView.text = ""
        frequencyTextView.text = table[cur].frequency.toString()
        restTextView.text = "${weights.subList(0, countLastWords).count { i -> i != 0 }} ($countLastWords)"

    }

    fun translateWord(view: View) {
        translateWordTextView.text = table[cur].translate
    }

    fun showWord(view: View) {
        newWord(0)
    }

    fun okWord(view: View) {
        newWord(-1)
    }

    fun wrongWord(view: View) {
        newWord(1)
    }

    fun changeCountLastWords(view: View) {
        countLastWords = restTextView.text.toString().toInt()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.myToolbar))

        showWordTextView = findViewById(R.id.wordText) as TextView
        translateWordTextView = findViewById(R.id.translateText) as TextView
        restTextView = findViewById(R.id.restText) as TextView
        frequencyTextView = findViewById(R.id.frequencyText) as TextView

        tableFile = File(getExternalFilesDir(null), "table")
        if (!tableFile.exists()) {
            val out = PrintWriter(tableFile)
            out.print(0)
            out.close()
        }

        try {
            val br = BufferedReader(FileReader(tableFile))
            n = br.readLine().toInt()
            for (i in 0 until n) {
                //Log.i("Linn's log", sc.nextLine())
                val (t1, t2, t3) = br.readLine().split("\t".toRegex())
                table.add(DictWord(t1, t2, t3.toDouble()))
            }
            countLastWords = n
            restTextView.text = n.toString()
            weights = MutableList(n) { 1 }
        } catch (e: Exception) {
            restTextView.text = "Failed to read file"
        }

        //val restTextView = findViewById(R.id.restText) as TextView



        //val s = SheetsQuickstart(assets.open("credentials.json"), File(getExternalFilesDir(null), "token"))
        //s.execute(applicationContext)
        //val sca = Scanner(exFile)
        //debugPrint(sca.next())


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show()
        return when (item.getItemId()) {
            R.id.downloadTable -> {
                Log.i("Linn's log", "Before download")
                val exec = SheetsQuickstart(
                    assets.open("credentials.json"),
                    File(getExternalFilesDir(null), "token"),
                    table
                )
                exec.execute(applicationContext)
                exec.get()
                n = table.size
                weights = MutableList(n) { 1 }
                //Log.i("Linn's log", "I am here before n with n = ${n}")
                restTextView.setText(n.toString())
                val out = PrintWriter(tableFile)
                out.println(n)
                for (e in table)
                    out.println("${e.word}\t${e.translate}\t${e.frequency}")
                out.close()
                true
            }
            R.id.clearTable -> {
                weights = MutableList(n) { 1 }
                restTextView.text = n.toString()
                countLastWords = n
                true

            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
