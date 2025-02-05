import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.myappproj.healthapp.R

class KadarFilterBottomSheet : BottomSheetDialogFragment() {
    private var selectedFilter: String? = null
    private var onFilterApplied: ((String?) -> Unit)? = null
    private lateinit var btnTerapkan: Button
    private lateinit var btnReset: Button
    private lateinit var dotTinggi: View
    private lateinit var dotRendah: View

    fun setOnFilterAppliedListener(listener: (String?) -> Unit) {
        onFilterApplied = listener
    }

    override fun getTheme(): Int {
        return R.style.RoundedBottomSheetDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheetInternal = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            ) as? FrameLayout

            bottomSheetInternal?.let { bottomSheet ->
                bottomSheet.setBackgroundResource(R.drawable.bottom_sheet_background)

                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnReset = view.findViewById(R.id.btn_reset)
        val layoutTinggi: LinearLayout = view.findViewById(R.id.layout_tinggi)
        val layoutRendah: LinearLayout = view.findViewById(R.id.layout_rendah)
        dotTinggi = view.findViewById(R.id.dot_tinggi)
        dotRendah = view.findViewById(R.id.dot_rendah)
        btnTerapkan = view.findViewById(R.id.btn_terapkan)

        // Tombol Terapkan selalu aktif dari awal
        btnTerapkan.isEnabled = true
        btnTerapkan.alpha = 1.0f

        // Tambahkan listener untuk tombol reset
        btnReset.setOnClickListener {
            // Reset pilihan
            selectedFilter = null

            // Reset dot
            dotTinggi.isSelected = false
            dotRendah.isSelected = false

            // Panggil listener dengan null untuk menandakan reset
            onFilterApplied?.invoke(null) // Mengindikasikan reset
            dismiss() // Tutup bottom sheet
        }

        // Atur listener untuk item filter
        layoutTinggi.setOnClickListener {
            selectedFilter = "Tinggi"
            updateFilterSelection(true)
        }

        layoutRendah.setOnClickListener {
            selectedFilter = "Rendah"
            updateFilterSelection(false)
        }

        // Tombol Terapkan
        btnTerapkan.setOnClickListener {
            onFilterApplied?.invoke(selectedFilter)
            dismiss()
        }
    }

    private fun updateFilterSelection(isTinggi: Boolean) {
        dotTinggi.isSelected = isTinggi
        dotRendah.isSelected = !isTinggi
    }

    companion object {
        const val TAG = "KadarFilterBottomSheet"
    }
}