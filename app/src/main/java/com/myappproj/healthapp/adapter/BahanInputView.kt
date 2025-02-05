import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.myappproj.healthapp.R

class BahanInputView(private val items: List<String>) :
    RecyclerView.Adapter<BahanInputView.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textInputLayout: TextInputLayout = itemView.findViewById(R.id.input_bahan)
        val judulBahanTextView: TextView = itemView.findViewById(R.id.judul_bahan)
        val listMarkImageView: ImageView = itemView.findViewById(R.id.list_mark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inputlayoutbahan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textInputLayout.hint = items[position]
        holder.judulBahanTextView.text = "Bahan-bahan" // Set judul untuk TextView baru
        holder.listMarkImageView.setImageResource(R.drawable.menu) // Set gambar untuk ImageView baru
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
