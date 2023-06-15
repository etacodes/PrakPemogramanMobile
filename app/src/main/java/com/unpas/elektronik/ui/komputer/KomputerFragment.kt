package com.unpas.elektronik.ui.komputer

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
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
import com.unpas.elektronik.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class KomputerFragment : Fragment() {

    val db by lazy { KomputerDatabase(requireContext()) }
    private lateinit var jenisAdapter: ArrayAdapter<KomputerData.Jenis>
    private var isUpgrade: Boolean = true

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_komputer, container, false)
        val tableLayout: TableLayout = root.findViewById(R.id.tableLayout)
        val horizontalScrollView: HorizontalScrollView = root.findViewById(R.id.horizontalScrollView)

        CoroutineScope(Dispatchers.IO).launch {
            val komputerList = db.komputerDao().getAllKomputers()

            requireActivity().runOnUiThread {
                for (index in komputerList.indices) {
                    val komputer = komputerList[index]
                    val tableRow = TableRow(requireContext())

                    val merkCell = createTableCell(komputer.merk)
                    val jenisCell = createTableCell(komputer.jenis)
                    val hargaCell = createTableCell(komputer.harga.toString())
                    val upgradeCell = createTableCell(komputer.dapat_diupgrade.toString())
                    val spesifikasiCell = createTableCell(komputer.spesifikasi)

                    // Set warna latar belakang
                    if (index % 2 == 0) {
                        tableRow.setBackgroundColor(Color.parseColor("#FFA3FD"))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor("#E5B8F4"))
                    }

                    tableRow.addView(merkCell)
                    tableRow.addView(jenisCell)
                    tableRow.addView(hargaCell)
                    tableRow.addView(upgradeCell)
                    tableRow.addView(spesifikasiCell)

                    // Add edit icon
                    val editIcon = createEditIcon(komputer)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(komputer)
                    tableRow.addView(deleteIcon)

                    tableLayout.addView(tableRow)
                }
            }
        }

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomSheetView = inflater.inflate(R.layout.bottom_sheet_komputer, container, false)

            val merkText = bottomSheetView.findViewById<EditText>(R.id.merkText)
            val jenisText = bottomSheetView.findViewById<Spinner>(R.id.jenisText)
            val hargaText = bottomSheetView.findViewById<EditText>(R.id.hargaText)
            val upgradeText = bottomSheetView.findViewById<CheckBox>(R.id.upgradeText)
            val spesifikasiText = bottomSheetView.findViewById<EditText>(R.id.spesifikasiText)
            val button = bottomSheetView.findViewById<Button>(R.id.komputerButton)

            // Inisialisasi Spinner
            val jenisAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                KomputerData.Jenis.values().map { it.name }
            )

            jenisText.adapter = jenisAdapter

            val retrofit = Retrofit.Builder()
                .baseUrl("https://ppm-api.gusdya.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val komputerApi = retrofit.create(KomputerApi::class.java)

            button.setOnClickListener {
                val merk = merkText.text.toString()
                val jenis = jenisText.selectedItem.toString()
                val hargaText = hargaText.text.toString()
                val spesifikasi = spesifikasiText.text.toString()

                if (merk.isEmpty() || jenis.isEmpty() || hargaText.isEmpty() || spesifikasi.isEmpty()) {
                    requireActivity().runOnUiThread {
                        showToast("Harap isi data terlebih dahulu")
                    }
                } else {
                    try {
                        val harga = hargaText.toInt()
                        isUpgrade = upgradeText.isChecked
                        val komputerData = KomputerData(0, merk, jenis, harga, isUpgrade, spesifikasi)

                        CoroutineScope(Dispatchers.IO).launch {
                            db.komputerDao().insertKomputer(komputerData)

                            // Tambahkan data ke endpoint menggunakan Retrofit
                            try {
                                val response = komputerApi.addKomputer(komputerData)
                                if (response.isSuccessful) {
                                    bottomSheetDialog.dismiss()
                                } else {
                                    requireActivity().runOnUiThread {
                                    showToast("Gagal menambahkan data ke server")
                                    }
                                }
                            } catch (e: Exception) {
                                requireActivity().runOnUiThread {
                                showToast("Gagal menambahkan data ke server: ${e.message}")

                                }
                            }
                        }

                        bottomSheetDialog.dismiss()
                        requireActivity().runOnUiThread {
                        showToast("Data berhasil ditambahkan")

                        }
                        refreshKomputerList()
                    } catch (e: NumberFormatException) {
                        requireActivity().runOnUiThread {
                        showToast("Harga harus berisi angka")

                        }
                    }
                }

                refreshKomputerList()
            }

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }

        return root
    }

    private fun refreshKomputerList() {
        CoroutineScope(Dispatchers.IO).launch {
            val komputerList = db.komputerDao().getAllKomputers()

            requireActivity().runOnUiThread {
                val tableLayout: TableLayout = requireView().findViewById(R.id.tableLayout)
                val childCount = tableLayout.childCount

                // Remove all views except the header row
                tableLayout.removeViews(1, childCount - 1)

                for (index in komputerList.indices) {
                    val komputer = komputerList[index]
                    val tableRow = TableRow(requireContext())

                    val merkCell = createTableCell(komputer.merk)
                    val jenisCell = createTableCell(komputer.jenis)
                    val hargaCell = createTableCell(komputer.harga.toString())
                    val upgradeCell = createTableCell(komputer.dapat_diupgrade.toString())
                    val spesifikasiCell = createTableCell(komputer.spesifikasi)

                    // Set warna latar belakang
                    if (index % 2 == 0) {
                        tableRow.setBackgroundColor(Color.parseColor("#FFA3FD"))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor("#E5B8F4"))
                    }

                    tableRow.addView(merkCell)
                    tableRow.addView(jenisCell)
                    tableRow.addView(hargaCell)
                    tableRow.addView(upgradeCell)
                    tableRow.addView(spesifikasiCell)

                    // Add edit icon
                    val editIcon = createEditIcon(komputer)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(komputer)
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

    private fun createEditIcon(komputer: KomputerData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_edit_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            editKomputer(komputer)
        }
        return imageView
    }

    @SuppressLint("MissingInflatedId")
    private fun editKomputer(komputer: KomputerData) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_komputer, null)

        val merkText = bottomSheetView.findViewById<EditText>(R.id.merkText)
        val jenisText = bottomSheetView.findViewById<Spinner>(R.id.jenisText)
        val hargaText = bottomSheetView.findViewById<EditText>(R.id.hargaText)
        val upgradeText = bottomSheetView.findViewById<CheckBox>(R.id.upgradeText)
        val spesifikasiText = bottomSheetView.findViewById<EditText>(R.id.spesifikasiText)
        val button = bottomSheetView.findViewById<Button>(R.id.komputerButton)

        merkText.setText(komputer.merk)
        val jenisAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            KomputerData.Jenis.values().map { it.name }
        )
        jenisText.adapter = jenisAdapter
        val jenisPosition = jenisAdapter.getPosition(komputer.jenis.toString())
        jenisText.setSelection(jenisPosition)
        hargaText.setText(komputer.harga.toString())
        upgradeText.isChecked = komputer.dapat_diupgrade
        spesifikasiText.setText(komputer.spesifikasi)

        button.text = "Update Data Komputer"

        button.setOnClickListener {
            val updatedInput1 = merkText.text.toString()
            val updatedInput2 = jenisText.selectedItem.toString()
            val updatedInput3Text = hargaText.text.toString()
            val updatedInput5 = spesifikasiText.text.toString()

            if (updatedInput1.isEmpty() || updatedInput2.isEmpty() || updatedInput3Text.isEmpty() || updatedInput5.isEmpty()) {
                requireActivity().runOnUiThread {
                showToast("Harap isi semua data terlebih dahulu")

                }
            } else {
                try {
                    val updatedInput3 = updatedInput3Text.toInt()
                    val updatedInput4 = upgradeText.isChecked

                    CoroutineScope(Dispatchers.IO).launch {
                        val updatedKomputer = komputer.copy(
                            merk = updatedInput1,
                            jenis = updatedInput2,
                            harga = updatedInput3,
                            dapat_diupgrade = updatedInput4,
                            spesifikasi = updatedInput5
                        )
                        db.komputerDao().updateKomputer(updatedKomputer)
                    }

                    bottomSheetDialog.dismiss()
                    requireActivity().runOnUiThread {
                    showToast("Data telah diperbarui")

                    }
                    refreshKomputerList()
                } catch (e: NumberFormatException) {
                    requireActivity().runOnUiThread {
                    showToast("Harga harus berisi angka")

                    }
                }
            }

            refreshKomputerList()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun createDeleteIcon(komputer: KomputerData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_delete_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            deleteKomputer(komputer)
        }
        return imageView
    }

    private fun deleteKomputer(komputer: KomputerData) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Apakah Anda yakin ingin menghapus data ini?")
            .setCancelable(false)
            .setPositiveButton("Ya") { dialog, id ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.komputerDao().deleteKomputer(komputer)
                    refreshKomputerList()
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