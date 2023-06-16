package com.unpas.elektronik.ui.anggota

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.unpas.elektronik.R

class AnggotaFragment : Fragment() {

    data class Person(val npm: String, val nama: String)

    private val peopleList: List<Person> = listOf(
        Person("203040127", "Ericko Timur Apandi"),
        Person("203040120", "Dimas Prayuda"),
        Person("203040150", "M. Tegar Nurul Fuad Rosmali"),
        Person("203040135", "Rafi Nuril Akbar Firmansyah"),
        Person("203040162", "Ahmad Reyhan Ronaldo"),
        Person("203040155", "Hendri T. Padang"),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_anggota, container, false)
        val tableLayout: TableLayout = root.findViewById(R.id.tableLayout)

        for (index in peopleList.indices) {
            val person = peopleList[index]
            val tableRow = TableRow(requireContext())

            val npmCell = createTableCell(person.npm)
            val namaCell = createTableCell(person.nama)

            // Set warna latar belakang
            if (index % 2 == 0) {
                tableRow.setBackgroundColor(Color.parseColor("#99DBF5"))
            } else {
                tableRow.setBackgroundColor(Color.parseColor("#A7ECEE"))
            }

            tableRow.addView(npmCell)
            tableRow.addView(namaCell)

            tableLayout.addView(tableRow)
        }

        return root
    }

    override fun onResume() {
        super.onResume()

        // Menyembunyikan tombol appBarMain
        (requireActivity() as AppCompatActivity).findViewById<FloatingActionButton>(R.id.fab).hide()
    }

    override fun onPause() {
        super.onPause()

        // Menampilkan kembali tombol appBarMain
        (requireActivity() as AppCompatActivity).findViewById<FloatingActionButton>(R.id.fab).show()
    }

    private fun createTableCell(text: String): TextView {
        val textView = TextView(requireContext())
        textView.text = text
        textView.setPadding(65, 16, 16, 16)
        textView.layoutParams = TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.WRAP_CONTENT,
            1f
        )
        return textView
    }

}