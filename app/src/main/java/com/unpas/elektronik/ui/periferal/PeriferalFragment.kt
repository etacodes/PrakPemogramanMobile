package com.unpas.elektronik.ui.periferal

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.unpas.elektronik.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar

class PeriferalFragment : Fragment() {

    val db by lazy { PeriferalDatabase(requireContext()) }
    private lateinit var jenisAdapter: ArrayAdapter<PeriferalData.Jenis>

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_periferal, container, false)
        val tableLayout: TableLayout = root.findViewById(R.id.tableLayout)
        val horizontalScrollView: HorizontalScrollView = root.findViewById(R.id.horizontalScrollView)

        CoroutineScope(Dispatchers.IO).launch {
            val periferalList = db.periferalDao().getAllPeriferals()

            requireActivity().runOnUiThread {
                for (index in periferalList.indices) {
                    val periferal = periferalList[index]
                    val tableRow = TableRow(requireContext())

                    val namaCell = createTableCell(periferal.nama)
                    val hargaCell = createTableCell(periferal.harga.toString())
                    val deskripsiCell = createTableCell(periferal.deskripsi)
                    val jenisCell = createTableCell(periferal.jenis)

                    // Set warna latar belakang
                    if (index % 2 == 0) {
                        tableRow.setBackgroundColor(Color.parseColor("#FFA3FD"))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor("#E5B8F4"))
                    }

                    tableRow.addView(namaCell)
                    tableRow.addView(hargaCell)
                    tableRow.addView(deskripsiCell)
                    tableRow.addView(jenisCell)

                    // Add edit icon
                    val editIcon = createEditIcon(periferal)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(periferal)
                    tableRow.addView(deleteIcon)

                    tableLayout.addView(tableRow)
                }
            }
        }

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomSheetView = inflater.inflate(R.layout.bottom_sheet_periferal, container, false)

            val namaText = bottomSheetView.findViewById<EditText>(R.id.namaText)
            val hargaText = bottomSheetView.findViewById<EditText>(R.id.hargaText)
            val deskripsiText = bottomSheetView.findViewById<EditText>(R.id.deskripsiText)
            val jenisText = bottomSheetView.findViewById<Spinner>(R.id.jenisText)
            val button = bottomSheetView.findViewById<Button>(R.id.periferalButton)

            // Inisialisasi Spinner
            val jenisAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                PeriferalData.Jenis.values().map { it.name }
            )

            jenisText.adapter = jenisAdapter

            val retrofit = Retrofit.Builder()
                .baseUrl("https://ppm-api.gusdya.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val periferalApi = retrofit.create(PeriferalApi::class.java)

            button.setOnClickListener {
                val nama = namaText.text.toString()
                val hargaText = hargaText.text.toString()
                val deskripsi = deskripsiText.text.toString()
                val jenis = jenisText.selectedItem.toString()

                if (nama.isEmpty() || hargaText.isEmpty() || deskripsi.isEmpty()) {
                    requireActivity().runOnUiThread{
                    showToast("Harap isi semua data terlebih dahulu")

                    }
                    return@setOnClickListener
                }

                val harga: Int
                try {
                    harga = hargaText.toInt()
                } catch (e: NumberFormatException) {
                    requireActivity().runOnUiThread{
                    showToast("Data harus berisi integer")

                    }
                    return@setOnClickListener
                }

                val periferalData = PeriferalData(0, nama, harga, deskripsi, jenis)

                CoroutineScope(Dispatchers.IO).launch {
                    db.periferalDao().insertPeriferal(periferalData)

                    // Tambahkan data ke endpoint menggunakan Retrofit
                    try {
                        val response = periferalApi.addPeriferal(periferalData)
                        if (response.isSuccessful) {
                            bottomSheetDialog.dismiss()
                        } else {
                            requireActivity().runOnUiThread{
                            showToast("Gagal menambahkan data ke server")

                            }
                        }
                    } catch (e: Exception) {
                        requireActivity().runOnUiThread{
                        showToast("Gagal menambahkan data ke server: ${e.message}")

                        }
                    }
                }

                bottomSheetDialog.dismiss()
                requireActivity().runOnUiThread{
                showToast("Data berhasil ditambahkan")

                }
                refreshPeriferalList()
            }

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }

        return root
    }

    private fun refreshPeriferalList() {
        CoroutineScope(Dispatchers.IO).launch {
            val periferalList = db.periferalDao().getAllPeriferals()

            requireActivity().runOnUiThread {
                val tableLayout: TableLayout = requireView().findViewById(R.id.tableLayout)
                val childCount = tableLayout.childCount

                // Remove all views except the header row
                tableLayout.removeViews(1, childCount - 1)

                for (index in periferalList.indices) {
                    val periferal = periferalList[index]
                    val tableRow = TableRow(requireContext())

                    val namaCell = createTableCell(periferal.nama)
                    val hargaCell = createTableCell(periferal.harga.toString())
                    val deskripsiCell = createTableCell(periferal.deskripsi)
                    val jenisCell = createTableCell(periferal.jenis)

                    // Set warna latar belakang
                    if (index % 2 == 0) {
                        tableRow.setBackgroundColor(Color.parseColor("#FFA3FD"))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor("#E5B8F4"))
                    }

                    tableRow.addView(namaCell)
                    tableRow.addView(hargaCell)
                    tableRow.addView(deskripsiCell)
                    tableRow.addView(jenisCell)

                    // Add edit icon
                    val editIcon = createEditIcon(periferal)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(periferal)
                    tableRow.addView(deleteIcon)

                    tableLayout.addView(tableRow)
                }
            }
        }
    }

    private fun createTableCell(text: String): TextView {
        val textView = TextView(requireContext())
        textView.text = text
        textView.setPadding(65, 16, 16, 16)
        return textView
    }

    private fun createEditIcon(periferal: PeriferalData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_edit_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            editPeriferal(periferal)
        }
        return imageView
    }

    @SuppressLint("MissingInflatedId")
    private fun editPeriferal(periferal: PeriferalData) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_periferal, null)

        val namaText = bottomSheetView.findViewById<EditText>(R.id.namaText)
        val hargaText = bottomSheetView.findViewById<EditText>(R.id.hargaText)
        val deskripsiText = bottomSheetView.findViewById<EditText>(R.id.deskripsiText)
        val jenisText = bottomSheetView.findViewById<Spinner>(R.id.jenisText)
        val button = bottomSheetView.findViewById<Button>(R.id.periferalButton)

        namaText.setText(periferal.nama)
        hargaText.setText(periferal.harga.toString())
        deskripsiText.setText(periferal.deskripsi)
        val jenisAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            PeriferalData.Jenis.values().map { it.name }
        )
        jenisText.adapter = jenisAdapter
        val jenisPosition = jenisAdapter.getPosition(periferal.jenis.toString())
        jenisText.setSelection(jenisPosition)

        button.text = "Update Data Periferal"

        button.setOnClickListener {
            val updatedInput1 = namaText.text.toString()
            val updatedInput2Text = hargaText.text.toString()
            val updatedInput3 = deskripsiText.text.toString()
            val updatedInput5 = jenisText.selectedItem.toString()

            if (updatedInput1.isEmpty() || updatedInput2Text.isEmpty() || updatedInput3.isEmpty()) {
                requireActivity().runOnUiThread{
                showToast("Harap isi semua data terlebih dahulu")

                }
                return@setOnClickListener
            }

            val updatedInput2: Int
            try {
                updatedInput2 = updatedInput2Text.toInt()
            } catch (e: NumberFormatException) {
                requireActivity().runOnUiThread{
                showToast("Data harus berisi integer")

                }
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val updatedPeriferal = periferal.copy(
                    nama = updatedInput1,
                    harga = updatedInput2,
                    deskripsi = updatedInput3,
                    jenis = updatedInput5
                )
                db.periferalDao().updatePeriferal(updatedPeriferal)
            }

            bottomSheetDialog.dismiss()
            requireActivity().runOnUiThread{
            showToast("Data telah diperbarui")

            }
            refreshPeriferalList()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun createDeleteIcon(periferal: PeriferalData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_delete_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            deletePeriferal(periferal)
        }
        return imageView
    }

    private fun deletePeriferal(periferal: PeriferalData) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Apakah Anda yakin ingin menghapus data ini?")
            .setCancelable(false)
            .setPositiveButton("Ya") { dialog, id ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.periferalDao().deletePeriferal(periferal)
                    refreshPeriferalList()
                }
                dialog.dismiss()
                showToast("Data telah dihapus") // Custom function to show a toast
            }
            .setNegativeButton("Tidak") { dialog, id ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}