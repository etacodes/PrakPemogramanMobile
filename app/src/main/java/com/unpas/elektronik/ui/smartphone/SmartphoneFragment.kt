package com.unpas.elektronik.ui.smartphone

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import com.google.android.material.textfield.TextInputEditText
import com.unpas.elektronik.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar

class SmartphoneFragment : Fragment() {

    val db by lazy { SmartphoneDatabase(requireContext()) }
    private lateinit var sisOpsAdapter: ArrayAdapter<SmartphoneData.SistemOperasi>

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_smartphone, container, false)
        val tableLayout: TableLayout = root.findViewById(R.id.tableLayout)
        val horizontalScrollView: HorizontalScrollView = root.findViewById(R.id.horizontalScrollView)

        CoroutineScope(Dispatchers.IO).launch {
            val smartphoneList = db.smartphoneDao().getAllSmartphones()

            requireActivity().runOnUiThread {
                for (index in smartphoneList.indices) {
                    val smartphone = smartphoneList[index]
                    val tableRow = TableRow(requireContext())

                    val modelCell = createTableCell(smartphone.model)
                    val warnaCell = createTableCell(smartphone.warna)
                    val storageCell = createTableCell(smartphone.storage.toString())
                    val tanggalRilisCell = createTableCell(smartphone.tanggal_rilis)
                    val sistemCell = createTableCell(smartphone.sistem_operasi)

                    // Set warna latar belakang
                    if (index % 2 == 0) {
                        tableRow.setBackgroundColor(Color.parseColor("#FFA3FD"))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor("#E5B8F4"))
                    }

                    tableRow.addView(modelCell)
                    tableRow.addView(warnaCell)
                    tableRow.addView(storageCell)
                    tableRow.addView(tanggalRilisCell)
                    tableRow.addView(sistemCell)

                    // Add edit icon
                    val editIcon = createEditIcon(smartphone)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(smartphone)
                    tableRow.addView(deleteIcon)

                    tableLayout.addView(tableRow)
                }
            }
        }

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomSheetView = inflater.inflate(R.layout.bottom_sheet_smartphone, container, false)

            val modelText = bottomSheetView.findViewById<EditText>(R.id.modelText)
            val warnaText = bottomSheetView.findViewById<EditText>(R.id.warnaText)
            val storageText = bottomSheetView.findViewById<EditText>(R.id.storageText)
            val tanggalRilisText = bottomSheetView.findViewById<EditText>(R.id.tanggalRilisText)
            tanggalRilisText.setOnClickListener(::onTanggalRilisClicked)
            val sistemText = bottomSheetView.findViewById<Spinner>(R.id.sisOpsText)
            val button = bottomSheetView.findViewById<Button>(R.id.smartPhoneButton)

            // Inisialisasi Spinner
            val sistemAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                SmartphoneData.SistemOperasi.values().map { it.name }
            )

            sistemText.adapter = sistemAdapter

            val retrofit = Retrofit.Builder()
                .baseUrl("https://ppm-api.gusdya.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val smartphoneApi = retrofit.create(SmartphoneApi::class.java)

            button.setOnClickListener {
                val model = modelText.text.toString()
                val warna = warnaText.text.toString()
                val storageString = storageText.text.toString()
                val tanggalRilis = tanggalRilisText.text.toString()
                val sistem = sistemText.selectedItem.toString()

                if (model.isEmpty() || warna.isEmpty() || storageString.isEmpty() || tanggalRilis.isEmpty() || sistem.isEmpty()) {
                    requireActivity().runOnUiThread{
                    showToast("Harap isi semua data terlebih dahulu")

                    }
                    return@setOnClickListener
                }

                val storage: Int
                try {
                    storage = storageString.toInt()
                } catch (e: NumberFormatException) {
                    requireActivity().runOnUiThread{
                    showToast("Data harus berisi integer")

                    }
                    return@setOnClickListener
                }

                val smartphoneData = SmartphoneData(0, model, warna, storage, tanggalRilis, sistem)

                CoroutineScope(Dispatchers.IO).launch {
                    db.smartphoneDao().insertSmartphone(smartphoneData)

                    // Tambahkan data ke endpoint menggunakan Retrofit
                    try {
                        val response = smartphoneApi.addSmartphone(smartphoneData)
                        if (response.isSuccessful) {
                            requireActivity().runOnUiThread {
                                requireActivity().runOnUiThread{
                                showToast("Data berhasil ditambahkan")

                                }
                            }
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
                refreshSmartphoneList()
            }

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }

        return root
    }

    fun onTanggalRilisClicked(view: View) {
        val tanggalRilisText = view as TextInputEditText

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            view.context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
                tanggalRilisText.setText(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun refreshSmartphoneList() {
        CoroutineScope(Dispatchers.IO).launch {
            val smartphoneList = db.smartphoneDao().getAllSmartphones()

            requireActivity().runOnUiThread {
                val tableLayout: TableLayout = requireView().findViewById(R.id.tableLayout)
                val childCount = tableLayout.childCount

                // Remove all views except the header row
                tableLayout.removeViews(1, childCount - 1)

                for (index in smartphoneList.indices) {
                    val smartphone = smartphoneList[index]
                    val tableRow = TableRow(requireContext())

                    val modelCell = createTableCell(smartphone.model)
                    val warnaCell = createTableCell(smartphone.warna)
                    val storageCell = createTableCell(smartphone.storage.toString())
                    val tanggalRilisCell = createTableCell(smartphone.tanggal_rilis)
                    val sistemCell = createTableCell(smartphone.sistem_operasi)

                    // Set warna latar belakang
                    if (index % 2 == 0) {
                        tableRow.setBackgroundColor(Color.parseColor("#FFA3FD"))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor("#E5B8F4"))
                    }

                    tableRow.addView(modelCell)
                    tableRow.addView(warnaCell)
                    tableRow.addView(storageCell)
                    tableRow.addView(tanggalRilisCell)
                    tableRow.addView(sistemCell)

                    // Add edit icon
                    val editIcon = createEditIcon(smartphone)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(smartphone)
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

    private fun createEditIcon(smartphone: SmartphoneData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_edit_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            editSmartphone(smartphone)
        }
        return imageView
    }

    @SuppressLint("MissingInflatedId")
    private fun editSmartphone(smartphone: SmartphoneData) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_smartphone, null)

        val modelText = bottomSheetView.findViewById<EditText>(R.id.modelText)
        val warnaText = bottomSheetView.findViewById<EditText>(R.id.warnaText)
        val storageText = bottomSheetView.findViewById<EditText>(R.id.storageText)
        val tanggalRilisText = bottomSheetView.findViewById<EditText>(R.id.tanggalRilisText)
        tanggalRilisText.setOnClickListener(::onTanggalRilisClicked)
        val sistemText = bottomSheetView.findViewById<Spinner>(R.id.sisOpsText)
        val button = bottomSheetView.findViewById<Button>(R.id.smartPhoneButton)

        modelText.setText(smartphone.model)
        warnaText.setText(smartphone.warna)
        storageText.setText(smartphone.storage.toString())
        tanggalRilisText.setText(smartphone.tanggal_rilis)
        val sistemAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            SmartphoneData.SistemOperasi.values().map { it.name }
        )
        sistemText.adapter = sistemAdapter
        val sistemPosition = sistemAdapter.getPosition(smartphone.sistem_operasi.toString())
        sistemText.setSelection(sistemPosition)

        button.text = "Update Data Smartphone"

        button.setOnClickListener {
            val updatedInput1 = modelText.text.toString()
            val updatedInput2 = warnaText.text.toString()
            val updatedInput3String = storageText.text.toString()
            val updatedInput4 = tanggalRilisText.text.toString()
            val updatedInput5 = sistemText.selectedItem.toString()

            if (updatedInput1.isEmpty() || updatedInput2.isEmpty() || updatedInput3String.isEmpty() || updatedInput4.isEmpty() || updatedInput5.isEmpty()) {
                requireActivity().runOnUiThread{
                showToast("Harap isi semua data terlebih dahulu")

                }
                return@setOnClickListener
            }

            val updatedInput3: Int
            try {
                updatedInput3 = updatedInput3String.toInt()
            } catch (e: NumberFormatException) {
                requireActivity().runOnUiThread{
                showToast("Data harus berisi integer")

                }
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val updatedSmartphone = smartphone.copy(
                    model = updatedInput1,
                    warna = updatedInput2,
                    storage = updatedInput3,
                    tanggal_rilis = updatedInput4,
                    sistem_operasi = updatedInput5
                )
                db.smartphoneDao().updateSmartphone(updatedSmartphone)
            }

            bottomSheetDialog.dismiss()
            requireActivity().runOnUiThread {
            showToast("Data telah diperbarui")

            }
            refreshSmartphoneList()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun createDeleteIcon(smartphone: SmartphoneData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_delete_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            deleteSmartphone(smartphone)
        }
        return imageView
    }

    private fun deleteSmartphone(smartphone: SmartphoneData) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Apakah Anda yakin ingin menghapus data ini?")
            .setCancelable(false)
            .setPositiveButton("Ya") { dialog, id ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.smartphoneDao().deleteSmartphone(smartphone)
                    refreshSmartphoneList()
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