package com.josias.pesagempaginainicial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.josias.pesagempaginainicial.data.Pesagem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private val items: List<Pesagem>) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvItemTitle)
        val subtitle: TextView = view.findViewById(R.id.tvItemSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val ts = fmt.format(Date(p.timestamp))
        holder.title.text = "${p.cliente} - $ts"
        holder.subtitle.text = String.format(Locale.US, "%.2f kg | Subtotal: %.2f kg", p.pesoAtual, p.subtotal)
    }

    override fun getItemCount(): Int = items.size
}
